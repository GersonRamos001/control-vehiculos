package com.gerson.ramos.control_vehiculos.shared.infrastructure.time;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Reloj inyectable: los servicios reciben "hoy" de aquí, así los tests pueden fijar la fecha. */
@Configuration(proxyBeanMethods = false)
class ClockConfig {

	@Bean
	Clock reloj() {
		return Clock.systemDefaultZone();
	}
}
