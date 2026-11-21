package com.gerson.ramos.control_vehiculos.usuarios.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.gerson.ramos.control_vehiculos.shared.domain.ReglaDeNegocioException;

class UsuarioTest {

	private static final Instant AHORA = Instant.parse("2026-11-21T12:00:00Z");
	private static final Duration BLOQUEO = Duration.ofMinutes(15);

	private static Usuario nuevo() {
		return Usuario.crear("Ana.Lopez", "Ana López", Rol.OPERADOR, "hash:clave-segura");
	}

	@Test
	void el_usuario_nuevo_nace_activo_sin_bloqueo_ni_acceso_y_con_el_nombre_en_minusculas() {
		Usuario usuario = nuevo();

		assertThat(usuario.id()).isNull();
		assertThat(usuario.username()).isEqualTo("ana.lopez");
		assertThat(usuario.activo()).isTrue();
		assertThat(usuario.intentosFallidos()).isZero();
		assertThat(usuario.bloqueadoHasta()).isNull();
		assertThat(usuario.ultimoAcceso()).isNull();
		assertThat(usuario.estaBloqueado(AHORA)).isFalse();
	}

	@ParameterizedTest
	@ValueSource(strings = { "ab", "con espacio", "ñandú", "a@b.com", "muy-largo-muy-largo-muy-largo-muy-largo" })
	void rechaza_nombres_de_usuario_con_formato_invalido(String username) {
		assertThatThrownBy(() -> Usuario.crear(username, "Nombre", Rol.CONSULTA, "hash:x")).isInstanceOf(ReglaDeNegocioException.class);
	}

	@Test
	void exige_nombre_rol_y_hash() {
		assertThatThrownBy(() -> Usuario.crear("ana", " ", Rol.CONSULTA, "hash:x")).isInstanceOf(ReglaDeNegocioException.class);
		assertThatThrownBy(() -> Usuario.crear("ana", "Ana", null, "hash:x")).isInstanceOf(ReglaDeNegocioException.class);
		assertThatThrownBy(() -> Usuario.crear("ana", "Ana", Rol.CONSULTA, null)).isInstanceOf(ReglaDeNegocioException.class);
	}

	@Test
	void corregir_datos_cambia_nombre_y_rol() {
		Usuario usuario = nuevo();

		usuario.corregirDatos("  Ana María López ", Rol.ADMINISTRADOR);

		assertThat(usuario.nombreCompleto()).isEqualTo("Ana María López");
		assertThat(usuario.rol()).isEqualTo(Rol.ADMINISTRADOR);
	}

	@Test
	void los_intentos_fallidos_bloquean_la_cuenta_al_llegar_al_maximo() {
		Usuario usuario = nuevo();

		for (int i = 0; i < 4; i++) {
			usuario.registrarIntentoFallido(AHORA, 5, BLOQUEO);
		}
		assertThat(usuario.estaBloqueado(AHORA)).isFalse();
		assertThat(usuario.intentosFallidos()).isEqualTo(4);

		usuario.registrarIntentoFallido(AHORA, 5, BLOQUEO);

		assertThat(usuario.estaBloqueado(AHORA)).isTrue();
		assertThat(usuario.bloqueadoHasta()).isEqualTo(AHORA.plus(BLOQUEO));
	}

	@Test
	void el_bloqueo_termina_solo_y_el_conteo_vuelve_a_empezar() {
		Usuario usuario = nuevo();
		for (int i = 0; i < 5; i++) {
			usuario.registrarIntentoFallido(AHORA, 5, BLOQUEO);
		}
		Instant despues = AHORA.plus(BLOQUEO).plusSeconds(1);

		assertThat(usuario.estaBloqueado(AHORA.plus(BLOQUEO))).isFalse();
		assertThat(usuario.estaBloqueado(despues)).isFalse();

		usuario.registrarIntentoFallido(despues, 5, BLOQUEO);

		assertThat(usuario.intentosFallidos()).isEqualTo(1);
		assertThat(usuario.bloqueadoHasta()).isNull();
	}

	@Test
	void mientras_esta_bloqueada_no_se_acumulan_mas_intentos_ni_se_alarga_el_bloqueo() {
		Usuario usuario = nuevo();
		for (int i = 0; i < 5; i++) {
			usuario.registrarIntentoFallido(AHORA, 5, BLOQUEO);
		}
		Instant limite = usuario.bloqueadoHasta();

		usuario.registrarIntentoFallido(AHORA.plusSeconds(60), 5, BLOQUEO);

		assertThat(usuario.intentosFallidos()).isEqualTo(5);
		assertThat(usuario.bloqueadoHasta()).isEqualTo(limite);
	}

	@Test
	void el_acceso_correcto_reinicia_el_conteo_levanta_el_bloqueo_y_anota_el_ultimo_acceso() {
		Usuario usuario = nuevo();
		usuario.registrarIntentoFallido(AHORA, 5, BLOQUEO);
		usuario.registrarIntentoFallido(AHORA, 5, BLOQUEO);

		usuario.registrarAcceso(AHORA.plusSeconds(10));

		assertThat(usuario.intentosFallidos()).isZero();
		assertThat(usuario.bloqueadoHasta()).isNull();
		assertThat(usuario.ultimoAcceso()).isEqualTo(AHORA.plusSeconds(10));
	}

	@Test
	void desbloquear_levanta_el_bloqueo_sin_tocar_el_ultimo_acceso() {
		Usuario usuario = nuevo();
		usuario.registrarAcceso(AHORA);
		for (int i = 0; i < 5; i++) {
			usuario.registrarIntentoFallido(AHORA, 5, BLOQUEO);
		}

		usuario.desbloquear();

		assertThat(usuario.estaBloqueado(AHORA)).isFalse();
		assertThat(usuario.intentosFallidos()).isZero();
		assertThat(usuario.ultimoAcceso()).isEqualTo(AHORA);
	}

	@Test
	void la_politica_de_clave_exige_entre_8_y_72_caracteres_y_distinta_del_usuario() {
		assertThatThrownBy(() -> PoliticaDeClave.validar(null, "ana")).isInstanceOf(ReglaDeNegocioException.class);
		assertThatThrownBy(() -> PoliticaDeClave.validar("", "ana")).isInstanceOf(ReglaDeNegocioException.class);
		assertThatThrownBy(() -> PoliticaDeClave.validar("corta12", "ana")).hasMessageContaining("al menos 8");
		assertThatThrownBy(() -> PoliticaDeClave.validar("x".repeat(73), "ana")).hasMessageContaining("72");
		assertThatThrownBy(() -> PoliticaDeClave.validar("ANA-LOPEZ", "ana-lopez")).hasMessageContaining("igual al nombre de usuario");

		PoliticaDeClave.validar("12345678", "ana");
		PoliticaDeClave.validar("x".repeat(72), "ana");
	}

	@Test
	void solo_consulta_no_puede_escribir_y_solo_el_administrador_administra() {
		assertThat(Rol.ADMINISTRADOR.puedeEscribir()).isTrue();
		assertThat(Rol.OPERADOR.puedeEscribir()).isTrue();
		assertThat(Rol.CONSULTA.puedeEscribir()).isFalse();
		assertThat(Rol.ADMINISTRADOR.esAdministrador()).isTrue();
		assertThat(Rol.OPERADOR.esAdministrador()).isFalse();
	}
}
