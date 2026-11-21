package com.gerson.ramos.control_vehiculos.configuracion.infrastructure.adapter.out.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.gerson.ramos.control_vehiculos.configuracion.application.port.out.RutinaMantenimientoRepositoryPort;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.CodigoCatalogo;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.RutinaMantenimiento;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.RutinaMantenimientoId;
import com.gerson.ramos.control_vehiculos.shared.domain.RecursoNoEncontradoException;

@Repository
@Transactional(readOnly = true)
public class RutinaMantenimientoPersistenceAdapter implements RutinaMantenimientoRepositoryPort {

	private final RutinaMantenimientoJpaRepository repositorio;
	private final RutinaMantenimientoPersistenceMapper mapper;

	RutinaMantenimientoPersistenceAdapter(RutinaMantenimientoJpaRepository repositorio,
			RutinaMantenimientoPersistenceMapper mapper) {
		this.repositorio = repositorio;
		this.mapper = mapper;
	}

	@Override
	@Transactional
	public RutinaMantenimiento guardar(RutinaMantenimiento rutina) {
		RutinaMantenimientoId id = rutina.id();
		RutinaMantenimientoJpaEntity entidad;
		if (id == null) {
			entidad = mapper.aNuevaEntidad(rutina);
		}
		else {
			entidad = repositorio.findById(id.valor())
				.orElseThrow(() -> new RecursoNoEncontradoException("Rutina de mantenimiento", id.valor()));
			mapper.actualizar(rutina, entidad);
		}
		return mapper.aDominio(repositorio.saveAndFlush(entidad));
	}

	@Override
	public Optional<RutinaMantenimiento> buscarPorId(RutinaMantenimientoId id) {
		return repositorio.findById(id.valor()).map(mapper::aDominio);
	}

	@Override
	public Optional<RutinaMantenimiento> buscarPorCodigo(CodigoCatalogo codigo) {
		return repositorio.findByCodigo(codigo.valor()).map(mapper::aDominio);
	}

	@Override
	public boolean existePorCodigo(CodigoCatalogo codigo) {
		return repositorio.existsByCodigo(codigo.valor());
	}

	@Override
	public List<RutinaMantenimiento> listarActivas() {
		return repositorio.findByActivoTrueOrderByCodigo().stream().map(mapper::aDominio).toList();
	}

	@Override
	public List<RutinaMantenimiento> listarTodas() {
		return repositorio.findAllByOrderByCodigo().stream().map(mapper::aDominio).toList();
	}
}
