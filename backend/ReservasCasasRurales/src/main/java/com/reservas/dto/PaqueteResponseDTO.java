package com.reservas.dto;

import com.reservas.model.ModalidadDisponibilidad;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class PaqueteResponseDTO {

    private Long id;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private Double precio;
    private Double precioHabitacion;
    private ModalidadDisponibilidad modalidad;
    private Long casaId;
    private String nombreCasa;
    private String poblacionCasa;
}
