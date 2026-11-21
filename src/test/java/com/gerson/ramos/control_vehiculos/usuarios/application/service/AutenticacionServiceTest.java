package com.gerson.ramos.control_vehiculos.usuarios.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

import com.gerson.ramos.control_vehiculos.testsupport.InMemoryUsuarioRepository;
import com.gerson.ramos.control_vehiculos.usuarios.application.port.in.CredencialesDeAcceso;
import com.gerson.ramos.control_vehiculos.usuarios.application.port.out.PoliticaDeBloqueo;
import com.gerson.ramos.control_vehiculos.usuarios.domain.model.Rol;
import com.gerson.ramos.control_vehiculos.usuarios.domain.model.Usuario;

class AutenticacionServiceTest {

	private static final Instant AHORA = Instant.parse("2026-11-21T12:00:00Z");
	private static final PoliticaDeBloqueo POLITICA = new PoliticaDeBloqueo(3, Duration.ofMinutes(15));

	private final InMemoryUsuarioRepository repositorio = new InMemoryUsuarioRepository();

	private AutenticacionService servicioEn(Instant instante) {
		return new AutenticacionService(repositorio, POLITICA, Clock.fixed(instante, ZoneOffset.UTC));
	}

	private Usuario guardar(String username, boolean activo) {
		Usuario usuario = Usuario.crear(username, "Nombre", Rol.OPERADOR, "hash:clave");
		if (!activo) {
			usuario.desactivar();
		}
		return repositorio.guardar(usuario);
	}

	private Usuario leer(String username) {
		return repositorio.buscarPorUsername(username).orElseThrow();
	}

	@Test
	void entrega_las_credenciales_sin_distinguir_mayusculas() {
		guardar("ana", true);

		CredencialesDeAcceso credenciales = servicioEn(AHORA).buscarPorUsername("  ANA ").orElseThrow();

		assertThat(credenciales).isEqualTo(new CredencialesDeAcceso("ana", "hash:clave", Rol.OPERADOR, true, false));
		assertThat(servicioEn(AHORA).buscarPorUsername("nadie")).isEmpty();
	}

	@Test
	void marca_como_inactivo_al_usuario_desactivado() {
		guardar("ana", false);

		assertThat(servicioEn(AHORA).buscarPorUsername("ana").orElseThrow().activo()).isFalse();
	}

	@Test
	void los_intentos_fallidos_seguidos_bloquean_y_el_bloqueo_se_ve_en_las_credenciales() {
		guardar("ana", true);
		AutenticacionService servicio = servicioEn(AHORA);

		servicio.registrarIntentoFallido("ana");
		servicio.registrarIntentoFallido("ANA");
		assertThat(servicio.buscarPorUsername("ana").orElseThrow().bloqueado()).isFalse();

		servicio.registrarIntentoFallido("ana");

		assertThat(servicio.buscarPorUsername("ana").orElseThrow().bloqueado()).isTrue();
		assertThat(leer("ana").bloqueadoHasta()).isEqualTo(AHORA.plus(Duration.ofMinutes(15)));
	}

	@Test
	void el_bloqueo_se_libera_solo_al_pasar_el_tiempo_y_el_conteo_empieza_de_nuevo() {
		guardar("ana", true);
		for (int i = 0; i < 3; i++) {
			servicioEn(AHORA).registrarIntentoFallido("ana");
		}
		Instant despues = AHORA.plus(Duration.ofMinutes(16));

		assertThat(servicioEn(AHORA).buscarPorUsername("ana").orElseThrow().bloqueado()).isTrue();
		assertThat(servicioEn(despues).buscarPorUsername("ana").orElseThrow().bloqueado()).isFalse();

		servicioEn(despues).registrarIntentoFallido("ana");

		assertThat(leer("ana").intentosFallidos()).isEqualTo(1);
		assertThat(leer("ana").bloqueadoHasta()).isNull();
	}

	@Test
	void un_usuario_inexistente_o_desactivado_no_acumula_intentos() {
		guardar("inactiva", false);

		servicioEn(AHORA).registrarIntentoFallido("nadie");
		servicioEn(AHORA).registrarIntentoFallido("inactiva");

		assertThat(repositorio.contar()).isEqualTo(1);
		assertThat(leer("inactiva").intentosFallidos()).isZero();
	}

	@Test
	void el_acceso_correcto_reinicia_los_intentos_y_anota_el_ultimo_acceso() {
		guardar("ana", true);
		servicioEn(AHORA).registrarIntentoFallido("ana");
		servicioEn(AHORA).registrarIntentoFallido("ana");

		servicioEn(AHORA.plusSeconds(30)).registrarAccesoCorrecto("ana");

		assertThat(leer("ana").intentosFallidos()).isZero();
		assertThat(leer("ana").ultimoAcceso()).isEqualTo(AHORA.plusSeconds(30));
	}

	@Test
	void el_acceso_correcto_de_un_usuario_desconocido_se_ignora() {
		servicioEn(AHORA).registrarAccesoCorrecto("nadie");

		assertThat(repositorio.contar()).isZero();
	}
}
