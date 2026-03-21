package com.reservas.repository;

import com.reservas.model.Propietario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PropietarioRepository extends JpaRepository<Propietario, String> {

    // Buscar propietario por nombre de cuenta (usado en autenticación)
    Optional<Propietario> findByNombreCuenta(String nombreCuenta);

    // Verificar si ya existe un nombre de cuenta (usado en registro HU1)
    boolean existsByNombreCuenta(String nombreCuenta);
}
