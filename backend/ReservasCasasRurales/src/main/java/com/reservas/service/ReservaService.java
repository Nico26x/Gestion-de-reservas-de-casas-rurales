package com.reservas.service;

import com.reservas.dto.ReservaRequestDTO;
import com.reservas.dto.ReservaResponseDTO;
import com.reservas.model.*;
import com.reservas.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
public class ReservaService {

    private final CasaRepository casaRepository;
    private final ReservaRepository reservaRepository;
    private final DisponibilidadRepository disponibilidadRepository;
    private final DisponibilidadHabitacionRepository disponibilidadHabitacionRepository;
    private final PaqueteRepository paqueteRepository;

    public ReservaService(CasaRepository casaRepository,
                          ReservaRepository reservaRepository,
                          DisponibilidadRepository disponibilidadRepository,
                          DisponibilidadHabitacionRepository disponibilidadHabitacionRepository,
                          PaqueteRepository paqueteRepository) {
        this.casaRepository = casaRepository;
        this.reservaRepository = reservaRepository;
        this.disponibilidadRepository = disponibilidadRepository;
        this.disponibilidadHabitacionRepository = disponibilidadHabitacionRepository;
        this.paqueteRepository = paqueteRepository;
    }

    @Transactional
    public ReservaResponseDTO realizarReserva(ReservaRequestDTO dto) {
        return realizarReservaInternal(dto);
    }

    private ReservaResponseDTO realizarReservaInternal(ReservaRequestDTO dto) {

        //Validaciones básicas
        if (dto.getCasaId() == null) {
            throw new RuntimeException("Debe enviar el id de la casa");
        }
        if (dto.getFechaEntrada() == null) {
            throw new RuntimeException("Debe enviar la fecha de entrada");
        }
        if (dto.getNumeroNoches() == null || dto.getNumeroNoches() <= 0) {
            throw new RuntimeException("El número de noches debe ser mayor que cero");
        }
        if (dto.getTelefonoCliente() == null || dto.getTelefonoCliente().trim().isEmpty()) {
            throw new RuntimeException("Debe enviar el teléfono de contacto");
        }

        //Validar tipoReserva obligatoriamente ────────────────────────────────
        if (dto.getTipoReserva() == null || dto.getTipoReserva().trim().isEmpty()) {
            throw new RuntimeException("Debe enviar el tipo de reserva");
        }

        String tipoReserva = dto.getTipoReserva().trim().toUpperCase();
        if (!tipoReserva.equals("HABITACIONES") && !tipoReserva.equals("CASA_COMPLETA")) {
            throw new RuntimeException(
                    "El tipo de reserva no es válido. Valores permitidos: HABITACIONES o CASA_COMPLETA"
            );
        }

        Casa casa = casaRepository.findById(dto.getCasaId())
                .orElseThrow(() -> new RuntimeException("Casa no encontrada"));

        //Validar coherencia entre tipoReserva y habitacionIds ────────────────────────────────
        if (tipoReserva.equals("HABITACIONES")) {
            // Si es por habitaciones, debe tener al menos una habitación seleccionada
            if (dto.getHabitacionIds() == null || dto.getHabitacionIds().isEmpty()) {
                throw new RuntimeException(
                        "Si la reserva es por habitaciones, debe seleccionar al menos una habitación"
                );
            }
        } else if (tipoReserva.equals("CASA_COMPLETA")) {
            // Si es casa completa, no debe tener habitaciones seleccionadas
            if (dto.getHabitacionIds() != null && !dto.getHabitacionIds().isEmpty()) {
                throw new RuntimeException(
                        "Si la reserva es de casa completa, no debe enviar habitaciones seleccionadas"
                );
            }
        }

        //Determinar tipo de reserva ──────────────────────────────
        boolean esPorHabitaciones = tipoReserva.equals("HABITACIONES");

        // Si es por habitaciones, verificar que las habitaciones existan en la casa
        List<Habitacion> habitacionesAReservar = new ArrayList<>();
        if (esPorHabitaciones) {
            // Validar que no haya IDs duplicados en habitacionIds
            if (dto.getHabitacionIds().size() != new java.util.HashSet<>(dto.getHabitacionIds()).size()) {
                throw new RuntimeException(
                        "No se puede seleccionar la misma habitación más de una vez"
                );
            }

            for (Long habId : dto.getHabitacionIds()) {
                Habitacion habitacion = casa.getHabitaciones().stream()
                        .filter(h -> h.getId().equals(habId))
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException(
                                "La habitación con id " + habId + " no pertenece a esta casa"
                        ));
                habitacionesAReservar.add(habitacion);
            }
        }

        //Validar disponibilidad día por día. Si algún día no está disponible, no se hace la reserva.
        LocalDate fechaSalida = dto.getFechaEntrada().plusDays(dto.getNumeroNoches());
        
        // Map para guardar disponibilidades bloqueadas y reutilizarlas en la actualización
        Map<LocalDate, Disponibilidad> disponibilidadesBloqueadas = new HashMap<>();

        for (LocalDate fecha = dto.getFechaEntrada();

            fecha.isBefore(fechaSalida);
            fecha = fecha.plusDays(1)) {

            LocalDate fechaEntrada = fecha;
            // Usar bloqueo pesimista para evitar condiciones de carrera
            Disponibilidad disponibilidad = disponibilidadRepository
                    .findByCasaIdAndFechaWithLock(casa.getId(), fecha)
                    .orElseThrow(() -> new RuntimeException(
                            "La casa no está disponible el día " + fechaEntrada + ". " +
                                    "El propietario no ha definido disponibilidad para esa fecha."
                    ));
            
            // Guardar disponibilidad bloqueada para reutilizarla en la fase de actualización
            disponibilidadesBloqueadas.put(fecha, disponibilidad);

            if (esPorHabitaciones) {

                //Validar que la modalidad permita reserva por habitaciones
                if (disponibilidad.getModalidad() == ModalidadDisponibilidad.CASA_ENTERA) {
                    throw new RuntimeException(
                            "El día " + fecha + " solo permite reserva de casa completa, " +
                                    "no por habitaciones."
                    );
                }

                //Validar que cada habitación solicitada esté LIBRE ese día
                for (Habitacion habitacion : habitacionesAReservar) {
                    // Usar bloqueo pesimista para evitar condiciones de carrera
                    DisponibilidadHabitacion dh = disponibilidadHabitacionRepository
                            .findByDisponibilidadIdAndHabitacionIdWithLock(
                                    disponibilidad.getId(),
                                    habitacion.getId()
                            )
                            .orElseThrow(() -> new RuntimeException(
                                    "No se encontró disponibilidad para la habitación " +
                                            habitacion.getId() + " el día " + fechaEntrada
                            ));

                    if (dh.getEstado() != EstadoDisponibilidad.LIBRE) {
                        throw new RuntimeException(
                                "La habitación " + habitacion.getCodigoHabitacion() +
                                        " no está disponible el día " + fecha +
                                        ". Estado: " + dh.getEstado()
                        );
                    }
                }
            }
            else {
                //Validar que la modalidad permita reserva de casa completa
                if (disponibilidad.getModalidad() == ModalidadDisponibilidad.HABITACIONES) {
                    throw new RuntimeException(
                            "El día " + fecha + " solo permite reserva por habitaciones, " +
                                    "no de casa completa."
                    );
                }

                //Validar si hay habitaciones ocupadas, por lo tanto no se puede reservar la casa completa

                // Usar bloqueo pesimista para evitar condiciones de carrera
                List<DisponibilidadHabitacion> habitaciones =
                        disponibilidadHabitacionRepository.findByDisponibilidadIdWithLock(disponibilidad.getId());

                boolean algunaOcupada = habitaciones.stream()
                        .anyMatch(h -> h.getEstado() != EstadoDisponibilidad.LIBRE);

                if (algunaOcupada) {
                    throw new RuntimeException(
                            "No se puede reservar la casa completa el día " + fecha +
                                    " porque hay habitaciones ocupadas"
                        );
                }

                //Validar que la casa esté LIBRE ese día
                if (disponibilidad.getEstadoCasa() != EstadoDisponibilidad.LIBRE) {
                    throw new RuntimeException(
                            "La casa no está disponible el día " + fecha +
                                    ". Estado: " + disponibilidad.getEstadoCasa()
                    );
                }
            }
        }

        //Calcular importe. Buscar el paquete que cubra las fechas solicitadas
        LocalDate fechaFin = dto.getFechaEntrada().plusDays(dto.getNumeroNoches() - 1);

        Paquete paquete = paqueteRepository
                .findByCasaIdAndFechaInicioLessThanEqualAndFechaFinGreaterThanEqual(
                        casa.getId(),
                        dto.getFechaEntrada(),
                        fechaFin
                )
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException(
                        "No existe un paquete de alquiler que cubra las fechas solicitadas"
                ));

        double importe;
        if (esPorHabitaciones) {
            if (paquete.getPrecioHabitacion() == null) {
                throw new RuntimeException(
                        "El paquete no tiene definido un precio por habitación"
                );
            }
            // Importe = precio por noche por habitación × cantidad de habitaciones × número de noches
            importe = paquete.getPrecioHabitacion() * habitacionesAReservar.size() * dto.getNumeroNoches();
        } else {
            if (paquete.getPrecio() == null) {
                throw new RuntimeException(
                        "El paquete no tiene definido un precio para casa completa"
                );
            }
            // Importe = precio por noche de casa completa × número de noches
            importe = paquete.getPrecio() * dto.getNumeroNoches();
        }

        //Generar número de reserva único
        Long numeroReserva = generarNumeroReservaUnico();

        //Calcular anticipo del 20%
        double anticipo = importe * 0.20;

        //Guardar la reserva
        Reserva reserva = new Reserva();
        reserva.setNumeroReserva(numeroReserva);
        reserva.setFechaEntrada(dto.getFechaEntrada());
        reserva.setNumeroNoches(dto.getNumeroNoches());
        reserva.setTelefonoCliente(dto.getTelefonoCliente());
        reserva.setImporte(importe);
        reserva.setAnticipo(anticipo);
        reserva.setEstadoReserva(EstadoReserva.PENDIENTE);
        reserva.setCasaId(casa);
        reserva.setHabitaciones(esPorHabitaciones ? habitacionesAReservar : new ArrayList<>());

        reservaRepository.save(reserva);

        //Actualizar disponibilidad a RESERVADA
        for (LocalDate fecha = dto.getFechaEntrada();
             fecha.isBefore(fechaSalida);
             fecha = fecha.plusDays(1)) {

            // Reutilizar disponibilidad ya bloqueada en la fase de validación
            Disponibilidad disponibilidad = disponibilidadesBloqueadas.get(fecha);

            if (esPorHabitaciones) {
                //Marcar solo las habitaciones reservadas como RESERVADA
                for (Habitacion habitacion : habitacionesAReservar) {
                    // Usar bloqueo pesimista para consistencia con fase de validación
                    DisponibilidadHabitacion dh = disponibilidadHabitacionRepository
                            .findByDisponibilidadIdAndHabitacionIdWithLock(
                                    disponibilidad.getId(),
                                    habitacion.getId()
                            ).get();
                    dh.setEstado(EstadoDisponibilidad.RESERVADA);
                    disponibilidadHabitacionRepository.save(dh);
                }
            }
            else {
                //Marcar la casa completa como RESERVADA
                disponibilidad.setEstadoCasa(EstadoDisponibilidad.RESERVADA);
                disponibilidadRepository.save(disponibilidad);

                //Bloquear todas las habitaciones como reservadas cuand se realiza una reserva de casa completa
                // Usar bloqueo pesimista para consistencia con fase de validación
                List<DisponibilidadHabitacion> habitaciones =
                        disponibilidadHabitacionRepository.findByDisponibilidadIdWithLock(disponibilidad.getId());

                for (DisponibilidadHabitacion dh : habitaciones) {
                    dh.setEstado(EstadoDisponibilidad.RESERVADA);
                    disponibilidadHabitacionRepository.save(dh);
                }
            }
        }

        //Construir y retornar la respuesta
        ReservaResponseDTO response = new ReservaResponseDTO();
        response.setNumeroReserva(numeroReserva);
        response.setFechaEntrada(dto.getFechaEntrada());
        response.setNumeroNoches(dto.getNumeroNoches());
        response.setImporte(importe);
        response.setAnticipo(anticipo);
        response.setNumeroCuentaBancaria(casa.getPropietario().getNumeroCuentaBancaria());
        response.setNombreCasa(casa.getNombre());
        response.setTelefonoCliente(dto.getTelefonoCliente());
        response.setEstado(EstadoReserva.PENDIENTE.name());
        response.setMensaje(
                "Reserva creada exitosamente. Debe consignar $" + anticipo +
                        " antes de 3 días en la cuenta " +
                        casa.getPropietario().getNumeroCuentaBancaria() +
                        " indicando el número de reserva: " + numeroReserva
        );

        return response;
    }

    //Método para obtener las notificaciones de reservas del propietario autenticado
    public List<com.reservas.dto.ReservaNotificacionDTO> obtenerNotificacionesReservas(String nombreCuenta) {
        List<Reserva> reservas = reservaRepository.findByPropietarioNombreCuenta(nombreCuenta);
        
        List<com.reservas.dto.ReservaNotificacionDTO> notificaciones = new ArrayList<>();
        
        for (Reserva reserva : reservas) {
            com.reservas.dto.ReservaNotificacionDTO dto = com.reservas.dto.ReservaNotificacionDTO.builder()
                    .reservaId(reserva.getId())
                    .numeroReserva(reserva.getNumeroReserva())
                    .casaId(reserva.getCasaId().getId())
                    .nombreCasa(reserva.getCasaId().getNombre())
                    .poblacionCasa(reserva.getCasaId().getPoblacion())
                    .fechaEntrada(reserva.getFechaEntrada())
                    .numeroNoches(reserva.getNumeroNoches())
                    .telefonoCliente(reserva.getTelefonoCliente())
                    .importeTotal(reserva.getImporte())
                    .anticipo(reserva.getAnticipo())
                    .estadoReserva(reserva.getEstadoReserva() != null ? reserva.getEstadoReserva().toString() : "")
                    .build();
            
            notificaciones.add(dto);
        }
        
        return notificaciones;
    }

    //Metodo auxiliar para generar un número de reserva único
    private Long generarNumeroReservaUnico() {
        Random random = new Random();
        Long numero;
        do {
            //Genera un número entre 100000 y 999999
            numero = 100000L + (long) (random.nextDouble() * 900000);
        } while (reservaRepository.existsByNumeroReserva(numero));
        return numero;
    }
}
