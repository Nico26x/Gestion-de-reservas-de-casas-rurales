package com.reservas.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "propietario")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Propietario {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_propietario")
    private String idPropietario;

    @Column(name = "nombre_cuenta", nullable = false, unique = true)
    private String nombreCuenta;

    @Column(name = "contrasena", nullable = false)
    private String contrasena;
}
