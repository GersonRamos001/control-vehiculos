package com.gerson.ramos.control_vehiculos.configuracion.application.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gerson.ramos.control_vehiculos.configuracion.application.port.in.GestionarLocalidadesUseCase;
import com.gerson.ramos.control_vehiculos.configuracion.application.port.out.LocalidadRepositoryPort;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.CodigoCatalogo;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.Localidad;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.LocalidadId;
import com.gerson.ramos.control_vehiculos.shared.domain.ConflictoDeDatosException;
import com.gerson.ramos.control_vehiculos.shared.domain.RecursoNoEncontradoException;

@Service
@Transactional
public class GestionarLocalidadesService implements GestionarLocalidadesUseCase {

	private final LocalidadRepositoryPort localidades;

	public GestionarLocalidadesService(LocalidadRepositoryPort localidades) {
		this.localidades = localidades;
	}

	@Override
	public Localidad crear(String codigo, String nombre) {
		CodigoCatalogo codigoCatalogo = new CodigoCatalogo(codigo);
		if (localidades.existePorCodigo(codigoCatalogo)) {
			throw new ConflictoDeDatosException("codigo", "Ya existe una localidad con el código " + codigoCatalogo.valor());
		}
		return localidades.guardar(Localidad.crear(codigoCatalogo, nombre));
	}

	@Override
	public Localidad renombrar(long id, String nombre) {
		Localidad localidad = cargar(id);
		localidad.renombrar(nombre);
		return localidades.guardar(localidad);
	}

	@Override
	public Localidad cambiarEstado(long id, boolean activa) {
		Localidad localidad = cargar(id);
		if (activa == localidad.activo()) {
			return localidad;
		}
		if (activa) {
			localidad.activar();
		}
		else {
			localidad.desactivar();
		}
		return localidades.guardar(localidad);
	}

	@Override
	@Transactional(readOnly = true)
	public Localidad buscar(long id) {
		return cargar(id);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Localidad> listar() {
		return localidades.listarTodas();
	}

	private Localidad cargar(long id) {
		return localidades.buscarPorId(new LocalidadId(id)).orElseThrow(() -> new RecursoNoEncontradoException("Localidad", id));
	}
}
