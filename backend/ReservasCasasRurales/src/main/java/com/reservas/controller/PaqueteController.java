package com.reservas.controller;

import com.reservas.dto.PaqueteRequestDTO;
import com.reservas.dto.PaqueteResponseDTO;
import com.reservas.model.Paquete;
import com.reservas.service.PaqueteService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/paquetes")
public class PaqueteController {

    private final PaqueteService paqueteService;

    public PaqueteController(PaqueteService paqueteService) {
        this.paqueteService = paqueteService;
    }

    //POST /api/paquetes
    @PostMapping
    public ResponseEntity<Paquete> crearPaquete(@RequestBody PaqueteRequestDTO dto, Authentication authentication) {

        if (authentication == null) {
            return ResponseEntity.status(401).build();
        }

        String username = authentication.getName();

        Paquete paquete = paqueteService.crearPaquete(dto, username);

        return ResponseEntity.ok(paquete);
    }

    //GET /api/paquetes/propietario
    @GetMapping("/propietario")
    public ResponseEntity<?> obtenerPaquetesDelPropietario(Authentication authentication) {

        if (authentication == null) {
            return ResponseEntity.status(401).build();
        }

        try {
            String username = authentication.getName();
            List<PaqueteResponseDTO> paquetes = paqueteService.obtenerPaquetesDelPropietario(username);
            return ResponseEntity.ok(paquetes);
        } catch(RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    //PUT /api/paquetes/{id}
    @PutMapping("{id}")
    public ResponseEntity<?> modificarPaquete(@PathVariable Long id, @RequestBody PaqueteRequestDTO dto, Authentication authentication) {

        if(authentication == null) {
            return ResponseEntity.status(401).build();
        }

        try {
            String username = authentication.getName();
            Paquete paquete = paqueteService.modificarPaquete(id, dto, username);
            return ResponseEntity.ok(paquete);
        } catch(RuntimeException e) {

            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    //GET /api/paquetes/{id}
    @GetMapping("{id}")
    public ResponseEntity<?> obtenerPaquete(@PathVariable Long id, Authentication authentication) {

        if (authentication == null) {
            return ResponseEntity.status(401).build();
        }

        try {
            String username = authentication.getName();
            PaqueteResponseDTO paquete = paqueteService.obtenerPaquete(id, username);
            return ResponseEntity.ok(paquete);
        } catch(RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    //GET /api/paquetes?casaId={id}
    @GetMapping
    public ResponseEntity<?> obtenerPaquetesPorCasa(@RequestParam(value = "casaId", required = false) Long casaId, Authentication authentication) {

        if (authentication == null) {
            return ResponseEntity.status(401).build();
        }

        if (casaId == null) {
            return ResponseEntity.badRequest().body("El parámetro casaId es requerido");
        }

        try {
            String username = authentication.getName();
            List<PaqueteResponseDTO> paquetes = paqueteService.obtenerPaquetesPorCasa(casaId, username);
            return ResponseEntity.ok(paquetes);
        } catch(RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}