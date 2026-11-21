package com.gerson.ramos.control_vehiculos.configuracion.application.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gerson.ramos.control_vehiculos.configuracion.application.port.in.GestionarRutinasUseCase;
import com.gerson.ramos.control_vehiculos.configuracion.application.port.out.RutinaMantenimientoRepositoryPort;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.CodigoCatalogo;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.RutinaMantenimiento;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.RutinaMantenimientoId;
import com.gerson.ramos.control_vehiculos.shared.domain.ConflictoDeDatosException;
import com.gerson.ramos.control_vehiculos.shared.domain.RecursoNoEncontradoException;

@Service
@Transactional
public class GestionarRutinasService implements GestionarRutinasUseCase {

	private final RutinaMantenimientoRepositoryPort rutinas;

	public GestionarRutinasService(RutinaMantenimientoRepositoryPort rutinas) {
		this.rutinas = rutinas;
	}

	@Override
	public RutinaMantenimiento crear(String codigo, String nombre) {
		CodigoCatalogo codigoCatalogo = new CodigoCatalogo(codigo);
		if (rutinas.existePorCodigo(codigoCatalogo)) {
			throw new ConflictoDeDatosException("codigo", "Ya existe una rutina con el código " + codigoCatalogo.valor());
		}
		return rutinas.guardar(RutinaMantenimiento.crear(codigoCatalogo, nombre));
	}

	@Override
	public RutinaMantenimiento renombrar(long id, String nombre) {
		RutinaMantenimiento rutina = cargar(id);
		rutina.renombrar(nombre);
		return rutinas.guardar(rutina);
	}

	@Override
	public RutinaMantenimiento cambiarEstado(long id, boolean activa) {
		RutinaMantenimiento rutina = cargar(id);
		if (activa == rutina.activo()) {
			return rutina;
		}
		if (activa) {
			rutina.activar();
		}
		else {
			rutina.desactivar();
		}
		return rutinas.guardar(rutina);
	}

	@Override
	@Transactional(readOnly = true)
	public RutinaMantenimiento buscar(long id) {
		return cargar(id);
	}

	@Override
	@Transactional(readOnly = true)
	public List<RutinaMantenimiento> listar() {
		return rutinas.listarTodas();
	}

	private RutinaMantenimiento cargar(long id) {
		return rutinas.buscarPorId(new RutinaMantenimientoId(id))
			.orElseThrow(() -> new RecursoNoEncontradoException("Rutina de mantenimiento", id));
	}
}
