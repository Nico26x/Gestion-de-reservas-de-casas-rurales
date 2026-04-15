package com.reservas.repository;

import com.reservas.model.DisponibilidadHabitacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DisponibilidadHabitacionRepository extends JpaRepository<DisponibilidadHabitacion, Long> {

    List<DisponibilidadHabitacion> findByDisponibilidadIdOrderByHabitacionIdAsc(Long disponibilidadId);

    void deleteByDisponibilidadId(Long disponibilidadId);

    List<DisponibilidadHabitacion> findByDisponibilidadId(Long disponibilidadId);

    // Buscar el estado de una habitación específica en un día específico
    // Usado en la validación de disponibilidad al momento de reservar
    Optional<DisponibilidadHabitacion> findByDisponibilidadIdAndHabitacionId(Long disponibilidadId, Long habitacionId);
}