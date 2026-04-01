package com.reservas.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.reservas.dto.CasaRequestDTO;
import com.reservas.model.*;
import com.reservas.repository.CasaRepository;

@Service
public class CasaService {

    @Autowired
    private CasaRepository casaRepository;

    public Casa crearCasa(CasaRequestDTO dto, Propietario propietario) {

        // ✅ VALIDACIONES
        if (dto.getNumeroHabitaciones() < 3) {
            throw new RuntimeException("Debe tener al menos 3 habitaciones");
        }

        if (dto.getNumeroBanos() < 2) {
            throw new RuntimeException("Debe tener al menos 2 baños");
        }

        if (dto.getNumeroCocinas() < 1) {
            throw new RuntimeException("Debe tener al menos 1 cocina");
        }

        // ✅ CREAR CASA
        Casa casa = new Casa();
        casa.setNombre(dto.getNombre());
        casa.setDireccion(dto.getDireccion());

        // ✅ ASIGNAR PROPIETARIO
        casa.setPropietario(propietario);

        // ✅ HABITACIONES
        List<Habitacion> habitaciones = new ArrayList<>();
        for (int i = 0; i < dto.getNumeroHabitaciones(); i++) {
            Habitacion h = new Habitacion();
            h.setCasa(casa);
            habitaciones.add(h);
        }

        // ✅ BAÑOS
        List<Bano> banos = new ArrayList<>();
        for (int i = 0; i < dto.getNumeroBanos(); i++) {
            Bano b = new Bano();
            b.setCasa(casa);
            banos.add(b);
        }

        // ✅ COCINAS
        List<Cocina> cocinas = new ArrayList<>();
        for (int i = 0; i < dto.getNumeroCocinas(); i++) {
            Cocina c = new Cocina();
            c.setCasa(casa);
            cocinas.add(c);
        }

        // ✅ ASIGNAR RELACIONES
        casa.setHabitaciones(habitaciones);
        casa.setBanos(banos);
        casa.setCocinas(cocinas);

        return casaRepository.save(casa);
    }
}