package com.gerson.ramos.control_vehiculos.usuarios.infrastructure.config;

import java.time.Duration;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.gerson.ramos.control_vehiculos.usuarios.application.port.out.PoliticaDeBloqueo;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(SeguridadProperties.class)
class UsuariosConfig {

	@Bean
	PoliticaDeBloqueo politicaDeBloqueo(SeguridadProperties propiedades) {
		return new PoliticaDeBloqueo(propiedades.maxIntentosFallidos(), Duration.ofMinutes(propiedades.minutosBloqueo()));
	}
}
