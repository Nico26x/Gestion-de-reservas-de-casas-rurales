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

        if (dto.getNumeroHabitaciones() < 3) {
            throw new RuntimeException("Debe tener al menos 3 habitaciones");
        }

        if (dto.getNumeroBanos() < 2) {
            throw new RuntimeException("Debe tener al menos 2 baños");
        }

        if (dto.getNumeroCocinas() < 1) {
            throw new RuntimeException("Debe tener al menos 1 cocina");
        }

        if (dto.getNumeroComedores() == null || dto.getNumeroComedores() < 1) {
            throw new RuntimeException("Debe tener al menos 1 comedor");
        }

        if (foto == null || foto.isEmpty()) {
            throw new RuntimeException("Debe subir una imagen");
        }

        if (dto.getNumeroCamas() == null || dto.getNumeroCamas() < 1) {
            throw new RuntimeException("Debe indicar al menos 1 cama por habitación");
        }

        if (dto.getTieneBano() == null) {
            throw new RuntimeException("Debe indicar si la habitación tiene baño");
        }

        if (dto.getTipoCama() == null) {
            throw new RuntimeException("Debe indicar el tipo de cama");
        }

        Casa casa = new Casa();
        casa.setNombre(dto.getNombre());
        casa.setDireccion(dto.getDireccion());
        casa.setPoblacion(dto.getPoblacion());
        casa.setDescripcion(dto.getDescripcion());
        casa.setNumeroComedores(dto.getNumeroComedores());

        Map data = imageService.upload(foto);
        String url = (String) data.get("url");
        casa.setFoto(url);

        casa.setPropietario(propietario);

        List<Habitacion> habitaciones = new ArrayList<>();
        for (int i = 0; i < dto.getNumeroHabitaciones(); i++) {
            Habitacion h = new Habitacion();
            h.setCasa(casa);
            h.setCodigoHabitacion("HAB-" + (i + 1));
            h.setNumeroCamas(dto.getNumeroCamas());
            h.setTieneBano(dto.getTieneBano());
            h.setTipoCama(dto.getTipoCama());
            habitaciones.add(h);
        }

        List<Bano> banos = new ArrayList<>();
        for (int i = 0; i < dto.getNumeroBanos(); i++) {
            Bano b = new Bano();
            b.setCasa(casa);
            banos.add(b);
        }

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