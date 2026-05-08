package com.reservas.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class PagoResponseDTO {

    private Long idPago;
    private Long numeroReserva;
    private Double monto;
    private LocalDate fechaPago;
    private String metodoPago;

    //Detalles del pago de la reserva
    private Double importeTotal;
    private Double anticipo;
    private Double montoRestante;

    // Número de cuenta del propietario
    private String numeroCuentaBancaria;

    // Estado del pago y de la reserva
    private String estadoPago;
    private String estadoReserva;

    private String mensaje;
}
