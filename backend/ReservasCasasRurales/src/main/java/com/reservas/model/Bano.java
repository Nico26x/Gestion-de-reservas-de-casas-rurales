package com.reservas.model;
import jakarta.persistence.*;

@Entity
public class Bano {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "casa_id")
    private Casa casa;
}