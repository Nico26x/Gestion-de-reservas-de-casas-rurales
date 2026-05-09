package com.reservas.repository;

import com.reservas.model.Casa;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;


public interface CasaRepository extends JpaRepository<Casa, Long> {
    
    @EntityGraph(attributePaths = {"habitaciones", "banos", "cocinas", "fotos"})
    List<Casa> findByPoblacionIgnoreCase(String poblacion);

    @EntityGraph(attributePaths = {"habitaciones", "banos", "cocinas", "fotos"})
    List<Casa> findAll();

    //Optional<Casa> findById(Long id);
}
