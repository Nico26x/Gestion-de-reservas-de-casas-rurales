package com.reservas.controller;

import com.reservas.dto.PagoRequestDTO;
import com.reservas.dto.PagoResponseDTO;
import com.reservas.model.Pago;
import com.reservas.service.PagoService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pagos")
@CrossOrigin(origins = "*")
public class PagoController {

    private final PagoService pagoService;

    public PagoController(PagoService pagoService) {
        this.pagoService = pagoService;
    }

    //El cliente registrar pago
    //Protegido: requiere autenticación (cliente o propietario)
    //POST /api/pagos
    @PostMapping
    public ResponseEntity<?> registrarPago(@RequestBody PagoRequestDTO dto, Authentication authentication) {
        try {
            String username = authentication.getName();

            PagoResponseDTO response = pagoService.registrarPago(dto, username);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    //El propietario ve los pagos pendientes de verificación
    //GET /api/pagos/pendientes
    @GetMapping("/pendientes")
    public ResponseEntity<?> obtenerPagosPendientes(Authentication authentication) {
        try {
            String username = authentication.getName();

            List<Pago> pagos = pagoService.obtenerPagosPendientes(username);

            return ResponseEntity.ok(pagos);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    //El propietario verifica y confirma los pagos
    //PUT /api/pagos/{id}/verificar
    @PutMapping("/{id}/verificar")
    public ResponseEntity<?> verificarPago(@PathVariable Long id, Authentication authentication) {
        try {
            String username = authentication.getName();

            PagoResponseDTO response = pagoService.verificarPago(id, username);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
