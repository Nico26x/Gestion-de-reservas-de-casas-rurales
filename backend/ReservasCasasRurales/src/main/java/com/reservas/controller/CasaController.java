package com.reservas.controller;

import com.reservas.dto.CasaRequestDTO;
import com.reservas.model.Casa;
import com.reservas.model.Propietario;
import com.reservas.model.TipoCama;
import com.reservas.service.CasaService;
import com.reservas.service.PropietarioService;

import java.util.List;

import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<Casa> crearCasa(
            @RequestParam("nombre") String nombre,
            @RequestParam("direccion") String direccion,
            @RequestParam("poblacion") String poblacion,
            @RequestParam("numeroHabitaciones") int numeroHabitaciones,
            @RequestParam("numeroBanos") int numeroBanos,
            @RequestParam("numeroCocinas") int numeroCocinas,
            @RequestParam("numeroCamas") Integer numeroCamas,
            @RequestParam("tieneBano") Boolean tieneBano,
            @RequestParam("tipoCama") TipoCama tipoCama,
            @RequestParam("foto") MultipartFile foto,
            Authentication authentication) throws Exception {

        if (authentication == null) {
            throw new RuntimeException("Usuario no autenticado");
        }

        if (foto == null || foto.isEmpty()) {
            throw new RuntimeException("La foto es obligatoria");
        }

        if (numeroHabitaciones <= 0 || numeroBanos <= 0 || numeroCocinas <= 0) {
            throw new RuntimeException("Valores inválidos");
        }

        String username = authentication.getName();

        Propietario propietario = propietarioService.buscarPorUsername(username);

        CasaRequestDTO dto = new CasaRequestDTO();
        dto.setNombre(nombre);
        dto.setDireccion(direccion);
        dto.setPoblacion(poblacion);
        dto.setNumeroHabitaciones(numeroHabitaciones);
        dto.setNumeroBanos(numeroBanos);
        dto.setNumeroCocinas(numeroCocinas);
        dto.setNumeroCamas(numeroCamas);
        dto.setTieneBano(tieneBano);
        dto.setTipoCama(tipoCama);

        Casa casa = casaService.crearCasa(dto, foto, propietario);

        return ResponseEntity.ok(casa);
    }

    @GetMapping
    public ResponseEntity<List<Casa>> getCasasPorPoblacion(
            @RequestParam(value = "poblacion", required = false) String poblacion) {

        if (poblacion != null) {
            return ResponseEntity.ok(casaService.buscarPorPoblacion(poblacion));
        }

        return ResponseEntity.ok(casaService.findAll());
    }
}