package com.reservas.repository;

import com.reservas.model.Paquete;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface PaqueteRepository extends JpaRepository<Paquete, Long> {

    //Para validar solapamientos al crear
    List<Paquete> findByCasaIdAndFechaInicioLessThanEqualAndFechaFinGreaterThanEqual(
            Long casaId,
            LocalDate fechaFin,
            LocalDate fechaInicio);

    // Para validar que un rango de fechas esté completamente cubierto por un paquete
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

    //Para validar solapamientos al modificar, excluye el paquete que se está editando
    @Query("SELECT p FROM Paquete p WHERE p.casa.id = :casaId " +
            "AND p.id <> :paqueteId " +
            "AND p.fechaInicio <= :fechaFin " +
            "AND p.fechaFin >= :fechaInicio")
    List<Paquete> findSolapamientosExcluyendoPaquete(
            @Param("casaId") Long casaId,
            @Param("paqueteId") Long paqueteId,
            @Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFin") LocalDate fechaFin);

    // Obtener todos los paquetes de las casas del propietario autenticado
    @Query("SELECT p FROM Paquete p WHERE p.casa.propietario.nombreCuenta = :nombreCuenta ORDER BY p.fechaInicio DESC")
    List<Paquete> findByCasaPropietarioNombreCuenta(@Param("nombreCuenta") String nombreCuenta);

    // Eliminar todos los paquetes de una casa
    @Modifying
    @Query("DELETE FROM Paquete p WHERE p.casa.id = :casaId")
    void deleteByCasaId(@Param("casaId") Long casaId);
}
