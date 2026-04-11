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
}
