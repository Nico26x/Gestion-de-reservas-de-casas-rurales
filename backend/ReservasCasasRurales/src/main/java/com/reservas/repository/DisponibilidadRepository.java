package com.reservas.repository;

import com.reservas.model.Disponibilidad;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DisponibilidadRepository extends JpaRepository<Disponibilidad, Long> {

    Optional<Disponibilidad> findByCasaIdAndFecha(Long casaId, LocalDate fecha);

    List<Disponibilidad> findByCasaIdAndFechaBetweenOrderByFechaAsc(
            Long casaId,
            LocalDate fechaInicio,
            LocalDate fechaFin
    );
}