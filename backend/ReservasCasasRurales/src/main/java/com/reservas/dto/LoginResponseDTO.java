package com.reservas.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponseDTO {
    private String token;
    private String nombreCuenta;
    private String rol;
    private String mensaje;

    // Constructor sin rol para compatibilidad (si se necesita)
    public LoginResponseDTO(String token, String nombreCuenta, String mensaje) {
        this.token = token;
        this.nombreCuenta = nombreCuenta;
        this.mensaje = mensaje;
        this.rol = null;
    }
}
