package com.gerson.ramos.control_vehiculos.usuarios.infrastructure.adapter.in.web;

import java.util.Objects;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.validation.BindingResult;

import com.gerson.ramos.control_vehiculos.shared.domain.ConflictoDeConcurrenciaException;
import com.gerson.ramos.control_vehiculos.shared.domain.ConflictoDeDatosException;

/** Traduce errores de dominio/persistencia a errores de formulario (junto al campo cuando el dominio lo indica). */
final class ErroresDeFormulario {

	private ErroresDeFormulario() {
	}

	static void registrar(BindingResult resultado, Exception excepcion) {
		if (excepcion instanceof ConflictoDeDatosException conflicto) {
			resultado.rejectValue(conflicto.campo(), "conflicto", conflicto.getMessage());
		}
		else if (excepcion instanceof ConflictoDeConcurrenciaException || excepcion instanceof ObjectOptimisticLockingFailureException) {
			resultado.reject("concurrencia", "Otro usuario modificó este registro. Recarga la página y vuelve a intentarlo.");
		}
		else if (excepcion instanceof DataIntegrityViolationException) {
			resultado.reject("duplicado", "Ya existe un usuario con ese nombre de usuario.");
		}
		else {
			resultado.reject("regla", Objects.requireNonNullElse(excepcion.getMessage(), "Datos inválidos"));
		}
	}

	/** La contraseña y su confirmación son obligatorias y deben coincidir. */
	static void validarClaveConfirmada(BindingResult resultado, String clave, String confirmacion) {
		if (clave == null || clave.isEmpty()) {
			resultado.rejectValue("clave", "obligatoria", "La contraseña es obligatoria");
		}
		else if (!clave.equals(confirmacion)) {
			resultado.rejectValue("confirmacion", "distinta", "La confirmación no coincide con la contraseña");
		}
	}
}
