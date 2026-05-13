package com.reservas.service;

//import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.reservas.dto.CasaRequestDTO;
import com.reservas.dto.CasaResponseDTO;
import com.reservas.dto.HabitacionDetalleDTO;
import com.reservas.model.*;
import com.reservas.repository.CasaRepository;

import com.reservas.repository.ReservaRepository;
import org.springframework.transaction.annotation.Transactional;
import java.util.Arrays;



@Service
public class CasaService {

    @Autowired
    private CasaRepository casaRepository;

    @Autowired
    private ImageServiceImpl imageService;

    @Autowired
    private ReservaRepository reservaRepository;

    public Casa crearCasa(CasaRequestDTO dto, List<MultipartFile> fotos, Propietario propietario) throws Exception {

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

        if (fotos == null || fotos.isEmpty()) {
            throw new RuntimeException("Debe subir una imagen");
        }

        if (dto.getNumeroCamas() == null || dto.getNumeroCamas() < 1) {
            throw new RuntimeException("Debe indicar al menos 1 cama por habitación");
        }

        if (dto.getNumeroGarajes() == null || dto.getNumeroGarajes() < 0) {
            throw new RuntimeException("Debe indicar un número válido de garajes");
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
        casa.setNumeroGarajes(dto.getNumeroGarajes());
        Set<CasaFoto> fotosCasa = new HashSet<>();

        for (MultipartFile foto : fotos) {

            Map data = imageService.upload(foto);
            String url = (String) data.get("url");
            CasaFoto casaFoto = new CasaFoto();
            casaFoto.setUrl(url);
            casaFoto.setCasa(casa);
            fotosCasa.add(casaFoto);
        }

        casa.setFotos(fotosCasa);

        casa.setPropietario(propietario);

        Set<Habitacion> habitaciones = new HashSet<>();
        for (int i = 0; i < dto.getNumeroHabitaciones(); i++) {
            Habitacion h = new Habitacion();
            h.setCasa(casa);
            h.setCodigoHabitacion("HAB-" + (i + 1));
            h.setNumeroCamas(dto.getNumeroCamas());
            h.setTieneBano(dto.getTieneBano());
            h.setTipoCama(dto.getTipoCama());
            habitaciones.add(h);
        }

        Set<Bano> banos = new HashSet<>();
        for (int i = 0; i < dto.getNumeroBanos(); i++) {
            Bano b = new Bano();
            b.setCasa(casa);
            banos.add(b);
        }

        Set<Cocina> cocinas = new HashSet<>();
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

    public List<CasaResponseDTO> buscarPorPoblacionConDTO(String poblacion) {
        return casaRepository.findByPoblacionIgnoreCase(poblacion)
                .stream()
                .map(this::convertirACasaResponseDTO)
                .toList();
    }

    public List<Casa> findAll() {
        return casaRepository.findAll();
    }

    public List<CasaResponseDTO> findAllConDTO() {
        return casaRepository.findAll()
                .stream()
                .map(this::convertirACasaResponseDTO)
                .toList();
    }

    public CasaResponseDTO buscarPorId(Long id) {

        Casa casa = casaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Casa no encontrada"));

        return convertirACasaResponseDTO(casa);
    }

    private CasaResponseDTO convertirACasaResponseDTO(Casa casa) {
        CasaResponseDTO dto = new CasaResponseDTO();

        dto.setId(casa.getId());
        dto.setNombre(casa.getNombre());
        dto.setDireccion(casa.getDireccion());
        dto.setPoblacion(casa.getPoblacion());
        dto.setDescripcion(casa.getDescripcion());
        
        // Mapear fotos: extraer URLs de los objetos CasaFoto
        if (casa.getFotos() != null && !casa.getFotos().isEmpty()) {
            dto.setFotos(
                    casa.getFotos()
                            .stream()
                            .map(CasaFoto::getUrl)
                            .filter(url -> url != null && !url.isEmpty())
                            .toList());
        } else {
            dto.setFotos(List.of());
        }
        
        dto.setNumeroComedores(casa.getNumeroComedores());
        dto.setNumeroGarajes(
                casa.getNumeroGarajes() != null ? casa.getNumeroGarajes() : 0);
        dto.setNumeroHabitaciones(
                casa.getHabitaciones() != null ? casa.getHabitaciones().size() : 0);

        dto.setNumeroBanos(
                casa.getBanos() != null ? casa.getBanos().size() : 0);

        dto.setNumeroCocinas(
                casa.getCocinas() != null ? casa.getCocinas().size() : 0);

        // Mapear habitaciones con detalles
        if (casa.getHabitaciones() != null && !casa.getHabitaciones().isEmpty()) {
            dto.setHabitaciones(
                    casa.getHabitaciones()
                            .stream()
                            .map(this::convertirAHabitacionDetalleDTO)
                            .toList());
        } else {
            dto.setHabitaciones(List.of());
        }

        return dto;
    }

    private HabitacionDetalleDTO convertirAHabitacionDetalleDTO(Habitacion habitacion) {
        HabitacionDetalleDTO dto = new HabitacionDetalleDTO();
        dto.setId(habitacion.getId());
        dto.setCodigoHabitacion(habitacion.getCodigoHabitacion());
        dto.setNumeroCamas(habitacion.getNumeroCamas());
        dto.setTipoCama(habitacion.getTipoCama());
        dto.setTieneBano(habitacion.getTieneBano());
        return dto;
    }

    @Transactional
    public void eliminarCasa(Long idCasa, String usernamePropietario) {

    Casa casa = casaRepository.findById(idCasa)
            .orElseThrow(() -> new RuntimeException("Casa no encontrada"));

    if (!casa.getPropietario().getNombreCuenta().equals(usernamePropietario)) {
        throw new RuntimeException("No tiene permiso para eliminar esta casa");
    }

    boolean tieneReservasActivas = !reservaRepository
            .findByCasaIdAndEstadoReservaIn(
                    casa,
                    Arrays.asList(EstadoReserva.PENDIENTE, EstadoReserva.CONFIRMADA)
            )
            .isEmpty();

    if (tieneReservasActivas) {
        throw new RuntimeException("No se puede eliminar la casa porque tiene reservas activas");
    }

    casaRepository.delete(casa);
}
}
