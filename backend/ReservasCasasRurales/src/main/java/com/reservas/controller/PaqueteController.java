package com.reservas.controller;

import com.reservas.dto.PaqueteRequestDTO;
import com.reservas.model.Paquete;
import com.reservas.service.PaqueteService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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

    //PUT /api/paquetes/{id}
    @PutMapping
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
}