package com.reservas.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(
        name = "disponibilidad",
        uniqueConstraints = @UniqueConstraint(columnNames = {"casa_id", "fecha"})
)
public class Disponibilidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "casa_id", nullable = false)
    private Casa casa;

    @Column(nullable = false)
    private LocalDate fecha;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ModalidadDisponibilidad modalidad;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_casa", nullable = false)
    private EstadoDisponibilidad estadoCasa;

    @JsonIgnore
    @OneToMany(mappedBy = "disponibilidad", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DisponibilidadHabitacion> habitaciones = new ArrayList<>();
}