package com.reservas.repository;

import com.reservas.model.EstadoPago;
import com.reservas.model.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PagoRepository extends JpaRepository<Pago, Long> {

    //Obtener todos los pagos de una reserva
    List<Pago> findByReservaId(Long reservaId);

    //Obtener todos los pagos VERIFICADOS de una reserva, para sumar lo que se ha pagado
    List<Pago> findByReservaIdAndEstadoPago(Long reservaId, EstadoPago estadoPago);

    //Suma total de pagos VERIFICADOS de una reserva
    @Query("SELECT COALESCE(SUM(p.monto), 0) FROM Pago p " +
            "WHERE p.reserva.id = :reservaId AND p.estadoPago = 'VERIFICADO'")
    Double sumMontoVerificadoByReservaId(@Param("reservaId") Long reservaId);

    //Buscar pagos pendientes de verificación del propietario
    List<Pago> findByEstadoPagoAndReserva_CasaId_Propietario_NombreCuenta(EstadoPago estadoPago, String nombreCuenta);

    //Verificar si existe un pago no cancelado para una reserva
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Pago p " +
            "WHERE p.reserva.id = :reservaId AND p.estadoPago IN ('PENDIENTE_VERIFICACION', 'VERIFICADO')")
    boolean existsByReservaIdAndNotCanceled(@Param("reservaId") Long reservaId);
}
