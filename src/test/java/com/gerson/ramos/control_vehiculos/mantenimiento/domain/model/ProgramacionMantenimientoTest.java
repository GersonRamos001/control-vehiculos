package com.gerson.ramos.control_vehiculos.mantenimiento.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import com.gerson.ramos.control_vehiculos.configuracion.domain.model.ParametrosMantenimiento;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.RutinaMantenimientoId;
import com.gerson.ramos.control_vehiculos.vehiculo.domain.model.VehiculoId;

class ProgramacionMantenimientoTest {

	private static final ParametrosMantenimiento PARAMETROS = new ParametrosMantenimiento(10_000, 6);

	private static Mantenimiento mantenimiento(LocalDate fecha, int km) {
		return Mantenimiento.reconstruir(new MantenimientoId(1), new VehiculoId(1), new RutinaMantenimientoId(1), fecha, km, null);
	}

	@Test
	void suma_el_intervalo_al_kilometraje_como_en_el_ejemplo_del_excel() {
		// Último mantenimiento 100,000 km -> próximo 110,000 km.
		ProgramacionMantenimiento p = ProgramacionMantenimiento.calcular(mantenimiento(LocalDate.of(2026, 5, 1), 100_000),
				PARAMETROS, LocalDate.of(2026, 5, 2));

		assertThat(p.proximoKm()).isEqualTo(110_000L);
	}

	@Test
	void la_fecha_de_referencia_es_seis_meses_despues_y_cuenta_los_dias_restantes() {
		ProgramacionMantenimiento p = ProgramacionMantenimiento.calcular(mantenimiento(LocalDate.of(2026, 5, 1), 100_000),
				PARAMETROS, LocalDate.of(2026, 10, 1));

		assertThat(p.fechaReferencia()).isEqualTo(LocalDate.of(2026, 11, 1));
		assertThat(p.diasRestantes()).isEqualTo(31L);
		assertThat(p.vencidaPorFecha()).isFalse();
	}

	@Test
	void el_dia_de_la_fecha_de_referencia_quedan_cero_dias_y_no_esta_vencido() {
		ProgramacionMantenimiento p = ProgramacionMantenimiento.calcular(mantenimiento(LocalDate.of(2026, 5, 1), 100_000),
				PARAMETROS, LocalDate.of(2026, 11, 1));

		assertThat(p.diasRestantes()).isZero();
		assertThat(p.vencidaPorFecha()).isFalse();
	}

	@Test
	void pasada_la_fecha_de_referencia_los_dias_son_negativos() {
		ProgramacionMantenimiento p = ProgramacionMantenimiento.calcular(mantenimiento(LocalDate.of(2026, 5, 1), 100_000),
				PARAMETROS, LocalDate.of(2026, 11, 4));

		assertThat(p.diasRestantes()).isEqualTo(-3L);
		assertThat(p.vencidaPorFecha()).isTrue();
	}

	@Test
	void suma_meses_sin_pasarse_de_fin_de_mes() {
		ProgramacionMantenimiento p = ProgramacionMantenimiento.calcular(mantenimiento(LocalDate.of(2026, 8, 31), 1_000),
				PARAMETROS, LocalDate.of(2026, 9, 1));

		assertThat(p.fechaReferencia()).isEqualTo(LocalDate.of(2027, 2, 28));
	}

	@Test
	void usa_los_parametros_recibidos() {
		ProgramacionMantenimiento p = ProgramacionMantenimiento.calcular(mantenimiento(LocalDate.of(2026, 5, 1), 100_000),
				new ParametrosMantenimiento(5_000, 3), LocalDate.of(2026, 5, 1));

		assertThat(p.proximoKm()).isEqualTo(105_000L);
		assertThat(p.fechaReferencia()).isEqualTo(LocalDate.of(2026, 8, 1));
	}

	@Test
	void no_desborda_con_kilometrajes_altos() {
		ProgramacionMantenimiento p = ProgramacionMantenimiento.calcular(mantenimiento(LocalDate.of(2026, 5, 1), Integer.MAX_VALUE),
				PARAMETROS, LocalDate.of(2026, 5, 1));

		assertThat(p.proximoKm()).isEqualTo((long) Integer.MAX_VALUE + 10_000);
	}
}
