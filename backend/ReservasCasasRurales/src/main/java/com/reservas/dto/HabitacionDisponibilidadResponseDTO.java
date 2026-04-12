package com.reservas.dto;

public class HabitacionDisponibilidadResponseDTO {

    private String codigoHabitacion;
    private String estado;

    public HabitacionDisponibilidadResponseDTO(String codigoHabitacion, String estado) {
        this.codigoHabitacion = codigoHabitacion;
        this.estado = estado;
    }

    public String getCodigoHabitacion() {
        return codigoHabitacion;
    }

    public String getEstado() {
        return estado;
    }
}