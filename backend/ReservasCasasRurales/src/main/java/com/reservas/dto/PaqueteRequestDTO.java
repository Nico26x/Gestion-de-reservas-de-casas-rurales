package com.reservas.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

import com.reservas.model.ModalidadDisponibilidad;

@Getter
@Setter
public class PaqueteRequestDTO {

    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private Double precio;
    private Long casaId;
    private ModalidadDisponibilidad modalidad;
}
