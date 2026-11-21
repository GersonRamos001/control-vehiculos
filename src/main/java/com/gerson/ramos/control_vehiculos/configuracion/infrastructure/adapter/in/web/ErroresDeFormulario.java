package com.gerson.ramos.control_vehiculos.configuracion.infrastructure.adapter.in.web;

import java.util.Objects;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.validation.BindingResult;

import com.gerson.ramos.control_vehiculos.shared.domain.ConflictoDeDatosException;

/** Traduce errores de dominio/persistencia a errores de formulario (junto al campo cuando el dominio lo indica). */
final class ErroresDeFormulario {

	private ErroresDeFormulario() {
	}

	static void registrar(BindingResult resultado, Exception excepcion) {
		if (excepcion instanceof ConflictoDeDatosException conflicto) {
			resultado.rejectValue(conflicto.campo(), "conflicto", conflicto.getMessage());
		}
		else if (excepcion instanceof DataIntegrityViolationException) {
			resultado.reject("duplicado", "Ya existe un registro con ese código.");
		}
		else {
			resultado.reject("regla", Objects.requireNonNullElse(excepcion.getMessage(), "Datos inválidos"));
		}
	}
}
