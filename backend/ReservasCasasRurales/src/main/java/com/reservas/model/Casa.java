package com.reservas.model;

import jakarta.persistence.*;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

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

    // Getters para contar elementos en las colecciones
    @JsonProperty("numeroHabitaciones")
    public Integer getNumeroHabitaciones() {
        return habitaciones != null ? habitaciones.size() : 0;
    }

    @JsonProperty("numeroBanos")
    public Integer getNumeroBanos() {
        return banos != null ? banos.size() : 0;
    }

    @JsonProperty("numeroCocinas")
    public Integer getNumeroCocinas() {
        return cocinas != null ? cocinas.size() : 0;
    }
}
