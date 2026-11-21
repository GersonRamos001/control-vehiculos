package com.gerson.ramos.control_vehiculos.usuarios.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Min;

/**
 * Configuración del acceso ({@code app.security}).
 * <ul>
 * <li>{@code usuario} y {@code password}: administrador inicial. Solo se usan cuando no existe ningún usuario en la base
 * de datos (primer arranque); después se administran desde la aplicación. Fuera del perfil {@code dev} vienen de las
 * variables de entorno {@code APP_SECURITY_USUARIO} y {@code APP_SECURITY_PASSWORD}.</li>
 * <li>{@code maxIntentosFallidos} y {@code minutosBloqueo}: contraseñas incorrectas seguidas que bloquean la cuenta y
 * por cuánto tiempo.</li>
 * </ul>
 */
@ConfigurationProperties(prefix = "app.security")
@Validated
public record SeguridadProperties(@DefaultValue("") String usuario, @DefaultValue("") String password,
		@DefaultValue("5") @Min(1) int maxIntentosFallidos, @DefaultValue("15") @Min(1) int minutosBloqueo) {
}
