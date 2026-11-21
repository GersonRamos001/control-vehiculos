package com.gerson.ramos.control_vehiculos.shared.infrastructure.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.gerson.ramos.control_vehiculos.shared.domain.RecursoNoEncontradoException;
import com.gerson.ramos.control_vehiculos.shared.domain.ReglaDeNegocioException;

/**
 * Red de seguridad para errores que los controladores no manejan por sí mismos (los formularios muestran sus
 * errores de dominio junto a los campos). Devuelve páginas de error en lugar de trazas.
 */
@ControllerAdvice
class GlobalExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(RecursoNoEncontradoException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	String noEncontrado(RecursoNoEncontradoException excepcion, Model modelo) {
		modelo.addAttribute("titulo", "No encontrado");
		modelo.addAttribute("mensaje", excepcion.getMessage());
		return "error/error";
	}

	@ExceptionHandler(ReglaDeNegocioException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	String reglaDeNegocio(ReglaDeNegocioException excepcion, Model modelo) {
		modelo.addAttribute("titulo", "No se pudo completar la operación");
		modelo.addAttribute("mensaje", excepcion.getMessage());
		return "error/error";
	}

	@ExceptionHandler({ ObjectOptimisticLockingFailureException.class, DataIntegrityViolationException.class })
	@ResponseStatus(HttpStatus.CONFLICT)
	String conflicto(Exception excepcion, Model modelo) {
		log.warn("Conflicto de datos: {}", excepcion.getMessage());
		modelo.addAttribute("titulo", "Conflicto de datos");
		modelo.addAttribute("mensaje",
				"Los datos cambiaron o ya existen. Recarga la página y vuelve a intentarlo.");
		return "error/error";
	}
}
