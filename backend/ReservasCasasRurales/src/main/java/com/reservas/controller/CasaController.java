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

    //POST /api/casas - Crear casa
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
            @RequestParam("fotos") List<MultipartFile> fotos,
            Authentication authentication) throws Exception {

        // === DIAGNÓSTICO DE MULTIPART: Log al entrar al controlador ===
        System.out.println("\n========== [CasaController.crearCasa DIAGNOSTICS] ==========");
        System.out.println("  ✓ Request llegó al controller");
        if (fotos != null && !fotos.isEmpty()) {

            System.out.println("  Cantidad de fotos recibidas: " + fotos.size());

            for (MultipartFile foto : fotos) {

                long fileSizeBytes = foto.getSize();
                long fileSizeMB = fileSizeBytes / (1024 * 1024);

                System.out.println("  Archivo recibido: " + foto.getOriginalFilename());
                System.out.println("  Tamaño: " + fileSizeBytes + " bytes (~" + fileSizeMB + "MB)");
                System.out.println("  Content-Type: " + foto.getContentType());
            }

        } else {
            System.out.println("  ⚠ No se recibieron fotos");
        }

        System.out.println("========================================================\n");

        if (authentication == null) {
            throw new RuntimeException("Usuario no autenticado");
        }

        if (fotos == null || fotos.isEmpty()) {
            throw new RuntimeException("Debe subir al menos una foto");
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

        Casa casa = casaService.crearCasa(dto, fotos, propietario);

        return ResponseEntity.ok(casa);
    }

    //GET /api/casas — Buscar casas por población
    @GetMapping
    public ResponseEntity<List<CasaResponseDTO>> getCasasPorPoblacion(
            @RequestParam(value = "poblacion", required = false) String poblacion) {

        if (poblacion != null) {
            return ResponseEntity.ok(casaService.buscarPorPoblacionConDTO(poblacion));
        }

        return ResponseEntity.ok(casaService.findAllConDTO());
    }

    //GET /api/casas/{id} — Ver detalle de una casa
    @GetMapping("/{id}")
    public CasaResponseDTO getCasaDetalle(@PathVariable Long id) {

        return casaService.buscarPorId(id);
    }

    //DELETE /api/casas/{id} — Eliminar casa
    @DeleteMapping("/{id}")
    
    public ResponseEntity<String> eliminarCasa(
        @PathVariable Long id,
        Authentication authentication) {

    if (authentication == null) {
        throw new RuntimeException("Usuario no autenticado");
    }

    casaService.eliminarCasa(id, authentication.getName());

    return ResponseEntity.ok("Casa eliminada correctamente");
    }

    //PUT /api/casas/{id} — Modificar casa
    @PutMapping(value = "/{id}", consumes = "multipart/form-data")
    public ResponseEntity<?> modificarCasa(@PathVariable Long id, @RequestParam("nombre") String nombre, @RequestParam("direccion") String direccion, @RequestParam("poblacion") String poblacion, @RequestParam(value = "descripcion", required = false) String descripcion, @RequestParam("numeroHabitaciones") int numeroHabitaciones, @RequestParam("numeroBanos") int numeroBanos, @RequestParam("numeroCocinas") int numeroCocinas, @RequestParam("numeroComedores") int numeroComedores, @RequestParam("numeroCamas") Integer numeroCamas, @RequestParam("numeroGarajes") Integer numeroGarajes, @RequestParam("tieneBano") Boolean tieneBano, @RequestParam("tipoCama") TipoCama tipoCama,
            // Fotos opcionales: si se envían reemplazan las actuales, si no se mantienen
            @RequestParam(value = "fotos", required = false) List<MultipartFile> fotos,
            Authentication authentication) throws Exception {

        if (authentication == null) {
            return ResponseEntity.status(401).body("Usuario no autenticado");
        }

        try {
            String username = authentication.getName();

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

            CasaResponseDTO response = casaService.modificarCasa(id, dto, fotos, username);
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}