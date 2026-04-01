package com.reservas.controller;

import com.reservas.dto.CasaRequestDTO;
import com.reservas.model.Casa;
import com.reservas.model.Propietario;
import com.reservas.service.CasaService;
import com.reservas.service.PropietarioService;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/casas")
public class CasaController {

    private final CasaService casaService;
    private final PropietarioService propietarioService;

    public CasaController(CasaService casaService, PropietarioService propietarioService) {
        this.casaService = casaService;
        this.propietarioService = propietarioService;
    }

    @PostMapping
    public Casa crearCasa(@RequestBody CasaRequestDTO dto, Authentication authentication) {

        // 🔐 Obtener usuario autenticado
        String username = authentication.getName();

        // 🔎 Buscar propietario en BD
        Propietario propietario = propietarioService.buscarPorUsername(username);

        // 🚀 Crear casa
        return casaService.crearCasa(dto, propietario);
    }
}