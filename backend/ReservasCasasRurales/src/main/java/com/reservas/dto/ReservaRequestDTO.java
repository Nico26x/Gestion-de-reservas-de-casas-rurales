package com.reservas.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class ReservaRequestDTO {

    private Long casaId;
    private LocalDate fechaEntrada;
    private Integer numeroNoches;
    private String telefonoCliente;
    private List<Long> habitacionIds;
}
