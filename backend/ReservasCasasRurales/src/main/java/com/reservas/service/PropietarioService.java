package com.reservas.service;
import com.reservas.dto.RegistroRequestDTO;
import com.reservas.dto.RegistroResponseDTO;
import com.reservas.model.Propietario;
import com.reservas.model.Rol;
import com.reservas.repository.PropietarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
public class PropietarioService {

    private final PropietarioRepository propietarioRepository;
    private final PasswordEncoder passwordEncoder;

    public PropietarioService(PropietarioRepository propietarioRepository,
                              PasswordEncoder passwordEncoder) {
        this.propietarioRepository = propietarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public RegistroResponseDTO registrarPropietario(RegistroRequestDTO dto) {

        if (propietarioRepository.existsByNombreCuenta(dto.getNombreCuenta())) {
            throw new RuntimeException("El nombre de usuario ya existe");
        }

        String contrasenaEncriptada = passwordEncoder.encode(dto.getContrasena());

        // Determinar el rol: si viene en el DTO, validar; sino, asignar PROPIETARIO
        Rol rol = Rol.PROPIETARIO; // default
        if (dto.getRol() != null && !dto.getRol().trim().isEmpty()) {
            try {
                rol = Rol.valueOf(dto.getRol().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Rol inválido. Use PROPIETARIO o CLIENTE");
            }
        }

        Propietario propietario = new Propietario();
        propietario.setNombreCuenta(dto.getNombreCuenta());
        propietario.setContrasena(contrasenaEncriptada);
        propietario.setRol(rol);

        

        propietarioRepository.save(propietario);

        return new RegistroResponseDTO("Usuario registrado correctamente", propietario.getNombreCuenta());
    }


    public Propietario buscarPorUsername(String nombreCuenta) {
        return propietarioRepository.findByNombreCuenta(nombreCuenta)
            .orElseThrow(() -> new RuntimeException("Propietario no encontrado"));
    }
}
