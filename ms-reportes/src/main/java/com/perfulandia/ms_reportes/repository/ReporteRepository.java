package com.perfulandia.ms_reportes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.perfulandia.ms_reportes.model.Reporte;

@Repository
public interface ReporteRepository extends JpaRepository<Reporte, Long> {
}
