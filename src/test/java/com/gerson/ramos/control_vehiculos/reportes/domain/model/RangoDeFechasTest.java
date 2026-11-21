package com.gerson.ramos.control_vehiculos.reportes.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import com.gerson.ramos.control_vehiculos.shared.domain.ConflictoDeDatosException;
import com.gerson.ramos.control_vehiculos.shared.domain.ReglaDeNegocioException;

class RangoDeFechasTest {

	private static final LocalDate HOY = LocalDate.of(2026, 11, 21);

	private static void assertCampo(Throwable error, String campo) {
		assertThat(error).isInstanceOfSatisfying(ConflictoDeDatosException.class, e -> assertThat(e.campo()).isEqualTo(campo));
	}

	@Test
	void acepta_un_rango_normal_y_un_rango_de_un_solo_dia() {
		assertThat(new RangoDeFechas(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31)).dias()).isEqualTo(31);
		assertThat(new RangoDeFechas(HOY, HOY).dias()).isEqualTo(1);
	}

	@Test
	void rechaza_desde_posterior_a_hasta_junto_al_campo_desde() {
		assertThatThrownBy(() -> new RangoDeFechas(LocalDate.of(2026, 2, 1), LocalDate.of(2026, 1, 1)))
			.satisfies(e -> assertCampo(e, "desde"));
	}

	@Test
	void exige_ambas_fechas() {
		assertThatThrownBy(() -> new RangoDeFechas(null, HOY)).isInstanceOf(ReglaDeNegocioException.class);
		assertThatThrownBy(() -> new RangoDeFechas(HOY, null)).isInstanceOf(ReglaDeNegocioException.class);
	}

	@Test
	void el_periodo_puede_ser_de_hasta_cinco_anos_y_no_mas() {
		LocalDate hasta = LocalDate.of(2026, 11, 21);

		assertThat(new RangoDeFechas(hasta.minusDays(RangoDeFechas.MAXIMO_DIAS - 1), hasta).dias()).isEqualTo(RangoDeFechas.MAXIMO_DIAS);
		assertThatThrownBy(() -> new RangoDeFechas(hasta.minusDays(RangoDeFechas.MAXIMO_DIAS), hasta))
			.satisfies(e -> assertCampo(e, "desde"))
			.hasMessageContaining("5 años");
	}

	@Test
	void la_fecha_final_no_puede_ser_futura() {
		RangoDeFechas hastaManana = new RangoDeFechas(HOY, HOY.plusDays(1));

		assertThatThrownBy(() -> hastaManana.verificarNoFutura(HOY)).satisfies(e -> assertCampo(e, "hasta"));
		assertThatCode(() -> new RangoDeFechas(HOY.minusDays(5), HOY).verificarNoFutura(HOY)).doesNotThrowAnyException();
	}
}
