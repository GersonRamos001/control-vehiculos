package com.gerson.ramos.control_vehiculos.usuarios.infrastructure.adapter.in.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

import com.gerson.ramos.control_vehiculos.usuarios.application.port.in.RegistrarAutenticacionUseCase;

/**
 * Escucha los resultados del inicio de sesión para llevar el conteo de intentos fallidos (bloqueo) y el último acceso.
 * Un fallo al registrar se anota en el log y nunca interrumpe el inicio de sesión.
 */
@Component
class EventosDeAutenticacion {

	private static final Logger log = LoggerFactory.getLogger(EventosDeAutenticacion.class);

	private final RegistrarAutenticacionUseCase autenticacion;

	EventosDeAutenticacion(RegistrarAutenticacionUseCase autenticacion) {
		this.autenticacion = autenticacion;
	}

	@EventListener
	void alFallarLaContrasena(AuthenticationFailureBadCredentialsEvent evento) {
		try {
			autenticacion.registrarIntentoFallido(evento.getAuthentication().getName());
		}
		catch (RuntimeException excepcion) {
			log.warn("No se pudo registrar el intento fallido de acceso: {}", excepcion.getMessage());
		}
	}

	@EventListener
	void alIniciarSesion(AuthenticationSuccessEvent evento) {
		try {
			autenticacion.registrarAccesoCorrecto(evento.getAuthentication().getName());
		}
		catch (RuntimeException excepcion) {
			log.warn("No se pudo registrar el último acceso: {}", excepcion.getMessage());
		}
	}
}
