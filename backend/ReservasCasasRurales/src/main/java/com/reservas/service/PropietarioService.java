package com.reservas.service;
import com.reservas.dto.RegistroRequestDTO;
import com.reservas.dto.RegistroResponseDTO;
import com.reservas.model.Propietario;
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

        Propietario propietario = new Propietario();
        propietario.setNombreCuenta(dto.getNombreCuenta());
        propietario.setContrasena(contrasenaEncriptada);

        propietarioRepository.save(propietario);

        return new RegistroResponseDTO("Usuario registrado correctamente", propietario.getNombreCuenta());
    }

    public Propietario buscarPorUsername(String username) {
        return propietarioRepository.findByNombreCuenta(username)
                .orElseThrow(() -> new RuntimeException("Propietario no encontrado"));
    }
}
