package com.reservas.service;

import com.reservas.model.Propietario;
import com.reservas.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PropietarioService propietarioService;

    public String login(String username, String password) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );

        // BUSCAR USUARIO
        Propietario propietario = propietarioService.buscarPorNombreCuenta(username);

        String rol = propietario.getRol();

        //  USAR MÉTODO CON ROL
        return jwtUtil.generarToken(username, rol);
    }
}
