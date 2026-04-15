package com.reservas.model;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Habitacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //Código que identifica la habitación dentro de la casa, "HAB-1", "HAB-2"
    @Column(name = "codigo_habitacion", nullable = false)
    private String codigoHabitacion;

    @Column(name = "numero_camas", nullable = false)
    private Integer numeroCamas;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_cama", nullable = false)
    private TipoCama tipoCama;

    @Column(name = "tiene_bano", nullable = false)
    private Boolean tieneBano;

    @ManyToOne
    @JoinColumn(name = "casa_id")
    @JsonIgnore
    private Casa casa;
}
