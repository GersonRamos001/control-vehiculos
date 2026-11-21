package com.gerson.ramos.control_vehiculos.alertas.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.gerson.ramos.control_vehiculos.configuracion.domain.model.ParametrosAlertas;
import com.gerson.ramos.control_vehiculos.shared.domain.ReglaDeNegocioException;

/** Umbrales por defecto: 1,000 km, 30 días y 7 días de lectura pendiente. */
class ReglasDeAlertaTest {

	private static final ParametrosAlertas UMBRALES = new ParametrosAlertas(1_000, 30, 7);

	private static List<Alerta> evaluar(Long kmRestantes, Long diasRestantes, boolean lecturaPendiente) {
		return ReglasDeAlerta.evaluar(new SituacionDeMantenimiento(kmRestantes, diasRestantes, false, lecturaPendiente), UMBRALES);
	}

	@Test
	void un_vehiculo_al_dia_no_tiene_alertas() {
		assertThat(evaluar(5_000L, 100L, false)).isEmpty();
	}

	@Test
	void vencido_por_kilometraje() {
		assertThat(evaluar(-1L, 100L, false)).containsExactly(new Alerta(TipoAlerta.MANTENIMIENTO_VENCIDO, true, false));
	}

	@Test
	void vencido_por_fecha() {
		assertThat(evaluar(5_000L, -1L, false)).containsExactly(new Alerta(TipoAlerta.MANTENIMIENTO_VENCIDO, false, true));
	}

	@Test
	void vencido_por_ambos_criterios() {
		assertThat(evaluar(-1_200L, -20L, false)).containsExactly(new Alerta(TipoAlerta.MANTENIMIENTO_VENCIDO, true, true));
	}

	@Test
	void si_esta_vencido_por_un_criterio_no_se_agrega_proximo_por_el_otro() {
		assertThat(evaluar(-5L, 10L, false)).containsExactly(new Alerta(TipoAlerta.MANTENIMIENTO_VENCIDO, true, false));
	}

	@Test
	void el_limite_exacto_del_umbral_de_km_cuenta_como_proximo() {
		assertThat(evaluar(1_000L, 100L, false)).containsExactly(new Alerta(TipoAlerta.MANTENIMIENTO_PROXIMO, true, false));
		assertThat(evaluar(1_001L, 100L, false)).isEmpty();
	}

	@Test
	void el_limite_exacto_del_umbral_de_dias_cuenta_como_proximo() {
		assertThat(evaluar(5_000L, 30L, false)).containsExactly(new Alerta(TipoAlerta.MANTENIMIENTO_PROXIMO, false, true));
		assertThat(evaluar(5_000L, 31L, false)).isEmpty();
	}

	@Test
	void proximo_por_ambos_criterios() {
		assertThat(evaluar(800L, 25L, false)).containsExactly(new Alerta(TipoAlerta.MANTENIMIENTO_PROXIMO, true, true));
	}

	@Test
	void cero_km_o_cero_dias_restantes_es_proximo_y_no_vencido() {
		assertThat(evaluar(0L, 100L, false)).containsExactly(new Alerta(TipoAlerta.MANTENIMIENTO_PROXIMO, true, false));
		assertThat(evaluar(5_000L, 0L, false)).containsExactly(new Alerta(TipoAlerta.MANTENIMIENTO_PROXIMO, false, true));
	}

	@Test
	void sin_programacion_solo_hay_alerta_informativa_de_falta_de_mantenimiento() {
		List<Alerta> alertas = ReglasDeAlerta.evaluar(new SituacionDeMantenimiento(null, null, true, false), UMBRALES);

		assertThat(alertas).containsExactly(new Alerta(TipoAlerta.SIN_MANTENIMIENTO, false, false));
		assertThat(alertas.get(0).severidad()).isEqualTo(Severidad.INFORMATIVA);
	}

	@Test
	void la_lectura_pendiente_acompana_a_las_demas_alertas() {
		assertThat(evaluar(-1L, 100L, true)).containsExactly(new Alerta(TipoAlerta.MANTENIMIENTO_VENCIDO, true, false),
				new Alerta(TipoAlerta.LECTURA_PENDIENTE, false, false));
		assertThat(evaluar(800L, 100L, true)).containsExactly(new Alerta(TipoAlerta.MANTENIMIENTO_PROXIMO, true, false),
				new Alerta(TipoAlerta.LECTURA_PENDIENTE, false, false));
		assertThat(evaluar(5_000L, 100L, true)).containsExactly(new Alerta(TipoAlerta.LECTURA_PENDIENTE, false, false));
		assertThat(ReglasDeAlerta.evaluar(new SituacionDeMantenimiento(null, null, true, true), UMBRALES))
			.containsExactly(new Alerta(TipoAlerta.SIN_MANTENIMIENTO, false, false), new Alerta(TipoAlerta.LECTURA_PENDIENTE, false, false));
	}

	@Test
	void usa_los_umbrales_recibidos() {
		ParametrosAlertas estrictos = new ParametrosAlertas(3_000, 60, 7);
		SituacionDeMantenimiento situacion = new SituacionDeMantenimiento(2_500L, 100L, false, false);

		assertThat(ReglasDeAlerta.evaluar(situacion, UMBRALES)).isEmpty();
		assertThat(ReglasDeAlerta.evaluar(situacion, estrictos)).containsExactly(new Alerta(TipoAlerta.MANTENIMIENTO_PROXIMO, true, false));
	}

	@Test
	void sin_datos_de_km_ni_dias_en_un_vehiculo_programado_no_hay_alerta_de_mantenimiento() {
		assertThat(evaluar(null, null, false)).isEmpty();
	}

	@Test
	void la_severidad_sigue_al_tipo() {
		assertThat(TipoAlerta.MANTENIMIENTO_VENCIDO.severidad()).isEqualTo(Severidad.VENCIDA);
		assertThat(TipoAlerta.MANTENIMIENTO_PROXIMO.severidad()).isEqualTo(Severidad.PROXIMA);
		assertThat(TipoAlerta.SIN_MANTENIMIENTO.severidad()).isEqualTo(Severidad.INFORMATIVA);
		assertThat(TipoAlerta.LECTURA_PENDIENTE.severidad()).isEqualTo(Severidad.INFORMATIVA);
	}

	@Test
	void exige_la_situacion_y_los_umbrales() {
		assertThatThrownBy(() -> ReglasDeAlerta.evaluar(null, UMBRALES)).isInstanceOf(ReglaDeNegocioException.class);
		assertThatThrownBy(() -> ReglasDeAlerta.evaluar(new SituacionDeMantenimiento(1L, 1L, false, false), null))
			.isInstanceOf(ReglaDeNegocioException.class);
	}
}
