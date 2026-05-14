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

    @ManyToOne
    @JoinColumn(name = "casa_id")
    @JsonIgnore
    private Casa casa;
}
