package com.reservas.controller;

import com.reservas.dto.CasaRequestDTO;
import com.reservas.dto.CasaResponseDTO;
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
            @RequestParam(value = "descripcion", required = false) String descripcion,
            @RequestParam("numeroHabitaciones") int numeroHabitaciones,
            @RequestParam("numeroBanos") int numeroBanos,
            @RequestParam("numeroCocinas") int numeroCocinas,
            @RequestParam("numeroComedores") int numeroComedores,
            @RequestParam("numeroCamas") Integer numeroCamas,
            @RequestParam("numeroGarajes") Integer numeroGarajes,
            @RequestParam("tieneBano") Boolean tieneBano,
            @RequestParam("tipoCama") TipoCama tipoCama,
            @RequestParam("foto") MultipartFile foto,
            Authentication authentication) throws Exception {

        // === DIAGNÓSTICO DE MULTIPART: Log al entrar al controlador ===
        System.out.println("\n========== [CasaController.crearCasa DIAGNOSTICS] ==========");
        System.out.println("  ✓ Request llegó al controller");
        if (foto != null && !foto.isEmpty()) {
            long fileSizeBytes = foto.getSize();
            long fileSizeMB = fileSizeBytes / (1024 * 1024);
            System.out.println("  Archivo recibido: " + foto.getOriginalFilename());
            System.out.println("  Tamaño: " + fileSizeBytes + " bytes (~" + fileSizeMB + "MB)");
            System.out.println("  Content-Type: " + foto.getContentType());
        } else {
            System.out.println("  ⚠ No se recibió archivo o está vacío");
        }
        System.out.println("========================================================\n");

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
        dto.setDescripcion(descripcion);
        dto.setNumeroHabitaciones(numeroHabitaciones);
        dto.setNumeroBanos(numeroBanos);
        dto.setNumeroCocinas(numeroCocinas);
        dto.setNumeroComedores(numeroComedores);
        dto.setNumeroCamas(numeroCamas);
        dto.setNumeroGarajes(numeroGarajes);
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

    @GetMapping("/{id}")
    public CasaResponseDTO getCasaDetalle(@PathVariable Long id) {

        return casaService.buscarPorId(id);
    }
}