package com.reservas.dto;

import java.time.LocalDate;

public class DisponibilidadRequestDTO {

    private Long casaId;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private String modalidad;
    private String estadoCasa;
    private String estadoHabitaciones;

    public Long getCasaId() {
        return casaId;
    }

    public void setCasaId(Long casaId) {
        this.casaId = casaId;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
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

    public String getEstadoHabitaciones() {
        return estadoHabitaciones;
    }

    public void setEstadoHabitaciones(String estadoHabitaciones) {
        this.estadoHabitaciones = estadoHabitaciones;
    }
}