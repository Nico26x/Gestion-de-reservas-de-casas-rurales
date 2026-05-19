package com.reservas.repository;

import com.reservas.model.Disponibilidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import jakarta.persistence.LockModeType;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DisponibilidadRepository extends JpaRepository<Disponibilidad, Long> {

    Optional<Disponibilidad> findByCasaIdAndFecha(Long casaId, LocalDate fecha);

    List<Disponibilidad> findByCasaIdAndFechaBetweenOrderByFechaAsc(
            Long casaId,
            LocalDate fechaInicio,
            LocalDate fechaFin
    );

    // Consulta con bloqueo pesimista para validación y reserva
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT d FROM Disponibilidad d WHERE d.casa.id = :casaId AND d.fecha = :fecha")
    Optional<Disponibilidad> findByCasaIdAndFechaWithLock(@Param("casaId") Long casaId, @Param("fecha") LocalDate fecha);

    // Buscar todas las disponibilidades de una casa
    List<Disponibilidad> findByCasaId(Long casaId);

    // Eliminar todas las disponibilidades de una casa
    @Modifying
    @Query("DELETE FROM Disponibilidad d WHERE d.casa.id = :casaId")
    void deleteByCasaId(@Param("casaId") Long casaId);
}