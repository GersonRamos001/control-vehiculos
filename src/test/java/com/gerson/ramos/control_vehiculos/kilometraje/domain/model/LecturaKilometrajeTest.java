package com.gerson.ramos.control_vehiculos.kilometraje.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.gerson.ramos.control_vehiculos.shared.domain.ConflictoDeDatosException;
import com.gerson.ramos.control_vehiculos.shared.domain.ReglaDeNegocioException;
import com.gerson.ramos.control_vehiculos.vehiculo.domain.model.VehiculoId;

class LecturaKilometrajeTest {

	private static final LocalDate HOY = LocalDate.of(2026, 11, 21);
	private static final VehiculoId VEHICULO = new VehiculoId(1);

	private static LecturaKilometraje nueva(LocalDate fecha, int km) {
		return LecturaKilometraje.registrar(VEHICULO, fecha, km, HOY);
	}

	private static LecturaKilometraje existente(long id, LocalDate fecha, int km) {
		return LecturaKilometraje.reconstruir(new LecturaId(id), VEHICULO, fecha, km);
	}

	private static void assertCampo(Throwable error, String campo) {
		assertThat(error).isInstanceOfSatisfying(ConflictoDeDatosException.class, e -> assertThat(e.campo()).isEqualTo(campo));
	}

	@Test
	void registra_una_lectura_nueva_sin_id() {
		LecturaKilometraje lectura = nueva(HOY, 107_500);

		assertThat(lectura.id()).isNull();
		assertThat(lectura.fecha()).isEqualTo(HOY);
		assertThat(lectura.kilometraje()).isEqualTo(107_500);
	}

	@Test
	void rechaza_fecha_futura_y_kilometraje_negativo() {
		assertThatThrownBy(() -> nueva(HOY.plusDays(1), 1)).satisfies(e -> assertCampo(e, "fecha"));
		assertThatThrownBy(() -> nueva(HOY, -1)).satisfies(e -> assertCampo(e, "kilometraje"));
	}

	@Test
	void exige_los_datos_obligatorios() {
		assertThatThrownBy(() -> LecturaKilometraje.registrar(null, HOY, 1, HOY)).isInstanceOf(ReglaDeNegocioException.class);
		assertThatThrownBy(() -> LecturaKilometraje.registrar(VEHICULO, null, 1, HOY)).isInstanceOf(ReglaDeNegocioException.class);
	}

	@Test
	void con_kilometraje_conserva_id_vehiculo_y_fecha() {
		LecturaKilometraje corregida = existente(7, LocalDate.of(2026, 11, 14), 175_000).conKilometraje(107_500);

		assertThat(corregida.id()).isEqualTo(new LecturaId(7));
		assertThat(corregida.vehiculoId()).isEqualTo(VEHICULO);
		assertThat(corregida.fecha()).isEqualTo(LocalDate.of(2026, 11, 14));
		assertThat(corregida.kilometraje()).isEqualTo(107_500);
		assertThatThrownBy(() -> corregida.conKilometraje(-5)).satisfies(e -> assertCampo(e, "kilometraje"));
	}

	@Test
	void el_odometro_no_puede_bajar_respecto_a_una_lectura_anterior() {
		LecturaKilometraje nueva = nueva(LocalDate.of(2026, 11, 14), 107_000);
		List<LecturaKilometraje> historial = List.of(existente(1, LocalDate.of(2026, 11, 7), 107_500));

		assertThatThrownBy(() -> nueva.verificarCoherenciaDeOdometro(historial)).satisfies(e -> assertCampo(e, "kilometraje"));
	}

	@Test
	void el_odometro_no_puede_superar_el_de_una_lectura_posterior() {
		LecturaKilometraje nueva = nueva(LocalDate.of(2026, 11, 7), 108_000);
		List<LecturaKilometraje> historial = List.of(existente(1, LocalDate.of(2026, 11, 14), 107_500));

		assertThatThrownBy(() -> nueva.verificarCoherenciaDeOdometro(historial)).satisfies(e -> assertCampo(e, "kilometraje"));
	}

	@Test
	void ignora_la_lectura_de_la_misma_fecha_porque_es_la_que_se_reemplaza() {
		LecturaKilometraje correccion = nueva(LocalDate.of(2026, 11, 14), 107_500);
		List<LecturaKilometraje> historial = List.of(existente(1, LocalDate.of(2026, 11, 7), 107_000),
				existente(2, LocalDate.of(2026, 11, 14), 190_000));

		assertThatCode(() -> correccion.verificarCoherenciaDeOdometro(historial)).doesNotThrowAnyException();
		assertThatCode(() -> correccion.verificarCoherenciaDeOdometro(List.of())).doesNotThrowAnyException();
	}

	@Test
	void acepta_lecturas_con_el_mismo_kilometraje_en_fechas_distintas() {
		LecturaKilometraje nueva = nueva(LocalDate.of(2026, 11, 14), 107_000);
		List<LecturaKilometraje> historial = List.of(existente(1, LocalDate.of(2026, 11, 7), 107_000),
				existente(2, LocalDate.of(2026, 11, 21), 107_000));

		assertThatCode(() -> nueva.verificarCoherenciaDeOdometro(historial)).doesNotThrowAnyException();
	}

	@Test
	void no_puede_marcar_menos_km_que_el_mantenimiento_si_es_de_igual_o_posterior_fecha() {
		LocalDate fechaMantenimiento = LocalDate.of(2026, 11, 1);

		assertThatThrownBy(() -> nueva(LocalDate.of(2026, 11, 14), 99_999).verificarContraMantenimiento(fechaMantenimiento, 100_000))
			.satisfies(e -> assertCampo(e, "kilometraje"));
		assertThatThrownBy(() -> nueva(fechaMantenimiento, 99_999).verificarContraMantenimiento(fechaMantenimiento, 100_000))
			.satisfies(e -> assertCampo(e, "kilometraje"));
		assertThatCode(() -> nueva(LocalDate.of(2026, 11, 14), 100_000).verificarContraMantenimiento(fechaMantenimiento, 100_000))
			.doesNotThrowAnyException();
		assertThatCode(() -> nueva(LocalDate.of(2026, 10, 20), 98_000).verificarContraMantenimiento(fechaMantenimiento, 100_000))
			.doesNotThrowAnyException();
	}
}
