package com.reservas.service;

import com.reservas.dto.PagoRequestDTO;
import com.reservas.dto.PagoResponseDTO;
import com.reservas.model.*;
import com.reservas.repository.PagoRepository;
import com.reservas.repository.ReservaRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.time.LocalDate;

@Service
public class PagoService {

    private final PagoRepository pagoRepository;
    private final ReservaRepository reservaRepository;

    public PagoService(PagoRepository pagoRepository, ReservaRepository reservaRepository) {
        this.pagoRepository = pagoRepository;
        this.reservaRepository = reservaRepository;
    }

    //El cliente registra el pago
    @Transactional
    public PagoResponseDTO registrarPago(PagoRequestDTO dto, String usernameCliente) {

        //Validaciones básicas
        if (dto.getNumeroReserva() == null) {
            throw new RuntimeException("Debe enviar el número de reserva");
        }
        if (dto.getMonto() == null || dto.getMonto() <= 0) {
            throw new RuntimeException("El monto debe ser mayor que cero");
        }
        if (dto.getMetodoPago() == null || dto.getMetodoPago().trim().isEmpty()) {
            throw new RuntimeException("Debe enviar el método de pago");
        }

        MetodoPago metodoPago;
        try {
            metodoPago = MetodoPago.valueOf(dto.getMetodoPago().trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Método de pago inválido. Valores permitidos: EFECTIVO, TRANSFERENCIA, TARJETA");
        }

        //Buscar la reserva
        Reserva reserva = reservaRepository.findByNumeroReserva(dto.getNumeroReserva())
                .orElseThrow(() -> new RuntimeException("No existe una reserva con el número: " + dto.getNumeroReserva()));

        //Verificar que la reserva NO esté CONFIRMADA
        if (reserva.getEstadoReserva() == EstadoReserva.CONFIRMADA) {
            throw new RuntimeException("No se puede registrar pago porque la reserva ya está confirmada.");
        }

        //Verificar que la reserva no esté CANCELADA
        if (reserva.getEstadoReserva() == EstadoReserva.CANCELADA) {
            throw new RuntimeException("No se puede registrar pago porque la reserva está cancelada.");
        }

        //Verificar que no exista otro pago registrado para esta reserva
        if (pagoRepository.existsByReservaIdAndNotCanceled(reserva.getId())) {
            throw new RuntimeException("Ya existe un pago registrado para esta reserva.");
        }

        //Calcular cuánto se ha pagado, solo pagos VERIFICADOS
        Double totalVerificado = pagoRepository.sumMontoVerificadoByReservaId(reserva.getId());

        //Validar que no se exceda el total de la reserva
        if (totalVerificado >= reserva.getImporte()) {
            throw new RuntimeException("Esta reserva ya está completamente pagada. Total pagado: $" + totalVerificado);
        }

        double montoRestantePorPagar = reserva.getImporte() - totalVerificado;

        if (dto.getMonto() > montoRestantePorPagar) {
            throw new RuntimeException(
                    "El monto ingresado ($" + dto.getMonto() + ") excede el valor restante por pagar ($"
                            + montoRestantePorPagar + ")"
            );
        }

        //Validar que el primer pago cubra mínimo el anticipo del 20% del total
        if (totalVerificado == 0 && dto.getMonto() < reserva.getAnticipo()) {
            throw new RuntimeException("El primer pago debe ser mínimo el anticipo de $" + reserva.getAnticipo());
        }

        // Usar fecha actual si no se envía fecha
        LocalDate fechaPago = dto.getFechaPago() != null ? dto.getFechaPago() : LocalDate.now();

        //Guardar el pago con estado VERIFICADO (HU13: pago registrado = pago verificado)
        Pago pago = new Pago();
        pago.setFechaPago(fechaPago);
        pago.setMonto(dto.getMonto());
        pago.setMetodoPago(metodoPago);
        pago.setEstadoPago(EstadoPago.VERIFICADO);
        pago.setReserva(reserva);

        pagoRepository.save(pago);

        //Actualizar estado de la reserva a CONFIRMADA inmediatamente (HU13)
        reserva.setEstadoReserva(EstadoReserva.CONFIRMADA);
        reservaRepository.save(reserva);

        //Construir respuesta con detalle del pago
        double montoRestanteDespuesDePagar = montoRestantePorPagar - dto.getMonto();
        String numeroCuenta = reserva.getCasaId().getPropietario().getNumeroCuentaBancaria();

        PagoResponseDTO response = new PagoResponseDTO();
        response.setIdPago(pago.getId());
        response.setNumeroReserva(reserva.getNumeroReserva());
        response.setMonto(pago.getMonto());
        response.setFechaPago(pago.getFechaPago());
        response.setMetodoPago(metodoPago.name());
        response.setImporteTotal(reserva.getImporte());
        response.setAnticipo(reserva.getAnticipo());
        response.setMontoRestante(montoRestanteDespuesDePagar < 0 ? 0 : montoRestanteDespuesDePagar);
        response.setNumeroCuentaBancaria(numeroCuenta);
        response.setEstadoPago(EstadoPago.VERIFICADO.name());
        response.setEstadoReserva(EstadoReserva.CONFIRMADA.name());
        response.setMensaje("Pago registrado y verificado correctamente. La reserva ha sido confirmada. " +
                            "Monto restante por pagar: $" + (montoRestanteDespuesDePagar < 0 ? 0 : montoRestanteDespuesDePagar));

        return response;
    }

    //El propietario ve los pagos pendientes de verificación
    @Transactional
    public List<Pago> obtenerPagosPendientes(String usernamePropietario) {
        return pagoRepository.findByEstadoPagoAndReserva_CasaId_Propietario_NombreCuenta(EstadoPago.PENDIENTE_VERIFICACION, usernamePropietario);
    }

    //El propietario verifica el pago y confirma la reserva
    @Transactional
    public PagoResponseDTO verificarPago(Long pagoId, String usernamePropietario) {

        Pago pago = pagoRepository.findById(pagoId)
                .orElseThrow(() -> new RuntimeException("Pago no encontrado con id: " + pagoId));

        //Verificar que el pago pertenece a una casa del propietario autenticado
        if (!pago.getReserva().getCasaId().getPropietario()
                .getNombreCuenta().equals(usernamePropietario)) {
            throw new RuntimeException(
                    "No puedes verificar pagos de reservas que no pertenecen a tus casas"
            );
        }

        //Verificar que el pago esté pendiente de verificación
        if (pago.getEstadoPago() == EstadoPago.VERIFICADO) {
            throw new RuntimeException("El pago ya fue verificado.");
        }

        Reserva reserva = pago.getReserva();

        //Verificar que la reserva NO esté CONFIRMADA (ya está confirmada al registrar pago)
        if (reserva.getEstadoReserva() == EstadoReserva.CONFIRMADA) {
            throw new RuntimeException("La reserva ya está confirmada. No es necesario verificar el pago nuevamente.");
        }

        //Verificar que la reserva no esté CANCELADA
        if (reserva.getEstadoReserva() == EstadoReserva.CANCELADA) {
            throw new RuntimeException("No se puede verificar el pago porque la reserva está cancelada.");
        }

        //Actualizar estado del pago a VERIFICADO
        pago.setEstadoPago(EstadoPago.VERIFICADO);
        pagoRepository.save(pago);

        //Si la reserva no está confirmada, confirmarla (no debería ocurrir en el flujo actual)
        if (reserva.getEstadoReserva() == EstadoReserva.PENDIENTE ||
                reserva.getEstadoReserva() == EstadoReserva.EXPIRADA) {
            reserva.setEstadoReserva(EstadoReserva.CONFIRMADA);
            reservaRepository.save(reserva);
        }

        //Calcular el total de pagos verificados después de este pago
        Double totalVerificado = pagoRepository.sumMontoVerificadoByReservaId(reserva.getId());
        double montoRestante = reserva.getImporte() - totalVerificado;

        String mensaje;
        if (montoRestante <= 0) {
            mensaje = "Pago verificado. La reserva " + reserva.getNumeroReserva() +
                    " está COMPLETAMENTE PAGADA.";
        } else {
            mensaje = "Pago verificado. Reserva confirmada. Monto restante por pagar: $" + montoRestante;
        }

        PagoResponseDTO response = new PagoResponseDTO();
        response.setIdPago(pago.getId());
        response.setNumeroReserva(reserva.getNumeroReserva());
        response.setMonto(pago.getMonto());
        response.setFechaPago(pago.getFechaPago());
        response.setMetodoPago(pago.getMetodoPago().name());
        response.setImporteTotal(reserva.getImporte());
        response.setAnticipo(reserva.getAnticipo());
        response.setMontoRestante(montoRestante < 0 ? 0 : montoRestante);
        response.setNumeroCuentaBancaria(reserva.getCasaId().getPropietario().getNumeroCuentaBancaria());
        response.setEstadoPago(EstadoPago.VERIFICADO.name());
        response.setEstadoReserva(reserva.getEstadoReserva().name());
        response.setMensaje(mensaje);

        return response;
    }
}
