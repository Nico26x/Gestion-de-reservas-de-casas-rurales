package com.reservas.controller;

import com.reservas.dto.LoginRequestDTO;
import com.reservas.dto.LoginResponseDTO;
import com.reservas.dto.RegistroRequestDTO;
import com.reservas.dto.RegistroResponseDTO;
import com.reservas.model.Propietario;
import com.reservas.security.JwtUtil;
import com.reservas.service.PropietarioService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PropietarioService propietarioService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getNombreCuenta(),
                            request.getContrasena()));

            String nombreCuenta = authentication.getName();
            
            // Obtener el propietario para extraer el rol
            Propietario propietario = propietarioService.buscarPorUsername(nombreCuenta);

            String rolNombre = "ROLE_" + propietario.getRol().name();
            
            // Generar token incluyendo el rol
            String token = jwtUtil.generarToken(nombreCuenta, rolNombre);

            return ResponseEntity.ok(
                    new LoginResponseDTO(token, nombreCuenta, rolNombre, "Login exitoso"));

        } catch (BadCredentialsException e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new LoginResponseDTO(null, null, null, "Credenciales incorrectas"));
        }
    }

    @PostMapping("/registro")
    public ResponseEntity<RegistroResponseDTO> registrar(@RequestBody RegistroRequestDTO dto) {
        try {
            RegistroResponseDTO respuesta = propietarioService.registrarPropietario(dto);
            return ResponseEntity.ok(respuesta);
        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new RegistroResponseDTO(e.getMessage(), null));
        }
    }
}