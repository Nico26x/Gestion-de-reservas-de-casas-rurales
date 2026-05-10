package com.reservas.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Getter
@Setter
@Entity
@Table(name = "paquete")
public class Paquete {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate fechaInicio;
    private LocalDate fechaFin;

    //Precio de la casa completa
    private Double precio;

    //Precio por habitación individual, aplica solo cuando es modalidad HABITACIONES o AMBAS
    @Column(name = "precio_habitacion")
    private Double precioHabitacion;

    @Enumerated(EnumType.STRING)
    @Column(name = "modalidad", nullable = false)
    private ModalidadDisponibilidad modalidad;

    //Relación con casa
    @ManyToOne
    @JoinColumn(name = "casa_id", nullable = false)
    @JsonIgnore
    private Casa casa;
}
