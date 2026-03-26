package com.reservas.service;

import com.reservas.dto.CasaRequestDTO;
import com.reservas.model.*;
import com.reservas.repository.CasaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CasaService {

    @Autowired
    private CasaRepository casaRepository;

    public Casa crearCasa(CasaRequestDTO dto) {

        // 🔴 VALIDACIONES
        if (dto.getNumeroHabitaciones() < 3) {
            throw new RuntimeException("Debe tener al menos 3 habitaciones");
        }

        if (dto.getNumeroBanos() < 2) {
            throw new RuntimeException("Debe tener al menos 2 baños");
        }

        if (dto.getNumeroCocinas() < 1) {
            throw new RuntimeException("Debe tener al menos 1 cocina");
        }

        Casa casa = new Casa();
        casa.setNombre(dto.getNombre());
        casa.setDireccion(dto.getDireccion());

        // Crear habitaciones
        List<Habitacion> habitaciones = new ArrayList<>();
        for (int i = 0; i < dto.getNumeroHabitaciones(); i++) {
            Habitacion h = new Habitacion();
            h.setCasa(casa);
            habitaciones.add(h);
        }

        // Crear baños
        List<Bano> banos = new ArrayList<>();
        for (int i = 0; i < dto.getNumeroBanos(); i++) {
            Bano b = new Bano();
            b.setCasa(casa);
            banos.add(b);
        }

        // Crear cocinas
        List<Cocina> cocinas = new ArrayList<>();
        for (int i = 0; i < dto.getNumeroCocinas(); i++) {
            Cocina c = new Cocina();
            c.setCasa(casa);
            cocinas.add(c);
        }

        casa.setHabitaciones(habitaciones);
        casa.setBanos(banos);
        casa.setCocinas(cocinas);

        return casaRepository.save(casa);
    }
}
