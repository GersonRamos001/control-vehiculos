package com.gerson.ramos.control_vehiculos.configuracion.application.port.in;

import java.util.List;

import com.gerson.ramos.control_vehiculos.configuracion.domain.model.TipoUso;

/** Administración de tipos de uso. El código es único e inmutable una vez creado. */
public interface GestionarTiposUsoUseCase {

	TipoUso crear(String codigo, String nombre);

	TipoUso renombrar(long id, String nombre);

	TipoUso cambiarEstado(long id, boolean activo);

	/** @throws com.gerson.ramos.control_vehiculos.shared.domain.RecursoNoEncontradoException si no existe */
	TipoUso buscar(long id);

	List<TipoUso> listar();
}
