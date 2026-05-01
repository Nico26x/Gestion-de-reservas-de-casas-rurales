package com.reservas.exception;

import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

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

    /**
     * Maneja excepciones de tamaño de archivo excedido.
     * Se ejecuta cuando el tamaño del archivo o el request total supera los límites configurados.
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<?> handleMaxUploadSizeExceeded(
            MaxUploadSizeExceededException e,
            HttpServletRequest request) {
        
        // Diagnóstico completo: imprimir detalles del request y causa raíz
        System.out.println("\n========== [MULTIPART ERROR - MaxUploadSizeExceededException] ==========");
        System.out.println("  URI: " + request.getRequestURI());
        System.out.println("  Method: " + request.getMethod());
        System.out.println("  Content-Length: " + request.getContentLengthLong() + " bytes");
        if (request.getContentLengthLong() > 0) {
            System.out.println("  Content-Length (MB): ~" + (request.getContentLengthLong() / (1024 * 1024)));
        }
        System.out.println("  Content-Type: " + request.getContentType());
        
        // Excepción
        System.out.println("\n  Exception Stack:");
        System.out.println("    Class: " + e.getClass().getName());
        System.out.println("    Message: " + e.getMessage());
        
        // Causa raíz completa
        Throwable cause = e.getCause();
        int level = 0;
        while (cause != null && level < 10) {
            System.out.println("    Cause[" + level + "]: " + cause.getClass().getName() + " - " + cause.getMessage());
            cause = cause.getCause();
            level++;
        }
        System.out.println("========================================================================\n");
        
        // Respuesta al cliente
        Map<String, String> response = new HashMap<>();
        response.put("mensaje", "El archivo supera el tamaño máximo permitido (máximo 25MB)");
        response.put("error", "PAYLOAD_TOO_LARGE");
        
        return ResponseEntity
                .status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(response);
    }
}
