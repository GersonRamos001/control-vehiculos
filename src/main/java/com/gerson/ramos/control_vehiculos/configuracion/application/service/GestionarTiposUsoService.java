package com.gerson.ramos.control_vehiculos.configuracion.application.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gerson.ramos.control_vehiculos.configuracion.application.port.in.GestionarTiposUsoUseCase;
import com.gerson.ramos.control_vehiculos.configuracion.application.port.out.TipoUsoRepositoryPort;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.CodigoCatalogo;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.TipoUso;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.TipoUsoId;
import com.gerson.ramos.control_vehiculos.shared.domain.ConflictoDeDatosException;
import com.gerson.ramos.control_vehiculos.shared.domain.RecursoNoEncontradoException;

@Service
@Transactional
public class GestionarTiposUsoService implements GestionarTiposUsoUseCase {

	private final TipoUsoRepositoryPort tiposUso;

	public GestionarTiposUsoService(TipoUsoRepositoryPort tiposUso) {
		this.tiposUso = tiposUso;
	}

	@Override
	public TipoUso crear(String codigo, String nombre) {
		CodigoCatalogo codigoCatalogo = new CodigoCatalogo(codigo);
		if (tiposUso.existePorCodigo(codigoCatalogo)) {
			throw new ConflictoDeDatosException("codigo", "Ya existe un tipo de uso con el código " + codigoCatalogo.valor());
		}
		return tiposUso.guardar(TipoUso.crear(codigoCatalogo, nombre));
	}

	@Override
	public TipoUso renombrar(long id, String nombre) {
		TipoUso tipoUso = cargar(id);
		tipoUso.renombrar(nombre);
		return tiposUso.guardar(tipoUso);
	}

	@Override
	public TipoUso cambiarEstado(long id, boolean activo) {
		TipoUso tipoUso = cargar(id);
		if (activo == tipoUso.activo()) {
			return tipoUso;
		}
		if (activo) {
			tipoUso.activar();
		}
		else {
			tipoUso.desactivar();
		}
		return tiposUso.guardar(tipoUso);
	}

	@Override
	@Transactional(readOnly = true)
	public TipoUso buscar(long id) {
		return cargar(id);
	}

	@Override
	@Transactional(readOnly = true)
	public List<TipoUso> listar() {
		return tiposUso.listarTodos();
	}

	private TipoUso cargar(long id) {
		return tiposUso.buscarPorId(new TipoUsoId(id)).orElseThrow(() -> new RecursoNoEncontradoException("Tipo de uso", id));
	}
}
