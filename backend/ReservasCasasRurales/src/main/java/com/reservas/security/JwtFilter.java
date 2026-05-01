package com.reservas.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Este filtro se ejecuta en CADA request que llega al servidor.
     * Si el request trae un token JWT válido en el header Authorization,
     * autentica al propietario en el contexto de seguridad de Spring.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        System.out.println("JWT FILTER EJECUTADO");
        // 1. Extraer el header Authorization del request
        String authHeader = request.getHeader("Authorization");

        String token = null;
        String nombreCuenta = null;

        // 2. Verificar que el header exista y empiece con "Bearer "
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7); // Quitar "Bearer " y quedarse con el token
            try {
                nombreCuenta = jwtUtil.extraerNombreCuenta(token);
            } catch (Exception e) {
                // Token inválido, no se autentica pero se deja continuar el filtro
                // Spring Security rechazará el acceso a rutas protegidas
            }
        }

        // 3. Si se extrajo el nombreCuenta y no hay autenticación previa
        if (nombreCuenta != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            if (jwtUtil.validarToken(token)) {

                String rol = jwtUtil.extraerRol(token);
                
                // Diagnóstico
                System.out.println("[JWT FILTER DIAGNOSTICS]");
                System.out.println("  URI solicitada: " + request.getRequestURI());
                System.out.println("  Método HTTP: " + request.getMethod());
                System.out.println("  Username: " + nombreCuenta);
                System.out.println("  Rol extraído del token: " + rol);
                
                // Asegurar que el rol tenga el prefijo "ROLE_"
                if (rol != null && !rol.startsWith("ROLE_")) {
                    rol = "ROLE_" + rol;
                }
                
                System.out.println("  Rol con prefijo: " + rol);

                List<SimpleGrantedAuthority> authorities =
                        List.of(new SimpleGrantedAuthority(rol));
                
                System.out.println("  Authorities creadas: " + authorities);

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                nombreCuenta,
                                null,
                                authorities
                        );

                SecurityContextHolder.getContext().setAuthentication(authToken);
                System.out.println("  ✓ Authentication establecida en SecurityContext");
            }
        }

        // 8. Continuar con el siguiente filtro en la cadena
        filterChain.doFilter(request, response);
    }

}