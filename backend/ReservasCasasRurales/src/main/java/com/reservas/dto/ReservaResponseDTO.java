package com.reservas.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ReservaResponseDTO {

    private Long numeroReserva;
    private LocalDate fechaEntrada;
    private Integer numeroNoches;
    private Double importe;
    private Double anticipo;
    private String numeroCuentaBancaria;
    private String nombreCasa;
    private String telefonoCliente;
    private String estado;
    private String mensaje;
}
