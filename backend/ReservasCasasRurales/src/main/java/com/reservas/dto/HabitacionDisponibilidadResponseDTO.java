package com.reservas.dto;

public class HabitacionDisponibilidadResponseDTO {

    private Long id;
    private String codigoHabitacion;
    private String estado;

    public HabitacionDisponibilidadResponseDTO(Long id, String codigoHabitacion, String estado) {
        this.id = id;
        this.codigoHabitacion = codigoHabitacion;
        this.estado = estado;
    }

    public Long getId() {
        return id;
    }

    public String getCodigoHabitacion() {
        return codigoHabitacion;
    }

    public String getEstado() {
        return estado;
    }
}