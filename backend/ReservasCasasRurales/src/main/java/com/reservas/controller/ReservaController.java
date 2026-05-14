package com.reservas.controller;

import com.reservas.dto.ReservaRequestDTO;
import com.reservas.dto.ReservaResponseDTO;
import com.reservas.dto.ReservaNotificacionDTO;
import com.reservas.service.ReservaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservas")
@CrossOrigin(origins = "*")
public class ReservaController {

    @Autowired
    private final ReservaService reservaService;

    public ReservaController(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    // Método POST /api/reservas. El cliente no necesita autenticación para realizar
    // una reserva
    @PostMapping
    public ResponseEntity<?> realizarReserva(@RequestBody ReservaRequestDTO dto) {

        try {
            ReservaResponseDTO response = reservaService.realizarReserva(dto);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Método GET /api/reservas/propietario. Obtiene las notificaciones de reservas
    // del propietario autenticado
    @GetMapping("/propietario")
    public ResponseEntity<?> obtenerReservasPropietario(Authentication authentication) {
        try {
            if (authentication == null) {
                return ResponseEntity.badRequest().body("Usuario no autenticado");
            }

            String nombreCuenta = authentication.getName();
            List<ReservaNotificacionDTO> notificaciones = reservaService.obtenerNotificacionesReservas(nombreCuenta);

            return ResponseEntity.ok(notificaciones);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}/cancelar")
    public ResponseEntity<?> cancelarReserva(
            @PathVariable Long id,
            Authentication authentication) {

        try {

            if (authentication == null) {
                return ResponseEntity.badRequest()
                        .body("Usuario no autenticado");
            }

            String nombreCuenta = authentication.getName();

            String response = reservaService.cancelarReserva(id, nombreCuenta);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {

            return ResponseEntity.badRequest()
                    .body(e.getMessage());
        }
    }
    @GetMapping("/vencidas")
    public ResponseEntity<?> listarReservasVencidas(Authentication authentication) {

    return ResponseEntity.ok(
            reservaService.listarReservasVencidas(authentication.getName())
    );
}
    @PutMapping("/{id}/vencida")
    public ResponseEntity<?> gestionarReservaVencida(
        @PathVariable Long id,
        @RequestParam String accion,
        Authentication authentication) {

    return ResponseEntity.ok(
            reservaService.gestionarReservaVencida(
                    id,
                    accion,
                    authentication.getName()
            )
    );
}
}
