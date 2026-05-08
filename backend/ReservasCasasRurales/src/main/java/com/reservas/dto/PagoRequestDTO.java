package com.reservas.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class PagoRequestDTO {

    //Número de la reserva al que se asocia el pago
    private Long numeroReserva;

    //Monto que el cliente consignó, mínimo el 20% del total
    private Double monto;

    //Fecha en que se realizó el pago
    private LocalDate fechaPago;

    //Método de pago, EFECTIVO, TRANSFERENCIA, TARJETA
    private String metodoPago;
}
