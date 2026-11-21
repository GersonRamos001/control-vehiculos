package com.gerson.ramos.control_vehiculos.vehiculo.infrastructure.adapter.out.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.gerson.ramos.control_vehiculos.shared.domain.RecursoNoEncontradoException;
import com.gerson.ramos.control_vehiculos.vehiculo.application.port.out.VehiculoRepositoryPort;
import com.gerson.ramos.control_vehiculos.vehiculo.domain.model.CodigoEquipo;
import com.gerson.ramos.control_vehiculos.vehiculo.domain.model.Placa;
import com.gerson.ramos.control_vehiculos.vehiculo.domain.model.Vehiculo;
import com.gerson.ramos.control_vehiculos.vehiculo.domain.model.VehiculoId;

@Repository
@Transactional(readOnly = true)
public class VehiculoPersistenceAdapter implements VehiculoRepositoryPort {

	private final VehiculoJpaRepository repositorio;
	private final VehiculoPersistenceMapper mapper;

	VehiculoPersistenceAdapter(VehiculoJpaRepository repositorio, VehiculoPersistenceMapper mapper) {
		this.repositorio = repositorio;
		this.mapper = mapper;
	}

	@Override
	@Transactional
	public Vehiculo guardar(Vehiculo vehiculo) {
		VehiculoId id = vehiculo.id();
		VehiculoJpaEntity entidad;
		if (id == null) {
			entidad = mapper.aNuevaEntidad(vehiculo);
		}
		else {
			entidad = repositorio.findById(id.valor()).orElseThrow(() -> new RecursoNoEncontradoException("Vehículo", id.valor()));
			Long versionLeida = vehiculo.version();
			if (versionLeida != null && !versionLeida.equals(entidad.getVersion())) {
				// El vehículo fue modificado por otra operación desde que se leyó.
				throw new ObjectOptimisticLockingFailureException(VehiculoJpaEntity.class, id.valor());
			}
			mapper.actualizar(vehiculo, entidad);
		}
		// saveAndFlush: asegura que las restricciones únicas fallen aquí y que la versión devuelta sea la vigente.
		return mapper.aDominio(repositorio.saveAndFlush(entidad));
	}

	@Override
	public Optional<Vehiculo> buscarPorId(VehiculoId id) {
		return repositorio.findById(id.valor()).map(mapper::aDominio);
	}

	@Override
	public Optional<Vehiculo> buscarPorPlaca(Placa placa) {
		return repositorio.findByPlaca(placa.valor()).map(mapper::aDominio);
	}

	@Override
	public boolean existePorPlaca(Placa placa) {
		return repositorio.existsByPlaca(placa.valor());
	}

	@Override
	public boolean existePorCodigoEquipo(CodigoEquipo codigoEquipo) {
		return repositorio.existsByCodigoEquipo(codigoEquipo.valor());
	}

	@Override
	public List<Vehiculo> listarActivos() {
		return repositorio.findByActivoTrueOrderByPlaca().stream().map(mapper::aDominio).toList();
	}

	@Override
	public List<Vehiculo> listarTodos() {
		return repositorio.findAllByOrderByPlaca().stream().map(mapper::aDominio).toList();
	}
}
