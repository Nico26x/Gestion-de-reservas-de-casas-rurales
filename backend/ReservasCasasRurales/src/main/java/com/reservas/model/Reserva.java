package com.reservas.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "reserva")
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //Número único generado por el sistema, se muestra al cliente como concepto de pago
    @Column(name = "numero_reserva", nullable = false, unique = true)
    private Long numeroReserva;

    // Fecha en la que se creó la reserva
    @Column(name = "fecha_creacion", nullable = false)
    private LocalDate fechaCreacion;

    //Fecha en que el cliente entra a la casa
    @Column(name = "fecha_entrada", nullable = false)
    private LocalDate fechaEntrada;

    //Número de noches que se desea alquilar
    @Column(name = "numero_noches", nullable = false)
    private Integer numeroNoches;

    //Teléfono de contacto del cliente
    @Column(name = "telefono_cliente", nullable = false)
    private String telefonoCliente;

    //Importe total de la reserva
    @Column(name = "importe", nullable = false)
    private Double importe;

    //20% del importe que el cliente debe consignar antes de 3 días
    @Column(name = "anticipio", nullable = false)
    private Double anticipo;

    //Estado de la reserva
    @Enumerated(EnumType.STRING)
    @JoinColumn(name = "estado_reserva", nullable = false)
    private EstadoReserva estadoReserva;

    //Casa que se está reservando
    @ManyToOne
    @JoinColumn(name = "casa_id", nullable = false)
    private Casa casaId;

    //Habitaciones reservadas (vacío si se reserva casa completa)
    @ManyToMany
    @JoinTable(
            name = "reserva_habitacion",
            joinColumns = @JoinColumn(name = "reserva_id"),
            inverseJoinColumns = @JoinColumn(name = "habitacion_id")
    )
    private List<Habitacion> habitaciones;
}
