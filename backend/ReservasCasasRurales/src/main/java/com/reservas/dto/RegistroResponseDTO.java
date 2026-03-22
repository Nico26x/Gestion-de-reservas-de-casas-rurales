package com.reservas.dto;

public class RegistroResponseDTO {

    private String mensaje;
    private String nombreCuenta;

    public RegistroResponseDTO(String mensaje, String nombreCuenta) {
        this.mensaje = mensaje;
        this.nombreCuenta = nombreCuenta;
    }

    public String getMensaje() {
        return mensaje;
    }

    public String getNombreCuenta() {
        return nombreCuenta;
    }
}