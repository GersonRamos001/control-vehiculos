package com.gerson.ramos.control_vehiculos.usuarios.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.gerson.ramos.control_vehiculos.shared.domain.ConflictoDeDatosException;
import com.gerson.ramos.control_vehiculos.shared.domain.RecursoNoEncontradoException;
import com.gerson.ramos.control_vehiculos.shared.domain.ReglaDeNegocioException;
import com.gerson.ramos.control_vehiculos.testsupport.CodificadorDeClaveFalso;
import com.gerson.ramos.control_vehiculos.testsupport.InMemoryUsuarioRepository;
import com.gerson.ramos.control_vehiculos.usuarios.domain.model.Rol;
import com.gerson.ramos.control_vehiculos.usuarios.domain.model.Usuario;

class CambiarClavePropiaServiceTest {

	private final InMemoryUsuarioRepository repositorio = new InMemoryUsuarioRepository();
	private final CodificadorDeClaveFalso codificador = new CodificadorDeClaveFalso();
	private final CambiarClavePropiaService servicio = new CambiarClavePropiaService(repositorio, codificador);

	@BeforeEach
	void crearUsuario() {
		repositorio.guardar(Usuario.crear("ana", "Ana", Rol.OPERADOR, codificador.codificar("clave-actual-1")));
	}

	@Test
	void cambia_la_clave_cuando_la_actual_es_correcta() {
		servicio.cambiar("ANA", "clave-actual-1", "clave-nueva-2");

		assertThat(repositorio.buscarPorUsername("ana").orElseThrow().hashClave()).isEqualTo(codificador.codificar("clave-nueva-2"));
	}

	@Test
	void rechaza_una_clave_actual_incorrecta_junto_a_su_campo() {
		assertThatThrownBy(() -> servicio.cambiar("ana", "equivocada-1", "clave-nueva-2"))
			.isInstanceOfSatisfying(ConflictoDeDatosException.class, e -> assertThat(e.campo()).isEqualTo("claveActual"));
		assertThat(repositorio.buscarPorUsername("ana").orElseThrow().hashClave()).isEqualTo(codificador.codificar("clave-actual-1"));
	}

	@Test
	void rechaza_una_clave_nueva_debil_o_igual_a_la_actual() {
		assertThatThrownBy(() -> servicio.cambiar("ana", "clave-actual-1", "corta")).isInstanceOf(ReglaDeNegocioException.class);
		assertThatThrownBy(() -> servicio.cambiar("ana", "clave-actual-1", "clave-actual-1")).hasMessageContaining("distinta de la actual");
	}

	@Test
	void falla_si_el_usuario_no_existe() {
		assertThatThrownBy(() -> servicio.cambiar("nadie", "x", "clave-nueva-2")).isInstanceOf(RecursoNoEncontradoException.class);
	}
}
