package com.reservas.exception;

import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

/**
 * Manejador global de excepciones a nivel de aplicación.
 * Captura excepciones de concurrencia/locking que pueden ocurrir tanto en el cuerpo
 * de los métodos transaccionales como en el momento del commit de la transacción.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Maneja excepciones de concurrencia y locking pesimista.
     * Se ejecuta al capturar PessimisticLockingFailureException (que incluye subclases
     * como CannotAcquireLockException), lo cual permite capturar errores no solo en
     * el cuerpo del método sino también durante el commit de la transacción.
     */
    @ExceptionHandler(PessimisticLockingFailureException.class)
    public ResponseEntity<?> handlePessimisticLockingFailure(
            PessimisticLockingFailureException e,
            WebRequest request) {
        
        String mensajeUsuario = "La disponibilidad cambió mientras se procesaba la reserva. Intente nuevamente.";
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(mensajeUsuario);
    }

    /**
     * Maneja excepciones de timeout de queries/locks.
     * Captura QueryTimeoutException que puede ocurrir durante validaciones
     * o al hacer commit en condiciones de alta concurrencia.
     */
    @ExceptionHandler(QueryTimeoutException.class)
    public ResponseEntity<?> handleQueryTimeout(
            QueryTimeoutException e,
            WebRequest request) {
        
        String mensajeUsuario = "La disponibilidad cambió mientras se procesaba la reserva. Intente nuevamente.";
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(mensajeUsuario);
    }
}
