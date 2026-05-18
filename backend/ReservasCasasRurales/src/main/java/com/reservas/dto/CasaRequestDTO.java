package com.reservas.dto;

import com.reservas.model.TipoCama;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CasaRequestDTO {

    private String nombre;
    private String direccion;
    private String poblacion;
    private String descripcion;

    private int numeroHabitaciones;
    private int numeroBanos;
    private int numeroCocinas;
    private Integer numeroComedores;
    private Integer numeroGarajes;

    private Integer numeroCamas;
    private Boolean tieneBano;
    private TipoCama tipoCama;

    // Nuevos campos opcionales para HU3 - Detalles individualizados
    private String habitacionesJson;
    private String cocinasJson;
}