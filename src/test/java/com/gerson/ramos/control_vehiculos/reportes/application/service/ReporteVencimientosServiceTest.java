package com.gerson.ramos.control_vehiculos.reportes.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.gerson.ramos.control_vehiculos.reportes.application.port.in.FilaVencimiento;
import com.gerson.ramos.control_vehiculos.reportes.application.port.in.FiltroVencimientos;
import com.gerson.ramos.control_vehiculos.reportes.application.port.in.ReporteVencimientos;
import com.gerson.ramos.control_vehiculos.shared.domain.ConflictoDeDatosException;

class ReporteVencimientosServiceTest {

	private final EscenarioReportes escenario = new EscenarioReportes();

	@BeforeEach
	void preparar() {
		// placa: km restantes / días restantes
		var a = escenario.vehiculo("AAA-111", "EQ-1", escenario.sanPedro, escenario.operativo); //  300 km /  10 días: dentro por ambos
		var b = escenario.vehiculo("BBB-222", "EQ-2", escenario.tegucigalpa, escenario.operativo); // 5000 km / 45 días: dentro por días
		var c = escenario.vehiculo("CCC-333", "EQ-3", escenario.sanPedro, escenario.operativo); // 1500 km / 200 días: dentro por km
		var d = escenario.vehiculo("DDD-444", "EQ-4", escenario.sanPedro, escenario.operativo); // 9000 km / 200 días: fuera del horizonte
		var e = escenario.vehiculo("EEE-555", "EQ-5", escenario.tegucigalpa, escenario.operativo); //  -50 km / 100 días: vencido por km
		var f = escenario.vehiculo("FFF-666", "EQ-6", escenario.sanPedro, escenario.operativo); // 4000 km /  -3 días: vencido por fecha
		var g = escenario.vehiculo("GGG-777", "EQ-7", escenario.sanPedro, escenario.operativo); // sin mantenimientos
		escenario.vehiculoInactivo("ZZZ-999", "EQ-9", escenario.sanPedro, escenario.operativo);

		escenario.seguimientoDe(a, escenario.sanPedro, 300L, 10L, true);
		escenario.seguimientoDe(b, escenario.tegucigalpa, 5_000L, 45L, true);
		escenario.seguimientoDe(c, escenario.sanPedro, 1_500L, 200L, true);
		escenario.seguimientoDe(d, escenario.sanPedro, 9_000L, 200L, true);
		escenario.seguimientoDe(e, escenario.tegucigalpa, -50L, 100L, true);
		escenario.seguimientoDe(f, escenario.sanPedro, 4_000L, -3L, true);
		escenario.seguimientoDe(g, escenario.sanPedro, null, null, false);
	}

	private ReporteVencimientos generar(int dias, int km, boolean incluirVencidos, Long localidadId) {
		return escenario.vencimientos.generar(new FiltroVencimientos(dias, km, incluirVencidos, localidadId));
	}

	private static java.util.List<String> placas(ReporteVencimientos reporte) {
		return reporte.filas().stream().map(FilaVencimiento::placa).toList();
	}

	@Test
	void incluye_los_que_estan_dentro_del_horizonte_por_dias_o_por_km_ordenados_por_dias_restantes() {
		ReporteVencimientos reporte = generar(60, 2_000, true, null);

		assertThat(placas(reporte)).containsExactly("FFF-666", "AAA-111", "BBB-222", "EEE-555", "CCC-333");
		assertThat(reporte.vencidos()).isEqualTo(2);
		assertThat(reporte.porVencer()).isEqualTo(3);
	}

	@Test
	void omite_los_vehiculos_sin_mantenimientos_los_inactivos_y_los_fuera_del_horizonte() {
		assertThat(placas(generar(60, 2_000, true, null))).doesNotContain("GGG-777", "ZZZ-999", "DDD-444");
	}

	@Test
	void con_vencidos_omitidos_solo_quedan_los_por_vencer() {
		ReporteVencimientos reporte = generar(60, 2_000, false, null);

		assertThat(placas(reporte)).containsExactly("AAA-111", "BBB-222", "CCC-333");
		assertThat(reporte.vencidos()).isZero();
		assertThat(reporte.porVencer()).isEqualTo(3);
	}

	@Test
	void el_limite_exacto_del_horizonte_cuenta() {
		assertThat(placas(generar(10, 1, false, null))).containsExactly("AAA-111");
		assertThat(placas(generar(9, 299, false, null))).isEmpty();
		// AAA-111 (300 km) entra con cualquiera de estos horizontes en km; CCC-333 (1,500 km) solo con 1,500 o más.
		assertThat(placas(generar(1, 1_500, false, null))).containsExactly("AAA-111", "CCC-333");
		assertThat(placas(generar(1, 1_499, false, null))).containsExactly("AAA-111");
	}

	@Test
	void horizontes_pequenos_dejan_solo_los_vencidos_y_los_muy_cercanos() {
		ReporteVencimientos reporte = generar(5, 100, true, null);

		assertThat(placas(reporte)).containsExactly("FFF-666", "EEE-555");
		assertThat(reporte.porVencer()).isZero();
	}

	@Test
	void filtra_por_localidad() {
		ReporteVencimientos reporte = generar(60, 2_000, true, escenario.tegucigalpa.id().valor());

		assertThat(placas(reporte)).containsExactly("BBB-222", "EEE-555");
	}

	@Test
	void cada_fila_trae_las_fechas_y_los_km_calculados() {
		FilaVencimiento fila = generar(60, 2_000, true, null).filas().stream().filter(f -> f.placa().equals("AAA-111")).findFirst().orElseThrow();

		assertThat(fila.vencido()).isFalse();
		assertThat(fila.diasRestantes()).isEqualTo(10L);
		assertThat(fila.fechaReferencia()).isEqualTo(EscenarioReportes.HOY.plusDays(10));
		assertThat(fila.kmRestantes()).isEqualTo(300L);
		assertThat(fila.proximoKm()).isEqualTo(110_000L);
		assertThat(fila.ultimoMantenimientoKm()).isEqualTo(100_000);
		assertThat(fila.localidad()).isEqualTo("San Pedro Sula");
	}

	@Test
	void marca_como_vencido_tanto_por_km_como_por_fecha() {
		var vencidos = generar(60, 2_000, true, null).filas().stream().filter(FilaVencimiento::vencido).map(FilaVencimiento::placa).toList();

		assertThat(vencidos).containsExactlyInAnyOrder("EEE-555", "FFF-666");
	}

	@Test
	void rechaza_horizontes_no_positivos_junto_al_campo() {
		assertThatThrownBy(() -> generar(0, 2_000, true, null))
			.isInstanceOfSatisfying(ConflictoDeDatosException.class, e -> assertThat(e.campo()).isEqualTo("horizonteDias"));
		assertThatThrownBy(() -> generar(60, -1, true, null))
			.isInstanceOfSatisfying(ConflictoDeDatosException.class, e -> assertThat(e.campo()).isEqualTo("horizonteKm"));
	}

	@Test
	void rechaza_horizontes_por_encima_de_los_topes_pero_acepta_los_maximos() {
		assertThatThrownBy(() -> generar(FiltroVencimientos.HORIZONTE_DIAS_MAXIMO + 1, 2_000, true, null))
			.isInstanceOfSatisfying(ConflictoDeDatosException.class, e -> assertThat(e.campo()).isEqualTo("horizonteDias"));
		assertThatThrownBy(() -> generar(60, FiltroVencimientos.HORIZONTE_KM_MAXIMO + 1, true, null))
			.isInstanceOfSatisfying(ConflictoDeDatosException.class, e -> assertThat(e.campo()).isEqualTo("horizonteKm"));

		assertThat(generar(FiltroVencimientos.HORIZONTE_DIAS_MAXIMO, FiltroVencimientos.HORIZONTE_KM_MAXIMO, true, null).filas())
			.isNotEmpty();
	}

	@Test
	void omite_en_lugar_de_fallar_las_filas_con_la_programacion_incompleta() {
		var incompleta = escenario.vehiculo("HHH-888", "EQ-8", escenario.sanPedro, escenario.operativo);
		// Tiene próximo km (así que "tiene programación") pero faltan los días y los km restantes.
		escenario.seguimiento.con(new com.gerson.ramos.control_vehiculos.kilometraje.application.port.in.FilaSeguimiento(
				incompleta.id().valor(), "HHH-888", "EQ-8", "San Pedro Sula", null, null, 110_000L, null, null, null, null, null, null, false));

		ReporteVencimientos reporte = generar(60, 2_000, true, null);

		assertThat(placas(reporte)).doesNotContain("HHH-888");
		assertThat(reporte.filas()).isNotEmpty();
	}

	@Test
	void sin_seguimiento_el_reporte_esta_vacio() {
		EscenarioReportes vacio = new EscenarioReportes();

		ReporteVencimientos reporte = vacio.vencimientos.generar(new FiltroVencimientos(60, 2_000, true, null));

		assertThat(reporte.filas()).isEmpty();
	}
}
