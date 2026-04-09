package com.reservas.security;

import com.reservas.model.Propietario;
import com.reservas.repository.PropietarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.List;


@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private PropietarioRepository propietarioRepository;

    /**
     * Spring Security llama a este método automáticamente durante la autenticación.
     * Busca al propietario por su nombreCuenta y lo convierte en un UserDetails.
     */
    @Override
    public UserDetails loadUserByUsername(String nombreCuenta) throws UsernameNotFoundException {

        Propietario propietario = propietarioRepository
                .findByNombreCuenta(nombreCuenta)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Propietario no encontrado con nombre de cuenta: " + nombreCuenta
                ));

        // Construimos el UserDetails con el nombreCuenta y la contraseña encriptada
        // Collections.emptyList() = sin roles por ahora (solo hay un tipo de usuario: propietario)
        return new User(
                propietario.getNombreCuenta(),
                propietario.getContrasena(),
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }
}
