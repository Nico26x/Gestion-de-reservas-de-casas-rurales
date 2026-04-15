package com.reservas.controller;

import com.reservas.dto.ReservaRequestDTO;
import com.reservas.dto.ReservaResponseDTO;
import com.reservas.service.ReservaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reservas")
@CrossOrigin(origins = "*")
public class ReservaController {

    @Autowired
    private final ReservaService reservaService;

    public ReservaController(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    //Método POST /api/reservas. El cliente no necesita autenticación para realizar una reserva
    @PostMapping
    public ResponseEntity<?> realizarReserva(@RequestBody ReservaRequestDTO dto){

        try {
            ReservaResponseDTO response = reservaService.realizarReserva(dto);
            return ResponseEntity.ok(response);
        }
        catch(RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
