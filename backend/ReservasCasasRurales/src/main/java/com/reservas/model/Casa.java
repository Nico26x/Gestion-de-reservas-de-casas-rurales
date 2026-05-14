package com.reservas.model;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

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

    @Column(length = 1000)
    private String descripcion;

    // Relación con propietario
    @ManyToOne
    @JoinColumn(name = "propietario_id", nullable = false)
    @JsonIgnore
    private Propietario propietario;

    // Relaciones
    @OneToMany(mappedBy = "casa", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Habitacion> habitaciones;

    @OneToMany(mappedBy = "casa", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Bano> banos;

    @OneToMany(mappedBy = "casa", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Cocina> cocinas;

    @OneToMany(mappedBy = "casa", cascade = CascadeType.ALL)
    private Set<Paquete> paquetes;

    @OneToMany(mappedBy = "casa", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CasaFoto> fotos = new HashSet<>();

    @Column(name = "numero_comedores")
    private Integer numeroComedores;

    @Column(name = "numero_garajes")
    private Integer numeroGarajes;

}
