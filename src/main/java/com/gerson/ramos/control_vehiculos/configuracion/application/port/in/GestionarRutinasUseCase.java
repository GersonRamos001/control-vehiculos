package com.gerson.ramos.control_vehiculos.configuracion.application.port.in;

import java.util.List;

import com.gerson.ramos.control_vehiculos.configuracion.domain.model.RutinaMantenimiento;

/** Administración de rutinas de mantenimiento preventivo (10K, 20K, 30K). El código es único e inmutable. */
public interface GestionarRutinasUseCase {

	RutinaMantenimiento crear(String codigo, String nombre);

	RutinaMantenimiento renombrar(long id, String nombre);

	RutinaMantenimiento cambiarEstado(long id, boolean activa);

	/** @throws com.gerson.ramos.control_vehiculos.shared.domain.RecursoNoEncontradoException si no existe */
	RutinaMantenimiento buscar(long id);

	List<RutinaMantenimiento> listar();
}
