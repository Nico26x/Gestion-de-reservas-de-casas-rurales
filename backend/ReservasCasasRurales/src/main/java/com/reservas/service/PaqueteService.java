package com.reservas.service;

import com.reservas.dto.PaqueteRequestDTO;
import com.reservas.model.Casa;
import com.reservas.model.Paquete;
import com.reservas.repository.CasaRepository;
import com.reservas.repository.PaqueteRepository;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PaqueteService {

    private final PaqueteRepository paqueteRepository;
    private final CasaRepository casaRepository;

    public PaqueteService(PaqueteRepository paqueteRepository,
                          CasaRepository casaRepository) {
        this.paqueteRepository = paqueteRepository;
        this.casaRepository = casaRepository;
    }

    public Paquete crearPaquete(PaqueteRequestDTO dto, String username) {

    //  VALIDAR FECHAS
    if (dto.getFechaInicio() == null || dto.getFechaFin() == null) {
        throw new RuntimeException("Las fechas son obligatorias");
    }

    if (dto.getFechaInicio().isAfter(dto.getFechaFin())) {
        throw new RuntimeException("La fecha inicio no puede ser mayor que la fecha fin");
    }

    //  VALIDAR PRECIO
    if (dto.getPrecio() == null || dto.getPrecio() <= 0) {
        throw new RuntimeException("El precio debe ser mayor a 0");
    }

    //VALIDAR MODALIDAD
    if (dto.getModalidad() == null) {
        throw new RuntimeException("La modalidad es obligatoria");
    }

    //  BUSCAR CASA
    Casa casa = casaRepository.findById(dto.getCasaId())
            .orElseThrow(() -> new RuntimeException("Casa no encontrada"));

    //  VALIDAR PROPIETARIO 
    if (!casa.getPropietario().getNombreCuenta().equals(username)) {
        throw new RuntimeException("No puedes crear paquetes en casas que no son tuyas");
    }

    //  VALIDAR SOLAPAMIENTOS
    List<Paquete> paquetesSolapados =
            paqueteRepository.findByCasaIdAndFechaInicioLessThanEqualAndFechaFinGreaterThanEqual(
                    dto.getCasaId(),
                    dto.getFechaFin(),
                    dto.getFechaInicio()
            );

    if (!paquetesSolapados.isEmpty()) {
        throw new RuntimeException("Ya existe un paquete en ese rango de fechas");
    }

    //  CREAR
    Paquete paquete = new Paquete();
    paquete.setFechaInicio(dto.getFechaInicio());
    paquete.setFechaFin(dto.getFechaFin());
    paquete.setPrecio(dto.getPrecio());
    paquete.setCasa(casa);
    paquete.setModalidad(dto.getModalidad());

    return paqueteRepository.save(paquete);
}
}