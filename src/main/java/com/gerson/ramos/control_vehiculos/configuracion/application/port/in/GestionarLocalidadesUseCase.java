package com.gerson.ramos.control_vehiculos.configuracion.application.port.in;

import java.util.List;

import com.gerson.ramos.control_vehiculos.configuracion.domain.model.Localidad;

/** Administración de localidades. El código es único e inmutable una vez creada. */
public interface GestionarLocalidadesUseCase {

	Localidad crear(String codigo, String nombre);

	Localidad renombrar(long id, String nombre);

	Localidad cambiarEstado(long id, boolean activa);

	/** @throws com.gerson.ramos.control_vehiculos.shared.domain.RecursoNoEncontradoException si no existe */
	Localidad buscar(long id);

	List<Localidad> listar();
}
