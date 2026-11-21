package com.gerson.ramos.control_vehiculos.configuracion.infrastructure.adapter.out.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.gerson.ramos.control_vehiculos.configuracion.application.port.out.LocalidadRepositoryPort;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.CodigoCatalogo;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.Localidad;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.LocalidadId;
import com.gerson.ramos.control_vehiculos.shared.domain.RecursoNoEncontradoException;

@Repository
@Transactional(readOnly = true)
public class LocalidadPersistenceAdapter implements LocalidadRepositoryPort {

	private final LocalidadJpaRepository repositorio;
	private final LocalidadPersistenceMapper mapper;

	LocalidadPersistenceAdapter(LocalidadJpaRepository repositorio, LocalidadPersistenceMapper mapper) {
		this.repositorio = repositorio;
		this.mapper = mapper;
	}

	@Override
	@Transactional
	public Localidad guardar(Localidad localidad) {
		LocalidadId id = localidad.id();
		LocalidadJpaEntity entidad;
		if (id == null) {
			entidad = mapper.aNuevaEntidad(localidad);
		}
		else {
			entidad = repositorio.findById(id.valor()).orElseThrow(() -> new RecursoNoEncontradoException("Localidad", id.valor()));
			mapper.actualizar(localidad, entidad);
		}
		return mapper.aDominio(repositorio.saveAndFlush(entidad));
	}

	@Override
	public Optional<Localidad> buscarPorId(LocalidadId id) {
		return repositorio.findById(id.valor()).map(mapper::aDominio);
	}

	@Override
	public Optional<Localidad> buscarPorCodigo(CodigoCatalogo codigo) {
		return repositorio.findByCodigo(codigo.valor()).map(mapper::aDominio);
	}

	@Override
	public boolean existePorCodigo(CodigoCatalogo codigo) {
		return repositorio.existsByCodigo(codigo.valor());
	}

	@Override
	public List<Localidad> listarActivas() {
		return repositorio.findByActivoTrueOrderByNombre().stream().map(mapper::aDominio).toList();
	}

	@Override
	public List<Localidad> listarTodas() {
		return repositorio.findAllByOrderByNombre().stream().map(mapper::aDominio).toList();
	}
}
