package com.reservas.dto;

import java.time.LocalDate;
import java.util.List;

public class DisponibilidadDiaResponseDTO {

    private LocalDate fecha;
    private String modalidad;
    private String estadoCasa;
    private List<HabitacionDisponibilidadResponseDTO> habitaciones;

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public String getModalidad() {
        return modalidad;
    }

    public void setModalidad(String modalidad) {
        this.modalidad = modalidad;
    }

    public String getEstadoCasa() {
        return estadoCasa;
    }

    public void setEstadoCasa(String estadoCasa) {
        this.estadoCasa = estadoCasa;
    }

    public List<HabitacionDisponibilidadResponseDTO> getHabitaciones() {
        return habitaciones;
    }

    public void setHabitaciones(List<HabitacionDisponibilidadResponseDTO> habitaciones) {
        this.habitaciones = habitaciones;
    }
}