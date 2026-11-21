package com.gerson.ramos.control_vehiculos.configuracion.infrastructure.adapter.out.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface LocalidadJpaRepository extends JpaRepository<LocalidadJpaEntity, Long> {

	Optional<LocalidadJpaEntity> findByCodigo(String codigo);

	boolean existsByCodigo(String codigo);

	List<LocalidadJpaEntity> findByActivoTrueOrderByNombre();

	List<LocalidadJpaEntity> findAllByOrderByNombre();
}
