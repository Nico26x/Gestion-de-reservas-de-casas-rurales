package com.reservas.dto;

import lombok.*;
@Getter
@Setter
public class CasaRequestDTO {

    private String nombre;
    private String direccion;

    private int numeroHabitaciones;
    private int numeroBanos;
    private int numeroCocinas;

}
