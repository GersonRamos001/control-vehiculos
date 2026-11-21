package com.gerson.ramos.control_vehiculos.usuarios.infrastructure.adapter.out.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioJpaRepository extends JpaRepository<UsuarioJpaEntity, Long> {

	Optional<UsuarioJpaEntity> findByUsername(String username);

	boolean existsByUsername(String username);

	List<UsuarioJpaEntity> findAllByOrderByUsername();

	long countByRolAndActivoTrue(String rol);
}
