package com.reservas.service;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reservas.dto.CasaRequestDTO;
import com.reservas.dto.CasaResponseDTO;
import com.reservas.dto.CocinaRequestDTO;
import com.reservas.dto.CocinaResponseDTO;
import com.reservas.dto.HabitacionDetalleDTO;
import com.reservas.dto.HabitacionRequestDTO;
import com.reservas.model.*;

import com.reservas.repository.CasaRepository;
import com.reservas.repository.ReservaRepository;
import com.reservas.repository.DisponibilidadRepository;
import com.reservas.repository.DisponibilidadHabitacionRepository;
import com.reservas.repository.PaqueteRepository;
import com.reservas.repository.PagoRepository;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CasaService {

    @Autowired
    private CasaRepository casaRepository;

    @Autowired
    private ImageServiceImpl imageService;

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private DisponibilidadRepository disponibilidadRepository;

    @Autowired
    private DisponibilidadHabitacionRepository disponibilidadHabitacionRepository;

    @Autowired
    private PaqueteRepository paqueteRepository;

    @Autowired
    private PagoRepository pagoRepository;

    @Autowired
    private PropietarioService propietarioService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public Casa crearCasa(CasaRequestDTO dto, List<MultipartFile> fotos, Propietario propietario) throws Exception {

        // Validar habitacionesJson si viene
        if (dto.getHabitacionesJson() != null && !dto.getHabitacionesJson().trim().isEmpty()) {
            List<HabitacionRequestDTO> habitacionesFromJson = parseHabitacionesJson(dto.getHabitacionesJson());
            if (habitacionesFromJson.size() < 3) {
                throw new RuntimeException("Debe tener al menos 3 habitaciones");
            }
            // Actualizar numeroHabitaciones basado en el JSON
            dto.setNumeroHabitaciones(habitacionesFromJson.size());
        } else {
            // Validar formato antiguo
            if (dto.getNumeroHabitaciones() < 3) {
                throw new RuntimeException("Debe tener al menos 3 habitaciones");
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
        }

        // Validar cocinasJson si viene
        if (dto.getCocinasJson() != null && !dto.getCocinasJson().trim().isEmpty()) {
            List<CocinaRequestDTO> cocinasFromJson = parseCocinasJson(dto.getCocinasJson());
            if (cocinasFromJson.isEmpty()) {
                throw new RuntimeException("Debe tener al menos 1 cocina");
            }
            // Actualizar numeroCocinas basado en el JSON
            dto.setNumeroCocinas(cocinasFromJson.size());
        } else {
            // Validar formato antiguo
            if (dto.getNumeroCocinas() < 1) {
                throw new RuntimeException("Debe tener al menos 1 cocina");
            }
        }

        if (dto.getNumeroBanos() < 2) {
            throw new RuntimeException("Debe tener al menos 2 baños");
        }

        if (dto.getNumeroComedores() == null || dto.getNumeroComedores() < 1) {
            throw new RuntimeException("Debe tener al menos 1 comedor");
        }

        if (fotos == null || fotos.isEmpty()) {
            throw new RuntimeException("Debe subir una imagen");
        }

        if (dto.getNumeroGarajes() == null || dto.getNumeroGarajes() < 0) {
            throw new RuntimeException("Debe indicar un número válido de garajes");
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

        // Crear habitaciones: usar JSON si viene, sino usar formato antiguo
        Set<Habitacion> habitaciones = new HashSet<>();
        if (dto.getHabitacionesJson() != null && !dto.getHabitacionesJson().trim().isEmpty()) {
            List<HabitacionRequestDTO> habitacionesFromJson = parseHabitacionesJson(dto.getHabitacionesJson());
            int index = 1;
            for (HabitacionRequestDTO habitacionDto : habitacionesFromJson) {
                Habitacion h = new Habitacion();
                h.setCasa(casa);
                h.setCodigoHabitacion("HAB-" + index);
                h.setNumeroCamas(habitacionDto.getNumeroCamas());
                h.setTieneBano(habitacionDto.getTieneBano() != null ? habitacionDto.getTieneBano() : false);
                h.setTipoCama(habitacionDto.getTipoCama());
                habitaciones.add(h);
                index++;
            }
        } else {
            // Formato antiguo: todas las habitaciones iguales
            for (int i = 0; i < dto.getNumeroHabitaciones(); i++) {
                Habitacion h = new Habitacion();
                h.setCasa(casa);
                h.setCodigoHabitacion("HAB-" + (i + 1));
                h.setNumeroCamas(dto.getNumeroCamas());
                h.setTieneBano(dto.getTieneBano());
                h.setTipoCama(dto.getTipoCama());
                habitaciones.add(h);
            }
        }

        Set<Bano> banos = new HashSet<>();
        for (int i = 0; i < dto.getNumeroBanos(); i++) {
            Bano b = new Bano();
            b.setCasa(casa);
            banos.add(b);
        }

        // Crear cocinas: usar JSON si viene, sino usar formato antiguo
        Set<Cocina> cocinas = new HashSet<>();
        if (dto.getCocinasJson() != null && !dto.getCocinasJson().trim().isEmpty()) {
            List<CocinaRequestDTO> cocinasFromJson = parseCocinasJson(dto.getCocinasJson());
            for (CocinaRequestDTO cocinaDto : cocinasFromJson) {
                Cocina c = new Cocina();
                c.setCasa(casa);
                c.setLavavajillas(cocinaDto.getLavavajillas() != null ? cocinaDto.getLavavajillas() : false);
                c.setLavadora(cocinaDto.getLavadora() != null ? cocinaDto.getLavadora() : false);
                cocinas.add(c);
            }
        } else {
            // Formato antiguo: cocinas sin propiedades (default false)
            for (int i = 0; i < dto.getNumeroCocinas(); i++) {
                Cocina c = new Cocina();
                c.setCasa(casa);
                c.setLavavajillas(false);
                c.setLavadora(false);
                cocinas.add(c);
            }
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
        dto.setNumeroGarajes(casa.getNumeroGarajes() != null ? casa.getNumeroGarajes() : 0);
        dto.setNumeroHabitaciones(casa.getHabitaciones() != null ? casa.getHabitaciones().size() : 0);
        dto.setNumeroBanos(casa.getBanos() != null ? casa.getBanos().size() : 0);
        dto.setNumeroCocinas(casa.getCocinas() != null ? casa.getCocinas().size() : 0);

        // Mapear habitaciones con detalles
        if (casa.getHabitaciones() != null && !casa.getHabitaciones().isEmpty()) {
            dto.setHabitaciones(
                    casa.getHabitaciones()
                            .stream()
                            .sorted(
                                Comparator
                                    .comparingInt(this::obtenerNumeroHabitacion)
                                    .thenComparing(Habitacion::getId)
                            )
                            .map(this::convertirAHabitacionDetalleDTO)
                            .toList());
        } else {
            dto.setHabitaciones(List.of());
        }

        // Mapear cocinas con detalles
        if (casa.getCocinas() != null && !casa.getCocinas().isEmpty()) {
            dto.setCocinas(
                    casa.getCocinas()
                            .stream()
                            .map(this::convertirACocinaResponseDTO)
                            .toList());
        } else {
            dto.setCocinas(List.of());
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

    private int obtenerNumeroHabitacion(Habitacion habitacion) {
        if (habitacion.getCodigoHabitacion() == null) {
            return Integer.MAX_VALUE;
        }

        try {
            return Integer.parseInt(
                habitacion.getCodigoHabitacion().replaceAll("\\D+", "")
            );
        } catch (NumberFormatException e) {
            return Integer.MAX_VALUE;
        }
    }

    @Transactional
    public void eliminarCasa(Long idCasa, String usernamePropietario) {

        Casa casa = casaRepository.findById(idCasa)
                .orElseThrow(() -> new RuntimeException("Casa no encontrada"));

        if (!casa.getPropietario().getNombreCuenta().equals(usernamePropietario)) {
            throw new RuntimeException("No tiene permiso para eliminar esta casa");
        }

        // Validar que NO existan reservas activas (PENDIENTE o CONFIRMADA)
        boolean tieneReservasActivas = !reservaRepository
                .findByCasaIdAndEstadoReservaIn(
                        casa,
                        Arrays.asList(EstadoReserva.PENDIENTE, EstadoReserva.CONFIRMADA)
                )
                .isEmpty();

        if (tieneReservasActivas) {
            throw new RuntimeException("No se puede eliminar la casa porque tiene reservas activas");
        }

        // Limpiar dependencias en orden correcto para evitar FK constraints
        
        // 1. Eliminar pagos de reservas CANCELADAS
        pagoRepository.deleteByReservasCanceladasDeCasa(idCasa);

        // 2. Limpiar tabla intermedia reserva_habitacion de reservas CANCELADAS
        reservaRepository.deleteHabitacionesDeReservasCanceladasPorCasa(idCasa);

        // 3. Eliminar reservas CANCELADAS
        reservaRepository.deleteCanceladasByCasaId(idCasa);

        // 4. Eliminar DisponibilidadHabitacion asociada a la casa
        disponibilidadHabitacionRepository.deleteByCasaId(idCasa);

        // 5. Eliminar Disponibilidad de la casa
        disponibilidadRepository.deleteByCasaId(idCasa);

        // 6. Eliminar Paquete de la casa
        paqueteRepository.deleteByCasaId(idCasa);

        // 7. Finalmente, eliminar la casa
        // Las relaciones cascada (Habitacion, Bano, Cocina, CasaFoto) se eliminarán automáticamente
        casaRepository.delete(casa);
    }

    //Modificar casa
    @Transactional
    public CasaResponseDTO modificarCasa(Long casaId, CasaRequestDTO dto, List<MultipartFile> fotos, String usernamePropietario) throws Exception {

        //Buscar la casa
        Casa casa = casaRepository.findById(casaId)
                .orElseThrow(() -> new RuntimeException("Casa no encontrada"));

        //Validar que el propietario autenticado sea dueño de la casa
        if (!casa.getPropietario().getNombreCuenta().equals(usernamePropietario)) {
            throw new RuntimeException("No tienes permiso para modificar esta casa");
        }

        // Validar habitacionesJson si viene
        if (dto.getHabitacionesJson() != null && !dto.getHabitacionesJson().trim().isEmpty()) {
            List<HabitacionRequestDTO> habitacionesFromJson = parseHabitacionesJson(dto.getHabitacionesJson());
            if (habitacionesFromJson.size() < 3) {
                throw new RuntimeException("Debe tener al menos 3 habitaciones");
            }
            dto.setNumeroHabitaciones(habitacionesFromJson.size());
        } else {
            // Validar formato antiguo
            if (dto.getNumeroHabitaciones() < 3) {
                throw new RuntimeException("Debe tener al menos 3 habitaciones");
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
        }

        // Validar cocinasJson si viene
        if (dto.getCocinasJson() != null && !dto.getCocinasJson().trim().isEmpty()) {
            List<CocinaRequestDTO> cocinasFromJson = parseCocinasJson(dto.getCocinasJson());
            if (cocinasFromJson.isEmpty()) {
                throw new RuntimeException("Debe tener al menos 1 cocina");
            }
            dto.setNumeroCocinas(cocinasFromJson.size());
        } else {
            // Validar formato antiguo
            if (dto.getNumeroCocinas() < 1) {
                throw new RuntimeException("Debe tener al menos 1 cocina");
            }
        }

        if (dto.getNumeroBanos() < 2) {
            throw new RuntimeException("Debe tener al menos 2 baños");
        }

        if (dto.getNumeroComedores() == null || dto.getNumeroComedores() < 1) {
            throw new RuntimeException("Debe tener al menos 1 comedor");
        }

        if (dto.getNumeroGarajes() == null || dto.getNumeroGarajes() < 0) {
            throw new RuntimeException("Debe indicar un número válido de garajes");
        }

        //Validar que no se reduzcan habitaciones si hay reservas activas por habitación
        boolean hayReservasActivas = !reservaRepository
                .findByCasaIdAndEstadoReservaIn(
                        casa,
                        Arrays.asList(EstadoReserva.PENDIENTE, EstadoReserva.CONFIRMADA)
                ).isEmpty();

        int habitacionesActuales = casa.getHabitaciones() != null ? casa.getHabitaciones().size() : 0;

        if (hayReservasActivas && dto.getNumeroHabitaciones() < habitacionesActuales) {
            throw new RuntimeException(
                    "No se puede reducir el número de habitaciones: existen reservas activas en esta casa");
        }

        //Actualizar datos básicos de la casa
        if (dto.getNombre() != null && !dto.getNombre().trim().isEmpty()) {
            casa.setNombre(dto.getNombre());
        }
        if (dto.getDireccion() != null && !dto.getDireccion().trim().isEmpty()) {
            casa.setDireccion(dto.getDireccion());
        }
        if (dto.getPoblacion() != null && !dto.getPoblacion().trim().isEmpty()) {
            casa.setPoblacion(dto.getPoblacion());
        }
        if (dto.getDescripcion() != null) {
            casa.setDescripcion(dto.getDescripcion());
        }
        casa.setNumeroComedores(dto.getNumeroComedores());
        casa.setNumeroGarajes(dto.getNumeroGarajes());

        // Actualizar habitaciones: usar JSON si viene, sino usar formato antiguo
        if (dto.getHabitacionesJson() != null && !dto.getHabitacionesJson().trim().isEmpty()) {
            List<HabitacionRequestDTO> habitacionesFromJson = parseHabitacionesJson(dto.getHabitacionesJson());
            casa.getHabitaciones().clear();
            int index = 1;
            for (HabitacionRequestDTO habitacionDto : habitacionesFromJson) {
                Habitacion h = new Habitacion();
                h.setCasa(casa);
                h.setCodigoHabitacion("HAB-" + index);
                h.setNumeroCamas(habitacionDto.getNumeroCamas());
                h.setTieneBano(habitacionDto.getTieneBano() != null ? habitacionDto.getTieneBano() : false);
                h.setTipoCama(habitacionDto.getTipoCama());
                casa.getHabitaciones().add(h);
                index++;
            }
        } else {
            // Formato antiguo: actualizar habitaciones
            if (dto.getNumeroHabitaciones() > habitacionesActuales) {
                int habitacionesAAgregar = dto.getNumeroHabitaciones() - habitacionesActuales;
                for (int i = 0; i < habitacionesAAgregar; i++) {
                    Habitacion h = new Habitacion();
                    h.setCasa(casa);
                    h.setCodigoHabitacion("HAB-" + (habitacionesActuales + i + 1));
                    h.setNumeroCamas(dto.getNumeroCamas());
                    h.setTieneBano(dto.getTieneBano());
                    h.setTipoCama(dto.getTipoCama());
                    casa.getHabitaciones().add(h);
                }
            } else if (dto.getNumeroHabitaciones() < habitacionesActuales && !hayReservasActivas) {
                int habitacionesAEliminar = habitacionesActuales - dto.getNumeroHabitaciones();
                List<Habitacion> listaHabitaciones = casa.getHabitaciones()
                        .stream()
                        .sorted((a, b) -> b.getId().compareTo(a.getId()))
                        .toList();

                for (int i = 0; i < habitacionesAEliminar; i++) {
                    casa.getHabitaciones().remove(listaHabitaciones.get(i));
                }
            }
            if (dto.getNumeroCamas() != null || dto.getTipoCama() != null || dto.getTieneBano() != null) {
                casa.getHabitaciones().forEach(h -> {
                    if (dto.getNumeroCamas() != null) h.setNumeroCamas(dto.getNumeroCamas());
                    if (dto.getTipoCama() != null) h.setTipoCama(dto.getTipoCama());
                    if (dto.getTieneBano() != null) h.setTieneBano(dto.getTieneBano());
                });
            }
        }

        //Actualizar baños
        casa.getBanos().clear();
        for (int i = 0; i < dto.getNumeroBanos(); i++) {
            Bano b = new Bano();
            b.setCasa(casa);
            casa.getBanos().add(b);
        }

        //Actualizar cocinas: usar JSON si viene, sino usar formato antiguo
        casa.getCocinas().clear();
        if (dto.getCocinasJson() != null && !dto.getCocinasJson().trim().isEmpty()) {
            List<CocinaRequestDTO> cocinasFromJson = parseCocinasJson(dto.getCocinasJson());
            for (CocinaRequestDTO cocinaDto : cocinasFromJson) {
                Cocina c = new Cocina();
                c.setCasa(casa);
                c.setLavavajillas(cocinaDto.getLavavajillas() != null ? cocinaDto.getLavavajillas() : false);
                c.setLavadora(cocinaDto.getLavadora() != null ? cocinaDto.getLavadora() : false);
                casa.getCocinas().add(c);
            }
        } else {
            // Formato antiguo: cocinas sin propiedades (default false)
            for (int i = 0; i < dto.getNumeroCocinas(); i++) {
                Cocina c = new Cocina();
                c.setCasa(casa);
                c.setLavavajillas(false);
                c.setLavadora(false);
                casa.getCocinas().add(c);
            }
        }

        //Actualizar fotos solo si se envían nuevas, si no se envían se mantienen las actuales
        if (fotos != null && !fotos.isEmpty()) {
            casa.getFotos().clear();
            for (MultipartFile foto : fotos) {
                Map data = imageService.upload(foto);
                String url = (String) data.get("url");
                CasaFoto casaFoto = new CasaFoto();
                casaFoto.setUrl(url);
                casaFoto.setCasa(casa);
                casa.getFotos().add(casaFoto);
            }
        }

        Casa casaGuardada = casaRepository.save(casa);
        return convertirACasaResponseDTO(casaGuardada);
    }

    public List<CasaResponseDTO> listarCasasDelPropietarioAutenticado(String username) {
        Propietario propietario = propietarioService.buscarPorUsername(username);
        List<Casa> casas = casaRepository.findByPropietario(propietario);
        return casas.stream()
                .map(this::convertirACasaResponseDTO)
                .toList();
    }

    private CocinaResponseDTO convertirACocinaResponseDTO(Cocina cocina) {
        CocinaResponseDTO dto = new CocinaResponseDTO();
        dto.setId(cocina.getId());
        dto.setLavavajillas(cocina.getLavavajillas() != null ? cocina.getLavavajillas() : false);
        dto.setLavadora(cocina.getLavadora() != null ? cocina.getLavadora() : false);
        return dto;
    }

    private List<HabitacionRequestDTO> parseHabitacionesJson(String json) throws Exception {
        if (json == null || json.trim().isEmpty()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, objectMapper.getTypeFactory()
                    .constructCollectionType(List.class, HabitacionRequestDTO.class));
        } catch (Exception e) {
            throw new RuntimeException("Error al parsear habitacionesJson: " + e.getMessage());
        }
    }

    private List<CocinaRequestDTO> parseCocinasJson(String json) throws Exception {
        if (json == null || json.trim().isEmpty()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, objectMapper.getTypeFactory()
                    .constructCollectionType(List.class, CocinaRequestDTO.class));
        } catch (Exception e) {
            throw new RuntimeException("Error al parsear cocinasJson: " + e.getMessage());
        }
    }
}
