package com.gerson.ramos.control_vehiculos.alertas.infrastructure.adapter.in.web;

import java.security.Principal;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.gerson.ramos.control_vehiculos.alertas.application.port.in.ConsultarAlertasUseCase;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Expone a todas las vistas el número de alertas activas (vencidas + próximas) como {@code alertasActivas}, para la
 * insignia de la barra de navegación.
 *
 * <p>Nunca debe romper una página: el caso de uso es opcional (en pruebas de otros módulos no existe y simplemente no
 * hay contador), solo se calcula para usuarios con sesión y en peticiones GET, y cualquier error se registra y se omite
 * el contador.
 */
@ControllerAdvice
class ContadorDeAlertasAdvice {

	private static final Logger log = LoggerFactory.getLogger(ContadorDeAlertasAdvice.class);

	private final ObjectProvider<ConsultarAlertasUseCase> alertas;

	ContadorDeAlertasAdvice(ObjectProvider<ConsultarAlertasUseCase> alertas) {
		this.alertas = alertas;
	}

	@ModelAttribute("alertasActivas")
	@Nullable Integer alertasActivas(HttpServletRequest peticion, @Nullable Principal usuario) {
		if (usuario == null || !"GET".equals(peticion.getMethod())) {
			return null;
		}
		ConsultarAlertasUseCase casoDeUso = alertas.getIfAvailable();
		if (casoDeUso == null) {
			return null;
		}
		try {
			return casoDeUso.consultar().resumen().activas();
		}
		catch (RuntimeException excepcion) {
			log.warn("No se pudo calcular el contador de alertas: {}", excepcion.getMessage());
			return null;
		}
	}
}
