package com.reservas.repository;

import com.reservas.model.DisponibilidadHabitacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DisponibilidadHabitacionRepository extends JpaRepository<DisponibilidadHabitacion, Long> {

    List<DisponibilidadHabitacion> findByDisponibilidadIdOrderByHabitacionIdAsc(Long disponibilidadId);

    void deleteByDisponibilidadId(Long disponibilidadId);
}