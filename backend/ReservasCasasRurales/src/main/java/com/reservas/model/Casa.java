package com.reservas.model;

import jakarta.persistence.*;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "casas")
public class Casa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;
    private String direccion;
    
    @Column(name = "poblacion")
    private String poblacion;

    @Column(name = "foto")
    private String foto;

    // Relación con propietario
    @ManyToOne
    @JoinColumn(name = "propietario_id", nullable = false)
    @JsonIgnore
    private Propietario propietario;

    // Relaciones 
    @OneToMany(mappedBy = "casa", cascade = CascadeType.ALL)
    private List<Habitacion> habitaciones;

    @OneToMany(mappedBy = "casa", cascade = CascadeType.ALL)
    private List<Bano> banos;

    @OneToMany(mappedBy = "casa", cascade = CascadeType.ALL)
    private List<Cocina> cocinas;

    @OneToMany(mappedBy = "casa", cascade = CascadeType.ALL)
    private List<Paquete> paquetes;
}
