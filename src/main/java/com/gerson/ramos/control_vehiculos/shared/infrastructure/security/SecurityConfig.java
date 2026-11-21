package com.gerson.ramos.control_vehiculos.shared.infrastructure.security;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationEventPublisher;
import org.springframework.security.authentication.DefaultAuthenticationEventPublisher;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.session.SessionLimit;
import org.springframework.security.web.session.HttpSessionEventPublisher;

/**
 * Inicio de sesión por formulario y permisos por rol. Los usuarios vienen de la base de datos (módulo {@code usuarios},
 * que aporta el {@code UserDetailsService}). CSRF queda activado (Thymeleaf lo incluye en los formularios).
 *
 * <p>Los nombres de rol coinciden con el enum {@code Rol} del módulo de usuarios (este paquete no depende de ese módulo):
 * <ul>
 * <li>ADMINISTRADOR: todo, incluidos {@code /usuarios/**} y {@code /configuracion/**}.</li>
 * <li>OPERADOR: todo lo operativo (vehículos, mantenimientos, kilometraje, alertas y reportes).</li>
 * <li>CONSULTA: solo lectura de la lista de vehículos, la programación, las alertas y los reportes.</li>
 * </ul>
 */
@Configuration(proxyBeanMethods = false)
@EnableWebSecurity
public class SecurityConfig {

	static final String ADMINISTRADOR = "ADMINISTRADOR";
	static final String OPERADOR = "OPERADOR";

	@Bean
	SecurityFilterChain filtrosDeSeguridad(HttpSecurity http, SessionRegistry registroDeSesiones) throws Exception {
		http
			.authorizeHttpRequests(autorizacion -> autorizacion
				.requestMatchers("/login", "/css/**", "/error", "/actuator/health", "/actuator/health/**").permitAll()
				.requestMatchers("/usuarios/**", "/configuracion/**").hasRole(ADMINISTRADOR)
				// Lo que puede ver cualquier usuario con sesión (incluido el rol CONSULTA).
				.requestMatchers("/", "/js/**", "/cuenta/**", "/alertas", "/reportes/**", "/mantenimientos/programacion")
				.authenticated()
				.requestMatchers(HttpMethod.GET, "/vehiculos").authenticated()
				.anyRequest().hasAnyRole(ADMINISTRADOR, OPERADOR))
			.formLogin(login -> login.loginPage("/login").permitAll())
			.logout(logout -> logout.logoutSuccessUrl("/login?logout"))
			// Sesiones registradas (sin límite por usuario) para poder cerrarlas al desactivar o cambiar a un usuario.
			.sessionManagement(sesiones -> sesiones.sessionConcurrency(
					concurrencia -> concurrencia.maximumSessions(SessionLimit.UNLIMITED)
						.sessionRegistry(registroDeSesiones)
						.expiredUrl("/login?expirada")));
		return http.build();
	}

	/** Codificador por defecto de Spring Security (BCrypt con prefijo {bcrypt}). */
	@Bean
	PasswordEncoder passwordEncoder() {
		return PasswordEncoderFactories.createDelegatingPasswordEncoder();
	}

	@Bean
	SessionRegistry registroDeSesiones() {
		return new SessionRegistryImpl();
	}

	/** Avisa al registro cuando una sesión termina, para que no acumule sesiones muertas. */
	@Bean
	HttpSessionEventPublisher publicadorDeEventosDeSesion() {
		return new HttpSessionEventPublisher();
	}

	/** Publica los resultados del inicio de sesión (éxito, contraseña incorrecta...) que escucha el módulo de usuarios. */
	@Bean
	AuthenticationEventPublisher publicadorDeEventosDeAutenticacion(ApplicationEventPublisher publicador) {
		return new DefaultAuthenticationEventPublisher(publicador);
	}
}
