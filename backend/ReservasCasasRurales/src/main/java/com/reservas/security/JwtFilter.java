package com.reservas.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CustomUserDetailsService userDetailsService;

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

        // 3. Si se extrajo el nombreCuenta y no hay autenticación previa en el contexto
        if (nombreCuenta != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // 4. Cargar el UserDetails desde la base de datos
            UserDetails userDetails = userDetailsService.loadUserByUsername(nombreCuenta);

            // 5. Validar el token
            if (jwtUtil.validarToken(token) && nombreCuenta.equals(userDetails.getUsername())) {
                // 6. Crear el objeto de autenticación
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                // 7. Registrar la autenticación en el contexto de seguridad
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
            System.out.println("AUTH HEADER: " + authHeader);
        }

        // 8. Continuar con el siguiente filtro en la cadena
        filterChain.doFilter(request, response);
    }
    
}
