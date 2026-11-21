package com.gerson.ramos.control_vehiculos.mantenimiento.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.gerson.ramos.control_vehiculos.configuracion.domain.model.RutinaMantenimientoId;
import com.gerson.ramos.control_vehiculos.shared.domain.ConflictoDeDatosException;
import com.gerson.ramos.control_vehiculos.shared.domain.ReglaDeNegocioException;
import com.gerson.ramos.control_vehiculos.vehiculo.domain.model.VehiculoId;

class MantenimientoTest {

	private static final LocalDate HOY = LocalDate.of(2026, 11, 21);
	private static final VehiculoId VEHICULO = new VehiculoId(1);
	private static final RutinaMantenimientoId RUTINA = new RutinaMantenimientoId(1);

	private static Mantenimiento nuevo(LocalDate fecha, int km) {
		return Mantenimiento.registrar(VEHICULO, RUTINA, fecha, km, null, HOY);
	}

	private static Mantenimiento existente(long id, LocalDate fecha, int km) {
		return Mantenimiento.reconstruir(new MantenimientoId(id), VEHICULO, RUTINA, fecha, km, null);
	}

	private static void assertCampo(Throwable error, String campo) {
		assertThat(error).isInstanceOfSatisfying(ConflictoDeDatosException.class, e -> assertThat(e.campo()).isEqualTo(campo));
	}

	@Test
	void registra_un_mantenimiento_nuevo_sin_id() {
		Mantenimiento m = Mantenimiento.registrar(VEHICULO, RUTINA, HOY, 100_000, "  cambio de aceite ", HOY);

		assertThat(m.id()).isNull();
		assertThat(m.fecha()).isEqualTo(HOY);
		assertThat(m.kilometraje()).isEqualTo(100_000);
		assertThat(m.observaciones()).isEqualTo("cambio de aceite");
	}

	@Test
	void las_observaciones_vacias_se_guardan_como_nulas() {
		assertThat(Mantenimiento.registrar(VEHICULO, RUTINA, HOY, 1, "   ", HOY).observaciones()).isNull();
		assertThat(Mantenimiento.registrar(VEHICULO, RUTINA, HOY, 1, null, HOY).observaciones()).isNull();
	}

	@Test
	void rechaza_observaciones_demasiado_largas() {
		String larga = "x".repeat(Mantenimiento.LONGITUD_MAXIMA_OBSERVACIONES + 1);

		assertThatThrownBy(() -> Mantenimiento.registrar(VEHICULO, RUTINA, HOY, 1, larga, HOY))
			.isInstanceOf(ReglaDeNegocioException.class);
	}

	@Test
	void rechaza_fecha_futura_y_kilometraje_negativo() {
		assertThatThrownBy(() -> nuevo(HOY.plusDays(1), 1)).satisfies(e -> assertCampo(e, "fecha"));
		assertThatThrownBy(() -> nuevo(HOY, -1)).satisfies(e -> assertCampo(e, "kilometraje"));
	}

	@Test
	void exige_los_datos_obligatorios() {
		assertThatThrownBy(() -> Mantenimiento.registrar(null, RUTINA, HOY, 1, null, HOY)).isInstanceOf(ReglaDeNegocioException.class);
		assertThatThrownBy(() -> Mantenimiento.registrar(VEHICULO, null, HOY, 1, null, HOY)).isInstanceOf(ReglaDeNegocioException.class);
		assertThatThrownBy(() -> Mantenimiento.registrar(VEHICULO, RUTINA, null, 1, null, HOY)).isInstanceOf(ReglaDeNegocioException.class);
	}

	@Test
	void el_odometro_no_puede_bajar_respecto_a_un_mantenimiento_anterior() {
		Mantenimiento nuevo = nuevo(LocalDate.of(2026, 10, 1), 99_000);
		List<Mantenimiento> historial = List.of(existente(1, LocalDate.of(2026, 5, 1), 100_000));

		assertThatThrownBy(() -> nuevo.verificarCoherenciaDeOdometro(historial)).satisfies(e -> assertCampo(e, "kilometraje"));
	}

	@Test
	void el_odometro_no_puede_superar_el_de_un_mantenimiento_posterior() {
		Mantenimiento nuevo = nuevo(LocalDate.of(2026, 5, 1), 120_000);
		List<Mantenimiento> historial = List.of(existente(1, LocalDate.of(2026, 10, 1), 110_000));

		assertThatThrownBy(() -> nuevo.verificarCoherenciaDeOdometro(historial)).satisfies(e -> assertCampo(e, "kilometraje"));
	}

	@Test
	void acepta_odometros_coherentes_incluso_iguales_o_en_la_misma_fecha() {
		Mantenimiento nuevo = nuevo(LocalDate.of(2026, 8, 1), 105_000);
		List<Mantenimiento> historial = List.of(existente(1, LocalDate.of(2026, 5, 1), 100_000),
				existente(2, LocalDate.of(2026, 10, 1), 110_000), existente(3, LocalDate.of(2026, 8, 1), 105_000));

		assertThatCode(() -> nuevo.verificarCoherenciaDeOdometro(historial)).doesNotThrowAnyException();
		assertThatCode(() -> nuevo.verificarCoherenciaDeOdometro(List.of())).doesNotThrowAnyException();
	}

	@Test
	void mas_reciente_se_decide_por_fecha_y_luego_por_orden_de_registro() {
		Mantenimiento viejo = existente(5, LocalDate.of(2026, 1, 1), 1);
		Mantenimiento nuevoPorFecha = existente(1, LocalDate.of(2026, 2, 1), 2);
		Mantenimiento mismaFechaMayorId = existente(9, LocalDate.of(2026, 1, 1), 1);

		assertThat(nuevoPorFecha.esMasRecienteQue(viejo)).isTrue();
		assertThat(viejo.esMasRecienteQue(nuevoPorFecha)).isFalse();
		assertThat(mismaFechaMayorId.esMasRecienteQue(viejo)).isTrue();
		assertThat(viejo.esMasRecienteQue(mismaFechaMayorId)).isFalse();
		assertThat(nuevo(LocalDate.of(2026, 1, 1), 1).esMasRecienteQue(viejo)).isTrue();
	}
}
