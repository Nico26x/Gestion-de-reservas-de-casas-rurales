package com.reservas.model;

public enum EstadoPago {
    //El cliente realizó el pago, pero el propietario aún no lo verifica
    PENDIENTE_VERIFICACION,

    //El propietario confirmó que recibió el pago
    VERIFICADO
}
