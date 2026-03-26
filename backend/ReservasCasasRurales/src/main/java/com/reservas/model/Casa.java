package com.reservas.model;
import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "casas")
public class Casa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String direccion;

    // Relación con propietario
    @ManyToOne
    @JoinColumn(name = "propietario_id", nullable = false)
    private Propietario propietario;

    // Relaciones (las implementaremos luego)
    @OneToMany(mappedBy = "casa", cascade = CascadeType.ALL)
    private List<Habitacion> habitaciones;

    @OneToMany(mappedBy = "casa", cascade = CascadeType.ALL)
    private List<Bano> banos;

    @OneToMany(mappedBy = "casa", cascade = CascadeType.ALL)
    private List<Cocina> cocinas;

    // Getters y Setters
}
