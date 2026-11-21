package com.gerson.ramos.control_vehiculos.usuarios.infrastructure.adapter.in.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.gerson.ramos.control_vehiculos.shared.domain.ReglaDeNegocioException;
import com.gerson.ramos.control_vehiculos.usuarios.application.port.in.CrearPrimerAdministradorUseCase;
import com.gerson.ramos.control_vehiculos.usuarios.infrastructure.config.SeguridadProperties;

class PrimerAdministradorRunnerTest {

	private final List<String> llamadas = new ArrayList<>();

	private static SeguridadProperties propiedades(String usuario, String password) {
		return new SeguridadProperties(usuario, password, 5, 15);
	}

	@Test
	void entrega_las_credenciales_configuradas_al_caso_de_uso() {
		CrearPrimerAdministradorUseCase casoDeUso = (usuario, clave) -> {
			llamadas.add(usuario + "/" + clave);
			return true;
		};

		new PrimerAdministradorRunner(casoDeUso, propiedades("admin", "admin123")).run(null);

		assertThat(llamadas).containsExactly("admin/admin123");
	}

	@Test
	void si_ya_hay_usuarios_no_pasa_nada() {
		CrearPrimerAdministradorUseCase casoDeUso = (usuario, clave) -> false;

		new PrimerAdministradorRunner(casoDeUso, propiedades("admin", "admin123")).run(null);
	}

	@Test
	void con_la_tabla_vacia_y_credenciales_invalidas_la_aplicacion_no_arranca_con_un_mensaje_claro() {
		CrearPrimerAdministradorUseCase casoDeUso = (usuario, clave) -> {
			throw new ReglaDeNegocioException("El nombre de usuario es obligatorio");
		};

		assertThatThrownBy(() -> new PrimerAdministradorRunner(casoDeUso, propiedades("", "")).run(null))
			.isInstanceOf(IllegalStateException.class)
			.hasMessageContaining("APP_SECURITY_USUARIO")
			.hasMessageContaining("El nombre de usuario es obligatorio");
	}
}
