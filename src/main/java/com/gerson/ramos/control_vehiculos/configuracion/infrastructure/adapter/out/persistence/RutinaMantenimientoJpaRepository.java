package com.gerson.ramos.control_vehiculos.configuracion.infrastructure.adapter.out.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RutinaMantenimientoJpaRepository extends JpaRepository<RutinaMantenimientoJpaEntity, Long> {

	Optional<RutinaMantenimientoJpaEntity> findByCodigo(String codigo);

	boolean existsByCodigo(String codigo);

	List<RutinaMantenimientoJpaEntity> findByActivoTrueOrderByCodigo();

	List<RutinaMantenimientoJpaEntity> findAllByOrderByCodigo();
}
