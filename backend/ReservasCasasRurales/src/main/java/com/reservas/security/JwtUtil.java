package com.reservas.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expirationMs;

    /**
     * En 0.12.6 el tipo retornado es SecretKey (no Key genérico)
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }


    /**
     * Genera un token JWT incluyendo el rol como claim.
     */
    public String generarToken(String nombreCuenta, String rol) {
        Date ahora = new Date();
        Date expiracion = new Date(ahora.getTime() + expirationMs);

        return Jwts.builder()
                .subject(nombreCuenta)
                .claim("role", rol)
                .issuedAt(ahora)
                .expiration(expiracion)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Extrae el rol desde el token.
     */
    public String extraerRol(String token) {
        return extraerClaims(token).get("role", String.class);
    }

    /**
     * Extrae el nombreCuenta (subject) del token.
     */
    public String extraerNombreCuenta(String token) {
        return extraerClaims(token).getSubject();
    }

    /**
     * Valida que el token sea correcto y no esté expirado.
     */
    public boolean validarToken(String token) {
        try {
            extraerClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            System.out.println("JWT expirado: " + e.getMessage());
        } catch (UnsupportedJwtException e) {
            System.out.println("JWT no soportado: " + e.getMessage());
        } catch (MalformedJwtException e) {
            System.out.println("JWT malformado: " + e.getMessage());
        } catch (SignatureException e) {
            System.out.println("Firma JWT inválida: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("JWT vacío o nulo: " + e.getMessage());
        }
        return false;
    }

    /**
     * Parsea el token y retorna el payload (Claims).
     * CAMBIO 0.12.6:
     *   Jwts.parserBuilder()  → Jwts.parser()
     *   .setSigningKey()      → .verifyWith()
     *   .parseClaimsJws()     → .parseSignedClaims()
     *   .getBody()            → .getPayload()
     */
    private Claims extraerClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
