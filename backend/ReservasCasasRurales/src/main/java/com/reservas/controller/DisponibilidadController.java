package com.reservas.controller;

import com.reservas.dto.DisponibilidadDiaResponseDTO;
import com.reservas.dto.DisponibilidadRequestDTO;
import com.reservas.service.DisponibilidadService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/disponibilidad")
@CrossOrigin(origins = "*")
public class DisponibilidadController {

    private final DisponibilidadService disponibilidadService;

    public DisponibilidadController(DisponibilidadService disponibilidadService) {
        this.disponibilidadService = disponibilidadService;
    }

    @PostMapping
    public ResponseEntity<?> registrarDisponibilidad(@RequestBody DisponibilidadRequestDTO dto,
                                                     Authentication authentication) {
        try {
            String username = authentication.getName();
            String mensaje = disponibilidadService.definirDisponibilidad(dto, username);
            return ResponseEntity.ok(Map.of("mensaje", mensaje));
        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("mensaje", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> consultarDisponibilidad(
            @RequestParam("casaId") Long casaId,
            @RequestParam("fechaEntrada") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaEntrada,
            @RequestParam("numeroNoches") int numeroNoches) {
        try {
            List<DisponibilidadDiaResponseDTO> respuesta =
                    disponibilidadService.consultarDisponibilidad(casaId, fechaEntrada, numeroNoches);
            return ResponseEntity.ok(respuesta);
        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("mensaje", e.getMessage()));
        }
    }
}