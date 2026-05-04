package com.reservas.dto;

import lombok.*;

@Getter
@Setter
public class CasaResponseDTO {

    private Long id;
    private String nombre;
    private String direccion;
    private String poblacion;
    private String descripcion;
    private String foto;

    private Integer numeroHabitaciones;
    private Integer numeroBanos;
    private Integer numeroCocinas;
    private Integer numeroComedores;
    private Integer numeroGarajes;
    
}
