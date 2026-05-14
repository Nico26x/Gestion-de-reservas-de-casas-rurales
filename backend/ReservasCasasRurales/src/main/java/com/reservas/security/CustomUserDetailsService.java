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

        // Crear la autoridad basada en el rol real del usuario
        String rolName = "ROLE_" + propietario.getRol().name();

        System.out.println("🔥 ENTRÓ A USER DETAILS");
        System.out.println("USER: " + nombreCuenta);
        
        return new User(
                propietario.getNombreCuenta(),
                propietario.getContrasena(),
                List.of(new SimpleGrantedAuthority(rolName))
        );
    }
}
