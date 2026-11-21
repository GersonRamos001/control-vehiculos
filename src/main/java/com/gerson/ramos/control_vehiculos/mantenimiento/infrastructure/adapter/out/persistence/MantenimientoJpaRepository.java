package com.gerson.ramos.control_vehiculos.mantenimiento.infrastructure.adapter.out.persistence;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MantenimientoJpaRepository extends JpaRepository<MantenimientoJpaEntity, Long> {

	List<MantenimientoJpaEntity> findByVehiculoIdOrderByFechaDescIdDesc(Long vehiculoId);

	Optional<MantenimientoJpaEntity> findFirstByVehiculoIdOrderByFechaDescIdDesc(Long vehiculoId);

	/** Entre dos fechas, ambas inclusive (BETWEEN). */
	List<MantenimientoJpaEntity> findByFechaBetweenOrderByFechaDescIdDesc(LocalDate desde, LocalDate hasta);

	/** El más reciente de cada vehículo: no existe otro del mismo vehículo con mayor fecha (o igual fecha y mayor id). */
	@Query("""
			select m from MantenimientoJpaEntity m
			where not exists (
				select o.id from MantenimientoJpaEntity o
				where o.vehiculoId = m.vehiculoId
				  and (o.fecha > m.fecha or (o.fecha = m.fecha and o.id > m.id)))
			""")
	List<MantenimientoJpaEntity> findUltimoDeCadaVehiculo();
}
