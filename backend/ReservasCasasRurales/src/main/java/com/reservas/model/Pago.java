package com.reservas.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "pago")
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //Fecha en la que el cliente realiza el pago
    @Column(name = "fecha_pago", nullable = false)
    private LocalDate fechaPago;

    //Cantidad de dinero que el cliente consignó, minímo el 20% del total
    @Column(name = "monto", nullable = false)
    private Double monto;

    //Método con el que se realizó el pago
    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_pago", nullable = false)
    private MetodoPago metodoPago;

    //Estado del pago, cuando el cliente registra el pago esta PENDIENTE_VERIFICACIÓN y cuando el propietario confirma pasa a VERIFICADO
    @Enumerated(EnumType.STRING)
    @Column(name = "estado_pago", nullable = false)
    private EstadoPago estadoPago;

    //Una reserva puede tener varios pagos
    @ManyToOne
    @JoinColumn(name = "reserva_id", nullable = false)
    private Reserva reserva;
}
