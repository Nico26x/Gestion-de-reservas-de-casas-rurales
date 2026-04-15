package com.reservas.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.reservas.dto.CasaRequestDTO;
import com.reservas.model.*;
import com.reservas.repository.CasaRepository;

@Service
public class CasaService {

    @Autowired
    private CasaRepository casaRepository;
    @Autowired
    private ImageServiceImpl imageService;

    public Casa crearCasa(CasaRequestDTO dto, MultipartFile foto, Propietario propietario) throws Exception {

        // VALIDACIONES
        if (dto.getNumeroHabitaciones() < 3) {
            throw new RuntimeException("Debe tener al menos 3 habitaciones");
        }

        if (dto.getNumeroBanos() < 2) {
            throw new RuntimeException("Debe tener al menos 2 baños");
        }

        if (dto.getNumeroCocinas() < 1) {
            throw new RuntimeException("Debe tener al menos 1 cocina");
        }

        if (foto == null || foto.isEmpty()) {
            throw new RuntimeException("Debe subir una imagen");
        }

        Casa casa = new Casa();
        casa.setNombre(dto.getNombre());
        casa.setDireccion(dto.getDireccion());
        casa.setPoblacion(dto.getPoblacion());

        // SUBIR IMAGEN
        Map data = imageService.upload(foto);
        String url = (String) data.get("url");

        casa.setFoto(url);

        // PROPIETARIO
        casa.setPropietario(propietario);

        // HABITACIONES
        List<Habitacion> habitaciones = new ArrayList<>();
        for (int i = 0; i < dto.getNumeroHabitaciones(); i++) {
            Habitacion h = new Habitacion();
            h.setCasa(casa);
            h.setCodigoHabitacion("HAB-" + (i + 1));
            h.setNumeroCamas(1);
            h.setTieneBano(true);
            h.setTipoCama(TipoCama.SIMPLE);
            habitaciones.add(h);
        }

        // BAÑOS
        List<Bano> banos = new ArrayList<>();
        for (int i = 0; i < dto.getNumeroBanos(); i++) {
            Bano b = new Bano();
            b.setCasa(casa);
            banos.add(b);
        }

        // COCINAS
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

    public List<Casa> buscarPorPoblacion(String poblacion) {
        return casaRepository.findByPoblacionIgnoreCase(poblacion);
    }

    public List<Casa> findAll() {
        return casaRepository.findAll();
    }
}