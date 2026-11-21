package com.gerson.ramos.control_vehiculos.configuracion.infrastructure.adapter.out.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.gerson.ramos.control_vehiculos.configuracion.application.port.out.TipoUsoRepositoryPort;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.CodigoCatalogo;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.TipoUso;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.TipoUsoId;
import com.gerson.ramos.control_vehiculos.shared.domain.RecursoNoEncontradoException;

@Repository
@Transactional(readOnly = true)
public class TipoUsoPersistenceAdapter implements TipoUsoRepositoryPort {

	private final TipoUsoJpaRepository repositorio;
	private final TipoUsoPersistenceMapper mapper;

	TipoUsoPersistenceAdapter(TipoUsoJpaRepository repositorio, TipoUsoPersistenceMapper mapper) {
		this.repositorio = repositorio;
		this.mapper = mapper;
	}

	@Override
	@Transactional
	public TipoUso guardar(TipoUso tipoUso) {
		TipoUsoId id = tipoUso.id();
		TipoUsoJpaEntity entidad;
		if (id == null) {
			entidad = mapper.aNuevaEntidad(tipoUso);
		}
		else {
			entidad = repositorio.findById(id.valor()).orElseThrow(() -> new RecursoNoEncontradoException("Tipo de uso", id.valor()));
			mapper.actualizar(tipoUso, entidad);
		}
		return mapper.aDominio(repositorio.saveAndFlush(entidad));
	}

	@Override
	public Optional<TipoUso> buscarPorId(TipoUsoId id) {
		return repositorio.findById(id.valor()).map(mapper::aDominio);
	}

	@Override
	public Optional<TipoUso> buscarPorCodigo(CodigoCatalogo codigo) {
		return repositorio.findByCodigo(codigo.valor()).map(mapper::aDominio);
	}

	@Override
	public boolean existePorCodigo(CodigoCatalogo codigo) {
		return repositorio.existsByCodigo(codigo.valor());
	}

	@Override
	public List<TipoUso> listarActivos() {
		return repositorio.findByActivoTrueOrderByNombre().stream().map(mapper::aDominio).toList();
	}

	@Override
	public List<TipoUso> listarTodos() {
		return repositorio.findAllByOrderByNombre().stream().map(mapper::aDominio).toList();
	}
}
