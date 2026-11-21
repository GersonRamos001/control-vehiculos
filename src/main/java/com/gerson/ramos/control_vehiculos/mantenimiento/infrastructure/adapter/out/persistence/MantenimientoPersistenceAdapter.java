package com.gerson.ramos.control_vehiculos.mantenimiento.infrastructure.adapter.out.persistence;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.gerson.ramos.control_vehiculos.mantenimiento.application.port.out.MantenimientoRepositoryPort;
import com.gerson.ramos.control_vehiculos.mantenimiento.domain.model.Mantenimiento;
import com.gerson.ramos.control_vehiculos.shared.domain.ReglaDeNegocioException;
import com.gerson.ramos.control_vehiculos.vehiculo.domain.model.VehiculoId;

@Repository
@Transactional(readOnly = true)
public class MantenimientoPersistenceAdapter implements MantenimientoRepositoryPort {

	private final MantenimientoJpaRepository repositorio;
	private final MantenimientoPersistenceMapper mapper;

	MantenimientoPersistenceAdapter(MantenimientoJpaRepository repositorio, MantenimientoPersistenceMapper mapper) {
		this.repositorio = repositorio;
		this.mapper = mapper;
	}

	@Override
	@Transactional
	public Mantenimiento guardar(Mantenimiento mantenimiento) {
		if (mantenimiento.id() != null) {
			throw new ReglaDeNegocioException("El historial de mantenimientos es de solo agregar: no se modifican registros");
		}
		return mapper.aDominio(repositorio.saveAndFlush(mapper.aNuevaEntidad(mantenimiento)));
	}

	@Override
	public List<Mantenimiento> historialDe(VehiculoId vehiculoId) {
		return repositorio.findByVehiculoIdOrderByFechaDescIdDesc(vehiculoId.valor()).stream().map(mapper::aDominio).toList();
	}

	@Override
	public Optional<Mantenimiento> ultimoDe(VehiculoId vehiculoId) {
		return repositorio.findFirstByVehiculoIdOrderByFechaDescIdDesc(vehiculoId.valor()).map(mapper::aDominio);
	}

	@Override
	public List<Mantenimiento> ultimosDeTodosLosVehiculos() {
		return repositorio.findUltimoDeCadaVehiculo().stream().map(mapper::aDominio).toList();
	}

	@Override
	public List<Mantenimiento> realizadosEntre(LocalDate desde, LocalDate hasta) {
		return repositorio.findByFechaBetweenOrderByFechaDescIdDesc(desde, hasta).stream().map(mapper::aDominio).toList();
	}
}
