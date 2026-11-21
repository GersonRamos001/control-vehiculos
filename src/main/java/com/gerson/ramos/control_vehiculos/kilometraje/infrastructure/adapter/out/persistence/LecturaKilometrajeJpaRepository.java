package com.gerson.ramos.control_vehiculos.kilometraje.infrastructure.adapter.out.persistence;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface LecturaKilometrajeJpaRepository extends JpaRepository<LecturaKilometrajeJpaEntity, Long> {

	Optional<LecturaKilometrajeJpaEntity> findByVehiculoIdAndFecha(Long vehiculoId, LocalDate fecha);

	List<LecturaKilometrajeJpaEntity> findByVehiculoIdOrderByFechaDesc(Long vehiculoId);

	Optional<LecturaKilometrajeJpaEntity> findFirstByVehiculoIdOrderByFechaDesc(Long vehiculoId);

	/** La más reciente de cada vehículo (fecha única por vehículo, así que no hay empates). */
	@Query("""
			select l from LecturaKilometrajeJpaEntity l
			where not exists (
				select o.id from LecturaKilometrajeJpaEntity o
				where o.vehiculoId = l.vehiculoId and o.fecha > l.fecha)
			""")
	List<LecturaKilometrajeJpaEntity> findUltimaDeCadaVehiculo();
}
