package com.reservas.dto;

import java.util.List;

import lombok.*;

@Getter
@Setter
public class CasaResponseDTO {

    private Long id;
    private String nombre;
    private String direccion;
    private String poblacion;
    private String descripcion;
    private List<String> fotos;

    private Integer numeroHabitaciones;
    private Integer numeroBanos;
    private Integer numeroCocinas;
    private Integer numeroComedores;
    private Integer numeroGarajes;
    
    private List<HabitacionDetalleDTO> habitaciones;
}
