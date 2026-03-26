package com.reservas.repository;

import com.reservas.model.Casa;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CasaRepository extends JpaRepository<Casa, Long> {
}
