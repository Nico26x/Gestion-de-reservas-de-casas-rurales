package com.reservas.dto;

import lombok.*;

@Getter
@Setter
@Data
public class RegistroRequestDTO {

    private String nombreCuenta;
    private String contrasena;
    private String rol;
    private String numeroCuentaBancaria;
}