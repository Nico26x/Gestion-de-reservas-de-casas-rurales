package com.reservas.repository;

import com.reservas.model.Casa;
import com.reservas.model.EstadoReserva;
import com.reservas.model.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    //Busca una reserva por su respectivo número
    Optional<Reserva> findByNumeroReserva(Long numeroReserva);

    //Verifica si ya existe una reserva con ese número
    boolean existsByNumeroReserva(Long numeroReserva);

    //Busca todas las reservas de una casa específica
    List<Reserva> findByCasaId(Casa casaId);

    //Lista reservas de una casa con plazo vencido (EXPIRADA)
    List<Reserva> findByCasaIdAndEstadoReserva(Casa casaId, EstadoReserva estadoReserva);

    //Buscar reservas activas de una casa (PENDIENTE o CONFIRMADA)
    List<Reserva> findByCasaIdAndEstadoReservaIn(Casa casaId, List<EstadoReserva> estadosReservas);

    //Buscar reservas que se solapan con el rango de fechas especificado
    List<Reserva> findByCasaIdAndFechaEntradaLessThanAndEstadoReservaIn(
            Casa casaId,
            LocalDate fechaSalida,
            List<EstadoReserva> estadosReservas
    );

    //Buscar todas las reservas de un propietario por su nombre de cuenta
    @Query("SELECT r FROM Reserva r WHERE r.casaId.propietario.nombreCuenta = :nombreCuenta ORDER BY r.id DESC")
    List<Reserva> findByPropietarioNombreCuenta(@Param("nombreCuenta") String nombreCuenta);
    
    @Query("""
    SELECT r
    FROM Reserva r
    WHERE r.estadoReserva = :estado
    AND r.fechaCreacion <= :fechaLimite
    AND r.casaId.propietario.nombreCuenta = :nombreCuenta
  """)
    List<Reserva> buscarReservasVencidasPorPropietario(
        @Param("estado") EstadoReserva estado,
        @Param("fechaLimite") LocalDate fechaLimite,
        @Param("nombreCuenta") String nombreCuenta
);
}
