package com.gerson.ramos.control_vehiculos.vehiculo.infrastructure.adapter.out.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface VehiculoJpaRepository extends JpaRepository<VehiculoJpaEntity, Long> {

	Optional<VehiculoJpaEntity> findByPlaca(String placa);

	boolean existsByPlaca(String placa);

	boolean existsByCodigoEquipo(String codigoEquipo);

	List<VehiculoJpaEntity> findByActivoTrueOrderByPlaca();

	List<VehiculoJpaEntity> findAllByOrderByPlaca();
}
