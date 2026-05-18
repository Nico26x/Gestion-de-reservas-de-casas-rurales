package com.reservas.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;   
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Cocina {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "lavavajillas", nullable = false)
    private Boolean lavavajillas = false;

    @Column(name = "lavadora", nullable = false)
    private Boolean lavadora = false;

    @ManyToOne
    @JoinColumn(name = "casa_id")
    @JsonIgnore
    private Casa casa;
}
