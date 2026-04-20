package com.reservas.repository;

import com.reservas.model.DisponibilidadHabitacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

@Repository
public interface DisponibilidadHabitacionRepository extends JpaRepository<DisponibilidadHabitacion, Long> {

    List<DisponibilidadHabitacion> findByDisponibilidadIdOrderByHabitacionIdAsc(Long disponibilidadId);

    void deleteByDisponibilidadId(Long disponibilidadId);

    List<DisponibilidadHabitacion> findByDisponibilidadId(Long disponibilidadId);

    // Buscar el estado de una habitación específica en un día específico
    // Usado en la validación de disponibilidad al momento de reservar
    Optional<DisponibilidadHabitacion> findByDisponibilidadIdAndHabitacionId(Long disponibilidadId, Long habitacionId);

    // Consulta con bloqueo pesimista para validación de habitación específica
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT dh FROM DisponibilidadHabitacion dh WHERE dh.disponibilidad.id = :disponibilidadId AND dh.habitacion.id = :habitacionId")
    Optional<DisponibilidadHabitacion> findByDisponibilidadIdAndHabitacionIdWithLock(
            @Param("disponibilidadId") Long disponibilidadId,
            @Param("habitacionId") Long habitacionId
    );

    // Consulta con bloqueo pesimista para validar todas las habitaciones de un día
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT dh FROM DisponibilidadHabitacion dh WHERE dh.disponibilidad.id = :disponibilidadId ORDER BY dh.habitacion.id ASC")
    List<DisponibilidadHabitacion> findByDisponibilidadIdWithLock(@Param("disponibilidadId") Long disponibilidadId);
}