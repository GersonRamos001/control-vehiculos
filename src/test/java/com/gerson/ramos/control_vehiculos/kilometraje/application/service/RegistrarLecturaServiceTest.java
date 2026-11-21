package com.gerson.ramos.control_vehiculos.kilometraje.application.service;

import static com.gerson.ramos.control_vehiculos.kilometraje.application.service.EscenarioKilometraje.HOY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import com.gerson.ramos.control_vehiculos.kilometraje.application.port.in.RegistrarLecturaComando;
import com.gerson.ramos.control_vehiculos.kilometraje.application.port.in.ResultadoRegistro;
import com.gerson.ramos.control_vehiculos.kilometraje.application.port.in.ResultadoRegistro.Tipo;
import com.gerson.ramos.control_vehiculos.shared.domain.ConflictoDeDatosException;
import com.gerson.ramos.control_vehiculos.shared.domain.RecursoNoEncontradoException;
import com.gerson.ramos.control_vehiculos.shared.domain.ReglaDeNegocioException;

class RegistrarLecturaServiceTest {

	private final EscenarioKilometraje escenario = new EscenarioKilometraje();

	private static void assertCampo(Throwable error, String campo) {
		assertThat(error).isInstanceOfSatisfying(ConflictoDeDatosException.class, e -> assertThat(e.campo()).isEqualTo(campo));
	}

	@Test
	void registra_una_lectura_nueva() {
		ResultadoRegistro resultado = escenario.registrarLectura(LocalDate.of(2026, 11, 14), 107_500);

		assertThat(resultado.tipo()).isEqualTo(Tipo.NUEVA);
		assertThat(resultado.lectura().id()).isNotNull();
		assertThat(resultado.lectura().kilometraje()).isEqualTo(107_500);
		assertThat(escenario.lecturasRepo.historialDe(escenario.vehiculoActivo.id())).hasSize(1);
	}

	@Test
	void volver_a_ingresar_la_misma_fecha_reemplaza_la_lectura() {
		ResultadoRegistro primera = escenario.registrarLectura(LocalDate.of(2026, 11, 14), 175_000); // error de dedo

		ResultadoRegistro corregida = escenario.registrarLectura(LocalDate.of(2026, 11, 14), 107_500);

		assertThat(corregida.tipo()).isEqualTo(Tipo.REEMPLAZADA);
		assertThat(corregida.lectura().id()).isEqualTo(primera.lectura().id());
		assertThat(escenario.lecturasRepo.historialDe(escenario.vehiculoActivo.id())).hasSize(1);
		assertThat(escenario.lecturasRepo.ultimaDe(escenario.vehiculoActivo.id()).orElseThrow().kilometraje()).isEqualTo(107_500);
	}

	@Test
	void reenviar_la_misma_lectura_no_cambia_nada() {
		escenario.registrarLectura(LocalDate.of(2026, 11, 14), 107_500);

		ResultadoRegistro otraVez = escenario.registrarLectura(LocalDate.of(2026, 11, 14), 107_500);

		assertThat(otraVez.tipo()).isEqualTo(Tipo.SIN_CAMBIOS);
		assertThat(escenario.lecturasRepo.historialDe(escenario.vehiculoActivo.id())).hasSize(1);
	}

	@Test
	void permite_la_lectura_de_hoy_y_rechaza_una_fecha_futura() {
		assertThat(escenario.registrarLectura(HOY, 100_000).tipo()).isEqualTo(Tipo.NUEVA);
		assertThatThrownBy(() -> escenario.registrarLectura(HOY.plusDays(1), 100_500)).satisfies(e -> assertCampo(e, "fecha"));
	}

	@Test
	void rechaza_un_vehiculo_inactivo_y_uno_inexistente() {
		assertThatThrownBy(() -> escenario.registrarLectura(escenario.vehiculoInactivo.id().valor(), HOY, 100_000))
			.isInstanceOf(ReglaDeNegocioException.class)
			.isNotInstanceOf(ConflictoDeDatosException.class);
		assertThatThrownBy(() -> escenario.registrarLectura(999, HOY, 100_000)).isInstanceOf(RecursoNoEncontradoException.class);
	}

	@Test
	void el_odometro_no_puede_bajar_respecto_a_una_lectura_anterior() {
		escenario.registrarLectura(LocalDate.of(2026, 11, 7), 107_500);

		assertThatThrownBy(() -> escenario.registrarLectura(LocalDate.of(2026, 11, 14), 107_000))
			.satisfies(e -> assertCampo(e, "kilometraje"));
	}

	@Test
	void el_odometro_no_puede_superar_el_de_una_lectura_posterior() {
		escenario.registrarLectura(LocalDate.of(2026, 11, 14), 107_500);

		assertThatThrownBy(() -> escenario.registrarLectura(LocalDate.of(2026, 11, 7), 108_000))
			.satisfies(e -> assertCampo(e, "kilometraje"));
	}

	@Test
	void una_correccion_de_la_misma_fecha_puede_bajar_el_kilometraje_si_sigue_siendo_coherente() {
		escenario.registrarLectura(LocalDate.of(2026, 11, 7), 107_000);
		escenario.registrarLectura(LocalDate.of(2026, 11, 14), 190_000); // dedo de más

		assertThatCode(() -> escenario.registrarLectura(LocalDate.of(2026, 11, 14), 107_500)).doesNotThrowAnyException();
	}

	@Test
	void rechaza_una_lectura_menor_al_ultimo_mantenimiento_si_es_de_igual_o_posterior_fecha() {
		escenario.programacion.establecer(escenario.idActivo(), LocalDate.of(2026, 11, 1), 100_000, 110_000, 161);

		assertThatThrownBy(() -> escenario.registrarLectura(LocalDate.of(2026, 11, 14), 99_999))
			.satisfies(e -> assertCampo(e, "kilometraje"));
		assertThatThrownBy(() -> escenario.registrarLectura(LocalDate.of(2026, 11, 1), 99_999))
			.satisfies(e -> assertCampo(e, "kilometraje"));
		assertThatCode(() -> escenario.registrarLectura(LocalDate.of(2026, 11, 14), 100_000)).doesNotThrowAnyException();
	}

	@Test
	void una_lectura_anterior_al_ultimo_mantenimiento_puede_tener_menos_km() {
		escenario.programacion.establecer(escenario.idActivo(), LocalDate.of(2026, 11, 1), 100_000, 110_000, 161);

		assertThatCode(() -> escenario.registrarLectura(LocalDate.of(2026, 10, 20), 98_000)).doesNotThrowAnyException();
	}

	@Test
	void rechaza_un_kilometraje_negativo() {
		assertThatThrownBy(() -> escenario.registrar.registrar(new RegistrarLecturaComando(escenario.idActivo(), HOY, -1)))
			.satisfies(e -> assertCampo(e, "kilometraje"));
	}

	@Test
	void las_lecturas_de_un_vehiculo_no_afectan_a_otro() {
		escenario.registrarLectura(HOY, 500_000);

		assertThat(escenario.registrarLectura(escenario.idOtro(), HOY, 50_000).tipo()).isEqualTo(Tipo.NUEVA);
	}
}
