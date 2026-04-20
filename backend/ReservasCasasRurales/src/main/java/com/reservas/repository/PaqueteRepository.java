package com.reservas.repository;

import com.reservas.model.Paquete;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface PaqueteRepository extends JpaRepository<Paquete, Long> {

    // Para validar solapamientos
    List<Paquete> findByCasaIdAndFechaInicioLessThanEqualAndFechaFinGreaterThanEqual(
            Long casaId,
            LocalDate fechaFin,
            LocalDate fechaInicio);

    // Para validar que un rango de fechas esté completamente cubierto por un
    // paquete
    List<Paquete> findByCasaIdAndFechaInicioLessThanEqualAndFechaFinGreaterThanEqualOrderByFechaInicio(
            Long casaId,
            LocalDate fechaFin,
            LocalDate fechaInicio);

    // Para buscar paquetes que contengan un rango específico (contención completa)
    // Busca: paquete.fechaInicio <= fechaInicio AND paquete.fechaFin >= fechaFin
    List<Paquete> findByCasaIdAndFechaInicioIsLessThanEqualAndFechaFinIsGreaterThanEqual(
            Long casaId,
            LocalDate fechaInicio,
            LocalDate fechaFin);
}
