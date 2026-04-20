package com.reservas.service;

import com.reservas.dto.DisponibilidadDiaResponseDTO;
import com.reservas.dto.DisponibilidadRequestDTO;
import com.reservas.dto.HabitacionDisponibilidadResponseDTO;
import com.reservas.model.*;
import com.reservas.repository.CasaRepository;
import com.reservas.repository.DisponibilidadHabitacionRepository;
import com.reservas.repository.DisponibilidadRepository;
import com.reservas.repository.PaqueteRepository;
import com.reservas.repository.ReservaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class DisponibilidadService {

    private final CasaRepository casaRepository;
    private final DisponibilidadRepository disponibilidadRepository;
    private final DisponibilidadHabitacionRepository disponibilidadHabitacionRepository;
    private final PaqueteRepository paqueteRepository;
    private final ReservaRepository reservaRepository;

    public DisponibilidadService(CasaRepository casaRepository,
                                 DisponibilidadRepository disponibilidadRepository,
                                 DisponibilidadHabitacionRepository disponibilidadHabitacionRepository,
                                 PaqueteRepository paqueteRepository,
                                 ReservaRepository reservaRepository) {
        this.casaRepository = casaRepository;
        this.disponibilidadRepository = disponibilidadRepository;
        this.disponibilidadHabitacionRepository = disponibilidadHabitacionRepository;
        this.paqueteRepository = paqueteRepository;
        this.reservaRepository = reservaRepository;
    }

    @Transactional
    public String definirDisponibilidad(DisponibilidadRequestDTO dto, String username) {

        if (dto.getCasaId() == null) {
            throw new RuntimeException("Debe enviar el id de la casa");
        }

        if (dto.getFechaInicio() == null || dto.getFechaFin() == null) {
            throw new RuntimeException("Debe enviar fechaInicio y fechaFin");
        }

        if (dto.getFechaInicio().isAfter(dto.getFechaFin())) {
            throw new RuntimeException("La fecha de inicio no puede ser mayor que la fecha fin");
        }

        Casa casa = casaRepository.findById(dto.getCasaId())
                .orElseThrow(() -> new RuntimeException("Casa no encontrada"));

        if (!casa.getPropietario().getNombreCuenta().equals(username)) {
            throw new RuntimeException("No puedes definir disponibilidad para una casa que no te pertenece");
        }

        ModalidadDisponibilidad modalidad = parsearModalidad(dto.getModalidad());
        EstadoDisponibilidad estadoCasa = parsearEstado(dto.getEstadoCasa());
        EstadoDisponibilidad estadoHabitaciones = parsearEstado(dto.getEstadoHabitaciones());

        //  VALIDAR QUE EXISTA UN PAQUETE QUE CUBRA COMPLETAMENTE EL RANGO DE FECHAS
        List<Paquete> paquetesCubrentes = paqueteRepository
                .findByCasaIdAndFechaInicioIsLessThanEqualAndFechaFinIsGreaterThanEqual(
                        casa.getId(),
                        dto.getFechaInicio(),
                        dto.getFechaFin()
                );

        if (paquetesCubrentes.isEmpty()) {
            throw new RuntimeException(
                    "No se puede definir disponibilidad fuera de las fechas cubiertas por un paquete. " +
                    "Debe crear un paquete que incluya el rango " + dto.getFechaInicio() + " a " + dto.getFechaFin()
            );
        }

        //  VALIDAR QUE LA MODALIDAD DE DISPONIBILIDAD SEA COMPATIBLE CON LA MODALIDAD DEL PAQUETE
        Paquete paqueteAplicable = paquetesCubrentes.get(0);
        validarCompatibilidadModalidad(paqueteAplicable.getModalidad(), modalidad);

        LocalDate fechaActual = dto.getFechaInicio();

        while (!fechaActual.isAfter(dto.getFechaFin())) {

            validarQueNoContradigaReservas(casa.getId(), fechaActual);

            Disponibilidad disponibilidad = disponibilidadRepository
                    .findByCasaIdAndFecha(casa.getId(), fechaActual)
                    .orElseGet(Disponibilidad::new);

            disponibilidad.setCasa(casa);
            disponibilidad.setFecha(fechaActual);
            disponibilidad.setModalidad(modalidad);
            disponibilidad.setEstadoCasa(ajustarEstadoCasaSegunModalidad(modalidad, estadoCasa));

            disponibilidad = disponibilidadRepository.save(disponibilidad);

            disponibilidadHabitacionRepository.deleteByDisponibilidadId(disponibilidad.getId());

            if (casa.getHabitaciones() != null) {
                for (Habitacion habitacion : casa.getHabitaciones()) {
                    DisponibilidadHabitacion dh = new DisponibilidadHabitacion();
                    dh.setDisponibilidad(disponibilidad);
                    dh.setHabitacion(habitacion);
                    dh.setEstado(ajustarEstadoHabitacionesSegunModalidad(modalidad, estadoHabitaciones));
                    disponibilidadHabitacionRepository.save(dh);
                }
            }

            fechaActual = fechaActual.plusDays(1);
        }

        return "Disponibilidad registrada correctamente";
    }

    @Transactional(readOnly = true)
    public List<DisponibilidadDiaResponseDTO> consultarDisponibilidad(Long casaId,
                                                                      LocalDate fechaEntrada,
                                                                      int numeroNoches) {

        if (casaId == null) {
            throw new RuntimeException("Debe enviar el id de la casa");
        }

        if (fechaEntrada == null) {
            throw new RuntimeException("Debe enviar la fecha de entrada");
        }

        if (numeroNoches <= 0) {
            throw new RuntimeException("El número de noches debe ser mayor que cero");
        }

        Casa casa = casaRepository.findById(casaId)
                .orElseThrow(() -> new RuntimeException("Casa no encontrada"));

        List<DisponibilidadDiaResponseDTO> respuesta = new ArrayList<>();

        for (int i = 0; i < numeroNoches; i++) {
            LocalDate fecha = fechaEntrada.plusDays(i);

            DisponibilidadDiaResponseDTO dia = new DisponibilidadDiaResponseDTO();
            dia.setFecha(fecha);

            Disponibilidad disponibilidad = disponibilidadRepository.findByCasaIdAndFecha(casaId, fecha).orElse(null);

            if (disponibilidad == null) {
                dia.setModalidad(ModalidadDisponibilidad.AMBAS.name());
                dia.setEstadoCasa(EstadoDisponibilidad.NO_DISPONIBLE.name());

                List<HabitacionDisponibilidadResponseDTO> habitaciones = new ArrayList<>();
                if (casa.getHabitaciones() != null) {
                    for (Habitacion habitacion : casa.getHabitaciones()) {
                        habitaciones.add(
                                new HabitacionDisponibilidadResponseDTO(
                                        habitacion.getId(),
                                        habitacion.getCodigoHabitacion(),
                                        EstadoDisponibilidad.NO_DISPONIBLE.name()
                                )
                        );
                    }
                }
                dia.setHabitaciones(habitaciones);
            } else {
                dia.setModalidad(disponibilidad.getModalidad().name());
                dia.setEstadoCasa(disponibilidad.getEstadoCasa().name());

                List<DisponibilidadHabitacion> habitacionesDb =
                        disponibilidadHabitacionRepository.findByDisponibilidadIdOrderByHabitacionIdAsc(disponibilidad.getId());

                List<HabitacionDisponibilidadResponseDTO> habitaciones = new ArrayList<>();
                for (DisponibilidadHabitacion dh : habitacionesDb) {
                    habitaciones.add(
                            new HabitacionDisponibilidadResponseDTO(
                                    dh.getHabitacion().getId(),
                                    dh.getHabitacion().getCodigoHabitacion(),
                                    dh.getEstado().name()
                            )
                    );
                }
                dia.setHabitaciones(habitaciones);
            }

            respuesta.add(dia);
        }

        return respuesta;
    }

    private void validarQueNoContradigaReservas(Long casaId, LocalDate fecha) {
        Casa casa = new Casa();
        casa.setId(casaId);

        List<EstadoReserva> estadosActivos = List.of(EstadoReserva.PENDIENTE, EstadoReserva.CONFIRMADA);

        //Buscar reservas activas que cubran esta fecha
        LocalDate fechaSiguiente = fecha.plusDays(1);
        List<Reserva> reservasEnFecha = reservaRepository
                .findByCasaIdAndFechaEntradaLessThanAndEstadoReservaIn(
                        casa,
                        fechaSiguiente,
                        estadosActivos
                );

        for (Reserva reserva : reservasEnFecha) {
            LocalDate fechaSalida = reserva.getFechaEntrada().plusDays(reserva.getNumeroNoches());

            //Si la reserva cubre esta fecha, no se puede redefinir disponibilidad
            //porque eso borraría y recrearía los estados del día, sobrescribiendo la reserva
            if (reserva.getFechaEntrada().isBefore(fechaSiguiente) && fecha.isBefore(fechaSalida)) {
                throw new RuntimeException(
                        "No se puede redefinir la disponibilidad de la fecha " + fecha + 
                        " porque ya tiene reservas activas"
                );
            }
        }
    }

    private void validarCompatibilidadModalidad(ModalidadDisponibilidad modalidadPaquete,
                                                  ModalidadDisponibilidad modalidadDisponibilidad) {
        //Si el paquete es CASA_ENTERA, la disponibilidad solo puede ser CASA_ENTERA
        if (modalidadPaquete == ModalidadDisponibilidad.CASA_ENTERA &&
            modalidadDisponibilidad != ModalidadDisponibilidad.CASA_ENTERA) {
            throw new RuntimeException(
                    "La modalidad de disponibilidad no es compatible con la modalidad del paquete (CASA_ENTERA). " +
                    "Solo se puede definir disponibilidad de CASA_ENTERA."
            );
        }

        //Si el paquete es HABITACIONES, la disponibilidad solo puede ser HABITACIONES
        if (modalidadPaquete == ModalidadDisponibilidad.HABITACIONES &&
            modalidadDisponibilidad != ModalidadDisponibilidad.HABITACIONES) {
            throw new RuntimeException(
                    "La modalidad de disponibilidad no es compatible con la modalidad del paquete (HABITACIONES). " +
                    "Solo se puede definir disponibilidad de HABITACIONES."
            );
        }

        //Si el paquete es AMBAS, cualquier modalidad de disponibilidad es válida
    }

    private ModalidadDisponibilidad parsearModalidad(String modalidad) {
        try {
            return ModalidadDisponibilidad.valueOf(modalidad.toUpperCase());
        } catch (Exception e) {
            throw new RuntimeException("Modalidad inválida. Usa CASA_ENTERA, HABITACIONES o AMBAS");
        }
    }

    private EstadoDisponibilidad parsearEstado(String estado) {
        try {
            return EstadoDisponibilidad.valueOf(estado.toUpperCase());
        } catch (Exception e) {
            throw new RuntimeException("Estado inválido. Usa LIBRE, RESERVADA o NO_DISPONIBLE");
        }
    }

    // Ajusta el estado de la casa según la modalidad
    private EstadoDisponibilidad ajustarEstadoCasaSegunModalidad(ModalidadDisponibilidad modalidad,
                                                                 EstadoDisponibilidad estadoCasa) {
        // Si la modalidad es HABITACIONES, el estado de la casa no aplica
        if (modalidad == ModalidadDisponibilidad.HABITACIONES) {
            return EstadoDisponibilidad.LIBRE;
        }
        // Si es CASA_ENTERA o AMBAS, devolver el estado como está
        return estadoCasa;
    }

    // Ajusta el estado de habitaciones según la modalidad
    private EstadoDisponibilidad ajustarEstadoHabitacionesSegunModalidad(ModalidadDisponibilidad modalidad,
                                                                         EstadoDisponibilidad estadoHabitaciones) {
        // Si la modalidad es CASA_ENTERA, el estado de habitaciones no aplica
        if (modalidad == ModalidadDisponibilidad.CASA_ENTERA) {
            return EstadoDisponibilidad.LIBRE;
        }
        // Si es HABITACIONES o AMBAS, devolver el estado como está
        return estadoHabitaciones;
    }
}