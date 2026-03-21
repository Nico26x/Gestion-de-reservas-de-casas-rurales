package com.reservas.dto;

import lombok.Data;

@Data
public class LoginRequestDTO {
    private String nombreCuenta;
    private String contrasena;
}