package com.reservas.config;

import com.reservas.security.CustomUserDetailsService;
import com.reservas.security.JwtFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtFilter jwtFilter;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    /**
     * Cadena de filtros — define rutas públicas vs protegidas con autorización por rol.
     * NOTA Spring Boot 4.x: se usa .csrf(AbstractHttpConfigurer::disable)
     * en lugar de .csrf().disable() que fue removido.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())

                // Spring Boot 4.x: nueva forma de deshabilitar CSRF
                .csrf(AbstractHttpConfigurer::disable)

                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // ── PÚBLICAS ──────────────────────────────
                        .requestMatchers("/api/auth/login").permitAll()
                        .requestMatchers("/api/auth/registro").permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/casas/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/disponibilidad/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/reservas").permitAll()

                        // ── SOLO PROPIETARIO ──────────────────────────────
                        .requestMatchers(HttpMethod.POST, "/api/casas").hasAuthority("ROLE_PROPIETARIO")
                        .requestMatchers(HttpMethod.POST, "/api/paquetes").hasAuthority("ROLE_PROPIETARIO")
                        .requestMatchers(HttpMethod.PUT, "/api/casas/**").hasAuthority("ROLE_PROPIETARIO")
                        .requestMatchers(HttpMethod.DELETE, "/api/casas/**").hasAuthority("ROLE_PROPIETARIO")
                        .requestMatchers(HttpMethod.GET, "/api/paquetes/**").hasAuthority("ROLE_PROPIETARIO")
                        .requestMatchers(HttpMethod.PUT, "/api/paquetes/**").hasAuthority("ROLE_PROPIETARIO")
                        .requestMatchers(HttpMethod.DELETE, "/api/paquetes/**").hasAuthority("ROLE_PROPIETARIO")
                        .requestMatchers(HttpMethod.POST, "/api/disponibilidad").hasAuthority("ROLE_PROPIETARIO")
                        .requestMatchers(HttpMethod.GET, "/api/pagos/pendientes").hasAuthority("ROLE_PROPIETARIO")
                        .requestMatchers(HttpMethod.PUT, "/api/pagos/*/verificar").hasAuthority("ROLE_PROPIETARIO")

                        // ── SOLO CLIENTE ──────────────────────────────
                        .requestMatchers("/api/reservas/**").hasAuthority("ROLE_CLIENTE")
                        .requestMatchers(HttpMethod.POST, "/api/pagos").hasAuthority("ROLE_CLIENTE")

                        // ── TODO LO DEMÁS REQUIERE JWT ──────────────────────────────
                        .anyRequest().authenticated()
                )

                // Sin sesión en servidor — usamos JWT (stateless)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .authenticationProvider(authenticationProvider())

                // Nuestro filtro JWT se ejecuta antes del filtro estándar
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:4200"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * BCrypt para encriptar contraseñas.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Proveedor de autenticación: usa nuestro UserDetailsService + BCrypt.
    */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * AuthenticationManager requerido por AuthController para ejecutar el login.
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}