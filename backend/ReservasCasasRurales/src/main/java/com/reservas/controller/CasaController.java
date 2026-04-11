package com.reservas.controller;

import com.reservas.dto.CasaRequestDTO;
import com.reservas.model.Casa;
import com.reservas.model.Propietario;
import com.reservas.service.CasaService;
import com.reservas.service.PropietarioService;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/casas")
public class CasaController {

    private final CasaService casaService;
    private final PropietarioService propietarioService;

    public CasaController(CasaService casaService, PropietarioService propietarioService) {
        this.casaService = casaService;
        this.propietarioService = propietarioService;
    }

    @PostMapping(consumes = "multipart/form-data")
public Casa crearCasa(
    @RequestParam String nombre,
    @RequestParam String direccion,
    @RequestParam int numeroHabitaciones,
    @RequestParam int numeroBanos,
    @RequestParam int numeroCocinas,
    @RequestParam MultipartFile foto,
    Authentication authentication
) throws Exception {

    String username = authentication.getName();
    Propietario propietario = propietarioService.buscarPorNombreCuenta(username);

    // Crear DTO manual
    CasaRequestDTO dto = new CasaRequestDTO();
    dto.setNombre(nombre);
    dto.setDireccion(direccion);
    dto.setNumeroHabitaciones(numeroHabitaciones);
    dto.setNumeroBanos(numeroBanos);
    dto.setNumeroCocinas(numeroCocinas);

    return casaService.crearCasa(dto, foto, propietario);
}
}