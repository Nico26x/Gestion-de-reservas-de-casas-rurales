package com.reservas.repository;

import com.reservas.model.Casa;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CasaRepository extends JpaRepository<Casa, Long> {
    List<Casa> findByPoblacionIgnoreCase(String poblacion);

}
