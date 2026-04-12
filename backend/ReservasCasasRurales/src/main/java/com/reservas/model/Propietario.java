package com.reservas.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "propietario")
@Getter
@Setter
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