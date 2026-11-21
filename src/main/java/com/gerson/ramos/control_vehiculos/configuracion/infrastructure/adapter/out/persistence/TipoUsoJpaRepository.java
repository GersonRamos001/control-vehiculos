package com.gerson.ramos.control_vehiculos.configuracion.infrastructure.adapter.out.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TipoUsoJpaRepository extends JpaRepository<TipoUsoJpaEntity, Long> {

	Optional<TipoUsoJpaEntity> findByCodigo(String codigo);

	boolean existsByCodigo(String codigo);

	List<TipoUsoJpaEntity> findByActivoTrueOrderByNombre();

	List<TipoUsoJpaEntity> findAllByOrderByNombre();
}
