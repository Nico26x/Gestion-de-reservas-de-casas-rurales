package com.reservas.service;

import com.reservas.dto.PaqueteRequestDTO;
import com.reservas.model.*;
import com.reservas.repository.CasaRepository;
import com.reservas.repository.PaqueteRepository;

import com.reservas.repository.ReservaRepository;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class PaqueteService {

    private final PaqueteRepository paqueteRepository;
    private final CasaRepository casaRepository;
    private final ReservaRepository reservaRepository;

    public PaqueteService(PaqueteRepository paqueteRepository, CasaRepository casaRepository, ReservaRepository reservaRepository) {
        this.paqueteRepository = paqueteRepository;
        this.casaRepository = casaRepository;
        this.reservaRepository = reservaRepository;
    }

    public Paquete crearPaquete(PaqueteRequestDTO dto, String username) {

    //  VALIDAR FECHAS
    if (dto.getFechaInicio() == null || dto.getFechaFin() == null) {
        throw new RuntimeException("Las fechas son obligatorias");
    }

    if (dto.getFechaInicio().isAfter(dto.getFechaFin())) {
        throw new RuntimeException("La fecha inicio no puede ser mayor que la fecha fin");
    }

    //  VALIDAR PRECIO Y PRECIO_HABITACION SEGÚN MODALIDAD
    if (dto.getModalidad() == null) {
        throw new RuntimeException("La modalidad es obligatoria");
    }

    validarPreciosSegunModalidad(dto.getModalidad(), dto.getPrecio(), dto.getPrecioHabitacion());

    //  BUSCAR CASA
    Casa casa = casaRepository.findById(dto.getCasaId())
            .orElseThrow(() -> new RuntimeException("Casa no encontrada"));

    //  VALIDAR PROPIETARIO 
    if (!casa.getPropietario().getNombreCuenta().equals(username)) {
        throw new RuntimeException("No puedes crear paquetes en casas que no son tuyas");
    }

    //  VALIDAR SOLAPAMIENTOS
    List<Paquete> paquetesSolapados =
            paqueteRepository.findByCasaIdAndFechaInicioLessThanEqualAndFechaFinGreaterThanEqual(
                    dto.getCasaId(),
                    dto.getFechaFin(),
                    dto.getFechaInicio()
            );

    if (!paquetesSolapados.isEmpty()) {
        throw new RuntimeException("Ya existe un paquete en ese rango de fechas");
    }

    //  CREAR
    Paquete paquete = new Paquete();
    paquete.setFechaInicio(dto.getFechaInicio());
    paquete.setFechaFin(dto.getFechaFin());
    paquete.setPrecio(dto.getPrecio());
    paquete.setPrecioHabitacion(dto.getPrecioHabitacion());
    paquete.setCasa(casa);
    paquete.setModalidad(dto.getModalidad());

    return paqueteRepository.save(paquete);
    }

    //Modificar paquete
    public Paquete modificarPaquete(Long paqueteId, PaqueteRequestDTO dto, String username) {

        //Buscar el paquete
        Paquete paquete = paqueteRepository.findById(paqueteId)
                .orElseThrow(() -> new RuntimeException("Paquete no encontrado con id: " + paqueteId));

        //Verificar que el propietario sea el dueño de la casa
        if(!paquete.getCasa().getPropietario().getNombreCuenta().equals(username)) {
            throw new RuntimeException("No puedes modificar paquetes de casas que no son tuyas");
        }

        //Validar fechas
        if (dto.getFechaInicio() == null || dto.getFechaFin() == null) {
            throw new RuntimeException("Las fechas son obligatorias");
        }
        if (dto.getFechaInicio().isAfter(dto.getFechaFin())) {
            throw new RuntimeException("La fecha inicio no puede ser mayor que la fecha fin");
        }

        //Validar modalidad y precios
        if (dto.getModalidad() == null) {
            throw new RuntimeException("La modalidad es obligatoria");
        }

        validarPreciosSegunModalidad(dto.getModalidad(), dto.getPrecio(), dto.getPrecioHabitacion());

        //Buscar reservas activas (PENDIENTES o CONFIRMADAS) de un paquete. Una reserva pertenece a un paquete si su fechaEntrada está dentro del rango del paquete
        List<EstadoReserva> estadosActivos = Arrays.asList(
                EstadoReserva.PENDIENTE, EstadoReserva.CONFIRMADA
        );

        List<Reserva> reservasActivas = reservaRepository.findByCasaIdAndEstadoReservaIn(
                paquete.getCasa(), estadosActivos
        );

        // Filtrar reservas que caen dentro del rango ACTUAL del paquete
        List<Reserva> reservasDelPaquete = reservasActivas.stream()
                .filter(r -> !r.getFechaEntrada().isBefore(paquete.getFechaInicio())
                        && !r.getFechaEntrada().isAfter(paquete.getFechaFin()))
                .toList();

        boolean hayReservas = !reservasDelPaquete.isEmpty();

        //Validar solapamientos con otros paquetes, excluyendo el paquete actual
        List<Paquete> solapados = paqueteRepository.findSolapamientosExcluyendoPaquete(
                paquete.getCasa().getId(),
                paqueteId,
                dto.getFechaInicio(),
                dto.getFechaFin()
        );

        if(!solapados.isEmpty()) {
            throw new RuntimeException("Las nuevas fechas se solapan con otro paquete existente");
        }

        //Validar si hay reservas activas y aplicar restricciones
        if(hayReservas) {

            //No se puede reducir la fechaInicio hacia adelante (dejaría reservas sin cobertura)
            if(dto.getFechaInicio().isAfter(paquete.getFechaInicio())) {
                //Verificar si alguna reserva quedaría fuera del nuevo rango
                boolean reservaFueraDeRango = reservasDelPaquete.stream()
                        .anyMatch(r -> r.getFechaEntrada().isBefore(dto.getFechaInicio()));
                if (reservaFueraDeRango) {
                    throw new RuntimeException("No se puede modificar la fecha inicio: existen reservas activas que quedarían fuera del nuevo rango del paquete");
                }
            }

            //No se puede reducir la fechaFin (dejaría reservas sin cobertura)
            if (dto.getFechaFin().isBefore(paquete.getFechaFin())) {
                boolean reservaFueraDeRango = reservasDelPaquete.stream()
                        .anyMatch(r -> {
                            //Calcular fecha salida de la reserva
                            java.time.LocalDate fechaSalida = r.getFechaEntrada()
                                    .plusDays(r.getNumeroNoches());
                            return fechaSalida.isAfter(dto.getFechaFin());
                        });
                if (reservaFueraDeRango) {
                    throw new RuntimeException(
                            "No se puede reducir la fecha fin: existen reservas activas que quedarían fuera del nuevo rango del paquete");
                }
            }

            //No se puede cambiar la modalidad si hay reservas por habitaciones
            if (!dto.getModalidad().equals(paquete.getModalidad())) {
                boolean hayReservasPorHabitacion = reservasDelPaquete.stream()
                        .anyMatch(r -> r.getHabitaciones() != null && !r.getHabitaciones().isEmpty());

                if (hayReservasPorHabitacion) {
                    throw new RuntimeException(
                            "No se puede cambiar la modalidad: existen reservas por habitaciones activas en este paquete");
                }

                boolean hayReservasCasaCompleta = reservasDelPaquete.stream()
                        .anyMatch(r -> r.getHabitaciones() == null || r.getHabitaciones().isEmpty());

                if (hayReservasCasaCompleta &&
                        dto.getModalidad() == ModalidadDisponibilidad.HABITACIONES) {
                    throw new RuntimeException(
                            "No se puede cambiar la modalidad a HABITACIONES: existen reservas de casa completa activas en este paquete");
                }
            }
        }

        //Guardar los nuevos datos del paquete
        paquete.setFechaInicio(dto.getFechaInicio());
        paquete.setFechaFin(dto.getFechaFin());
        paquete.setPrecio(dto.getPrecio());
        paquete.setPrecioHabitacion(dto.getPrecioHabitacion());
        paquete.setModalidad(dto.getModalidad());

        return paqueteRepository.save(paquete);
    }

    //Método auxiliar para validar precios de acuerdo a la modalidad
    private void validarPreciosSegunModalidad(ModalidadDisponibilidad modalidad, Double precio, Double precioHabitacion) {
        if (modalidad == ModalidadDisponibilidad.CASA_ENTERA) {
            if (precio == null || precio <= 0) {
                throw new RuntimeException("El precio de casa completa debe ser mayor a 0");
            }
        } else if (modalidad == ModalidadDisponibilidad.HABITACIONES) {
            if (precioHabitacion == null || precioHabitacion <= 0) {
                throw new RuntimeException("El precio por habitación debe ser mayor a 0");
            }
        } else if (modalidad == ModalidadDisponibilidad.AMBAS) {
            if (precio == null || precio <= 0) {
                throw new RuntimeException("El precio de casa completa debe ser mayor a 0");
            }
            if (precioHabitacion == null || precioHabitacion <= 0) {
                throw new RuntimeException("El precio por habitación debe ser mayor a 0");
            }
        }
    }
}