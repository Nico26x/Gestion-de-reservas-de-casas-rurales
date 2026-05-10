package com.reservas.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservaNotificacionDTO {

    private Long reservaId;
    private Long numeroReserva;
    private Long casaId;
    private String nombreCasa;
    private String poblacionCasa;
    private LocalDate fechaEntrada;
    private Integer numeroNoches;
    private String telefonoCliente;
    private Double importeTotal;
    private Double anticipo;
    private String estadoReserva;
    private LocalDate fechaCreacion;
}
