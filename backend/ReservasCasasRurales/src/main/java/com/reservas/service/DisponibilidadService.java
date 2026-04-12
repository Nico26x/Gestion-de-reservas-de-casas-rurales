package com.reservas.service;

import com.reservas.dto.DisponibilidadDiaResponseDTO;
import com.reservas.dto.DisponibilidadRequestDTO;
import com.reservas.dto.HabitacionDisponibilidadResponseDTO;
import com.reservas.model.*;
import com.reservas.repository.CasaRepository;
import com.reservas.repository.DisponibilidadHabitacionRepository;
import com.reservas.repository.DisponibilidadRepository;
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

    public DisponibilidadService(CasaRepository casaRepository,
                                 DisponibilidadRepository disponibilidadRepository,
                                 DisponibilidadHabitacionRepository disponibilidadHabitacionRepository) {
        this.casaRepository = casaRepository;
        this.disponibilidadRepository = disponibilidadRepository;
        this.disponibilidadHabitacionRepository = disponibilidadHabitacionRepository;
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
                                        "HAB-" + habitacion.getId(),
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
                                    "HAB-" + dh.getHabitacion().getId(),
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
        // Aquí va la validación real cuando exista la HU12 con Reserva.
        // Por ahora queda preparado para no inventar una tabla que aún no existe.
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

    private EstadoDisponibilidad ajustarEstadoCasaSegunModalidad(ModalidadDisponibilidad modalidad,
                                                                 EstadoDisponibilidad estadoCasa) {
        if (modalidad == ModalidadDisponibilidad.HABITACIONES) {
            return EstadoDisponibilidad.NO_DISPONIBLE;
        }
        return estadoCasa;
    }

    private EstadoDisponibilidad ajustarEstadoHabitacionesSegunModalidad(ModalidadDisponibilidad modalidad,
                                                                         EstadoDisponibilidad estadoHabitaciones) {
        if (modalidad == ModalidadDisponibilidad.CASA_ENTERA) {
            return EstadoDisponibilidad.NO_DISPONIBLE;
        }
        return estadoHabitaciones;
    }
}