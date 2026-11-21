package com.gerson.ramos.control_vehiculos.reportes.application.service;

import static com.gerson.ramos.control_vehiculos.reportes.application.service.EscenarioReportes.HOY;
import static com.gerson.ramos.control_vehiculos.reportes.application.service.EscenarioReportes.realizado;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.gerson.ramos.control_vehiculos.mantenimiento.application.port.in.MantenimientoRealizado;
import com.gerson.ramos.control_vehiculos.reportes.application.port.in.ConteoPorClave;
import com.gerson.ramos.control_vehiculos.reportes.application.port.in.FiltroRealizados;
import com.gerson.ramos.control_vehiculos.reportes.application.port.in.ReporteRealizados;
import com.gerson.ramos.control_vehiculos.shared.domain.ConflictoDeDatosException;

/** Con el reloj en 2026-11-21 el período por defecto es 2026-08-23 (90 días antes) a 2026-11-21. */
class ReporteRealizadosServiceTest {

	private final EscenarioReportes escenario = new EscenarioReportes();

	@BeforeEach
	void preparar() {
		escenario.realizados
			.con(realizado(1, "ABC-123", escenario.sanPedro, LocalDate.of(2026, 11, 10), 100_000, 1, "10K", "cambio de aceite"))
			.con(realizado(2, "DEF-456", escenario.tegucigalpa, LocalDate.of(2026, 10, 1), 50_000, 2, "20K", null))
			.con(realizado(3, "ABC-123", escenario.sanPedro, LocalDate.of(2026, 8, 30), 90_000, 1, "10K", null))
			.con(realizado(4, "GHI-789", escenario.sanPedro, LocalDate.of(2026, 8, 22), 70_000, 1, "10K", "fuera del período por defecto"))
			.con(realizado(5, "DEF-456", escenario.tegucigalpa, LocalDate.of(2026, 8, 23), 40_000, 1, "10K", "justo en el límite"));
	}

	private ReporteRealizados generar(LocalDate desde, LocalDate hasta, Long localidadId, Long rutinaId, String placa) {
		return escenario.reporteRealizados.generar(new FiltroRealizados(desde, hasta, localidadId, rutinaId, placa));
	}

	private static java.util.List<Long> ids(ReporteRealizados reporte) {
		return reporte.filas().stream().map(MantenimientoRealizado::id).toList();
	}

	@Test
	void sin_fechas_usa_los_ultimos_90_dias_hasta_hoy_con_ambos_limites_inclusivos() {
		ReporteRealizados reporte = generar(null, null, null, null, null);

		assertThat(reporte.rango().desde()).isEqualTo(LocalDate.of(2026, 8, 23));
		assertThat(reporte.rango().hasta()).isEqualTo(HOY);
		assertThat(ids(reporte)).containsExactly(1L, 2L, 3L, 5L);
		assertThat(reporte.total()).isEqualTo(4);
		assertThat(escenario.realizados.ultimoDesde()).isEqualTo(LocalDate.of(2026, 8, 23));
		assertThat(escenario.realizados.ultimoHasta()).isEqualTo(HOY);
	}

	@Test
	void solo_con_hasta_el_periodo_por_defecto_cuenta_90_dias_hacia_atras_desde_esa_fecha() {
		ReporteRealizados reporte = generar(null, LocalDate.of(2026, 10, 31), null, null, null);

		assertThat(reporte.rango().desde()).isEqualTo(LocalDate.of(2026, 8, 2));
		assertThat(ids(reporte)).containsExactly(2L, 3L, 5L, 4L); // del más reciente al más antiguo: 10-01, 08-30, 08-23, 08-22
	}

	@Test
	void solo_con_desde_llega_hasta_hoy() {
		ReporteRealizados reporte = generar(LocalDate.of(2026, 10, 1), null, null, null, null);

		assertThat(reporte.rango().hasta()).isEqualTo(HOY);
		assertThat(ids(reporte)).containsExactly(1L, 2L);
	}

	@Test
	void un_rango_explicito_se_respeta() {
		ReporteRealizados reporte = generar(LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 30), null, null, null);

		assertThat(ids(reporte)).containsExactly(3L, 5L, 4L); // 08-30, 08-23, 08-22
	}

	@Test
	void filtra_por_localidad() {
		assertThat(ids(generar(null, null, escenario.sanPedro.id().valor(), null, null))).containsExactly(1L, 3L);
		assertThat(ids(generar(null, null, escenario.tegucigalpa.id().valor(), null, null))).containsExactly(2L, 5L);
	}

	@Test
	void filtra_por_rutina() {
		assertThat(ids(generar(null, null, null, 2L, null))).containsExactly(2L);
		assertThat(ids(generar(null, null, null, 1L, null))).containsExactly(1L, 3L, 5L);
	}

	@Test
	void filtra_la_placa_por_coincidencia_parcial_sin_distinguir_mayusculas() {
		assertThat(ids(generar(null, null, null, null, "abc"))).containsExactly(1L, 3L);
		assertThat(ids(generar(null, null, null, null, " -456 "))).containsExactly(2L, 5L);
		assertThat(ids(generar(null, null, null, null, "no-existe"))).isEmpty();
		assertThat(ids(generar(null, null, null, null, "   "))).containsExactly(1L, 2L, 3L, 5L);
	}

	@Test
	void combina_los_filtros() {
		ReporteRealizados reporte = generar(null, null, escenario.sanPedro.id().valor(), 1L, "abc");

		assertThat(ids(reporte)).containsExactly(1L, 3L);
	}

	@Test
	void desglosa_por_rutina_y_por_localidad_de_mayor_a_menor_cantidad() {
		ReporteRealizados reporte = generar(null, null, null, null, null);

		assertThat(reporte.porRutina()).containsExactly(new ConteoPorClave("10K", 3), new ConteoPorClave("20K", 1));
		// Empate (2 y 2): se ordena por nombre.
		assertThat(reporte.porLocalidad()).containsExactly(new ConteoPorClave("San Pedro Sula", 2), new ConteoPorClave("Tegucigalpa", 2));
	}

	@Test
	void el_desglose_respeta_los_filtros() {
		ReporteRealizados reporte = generar(null, null, escenario.tegucigalpa.id().valor(), null, null);

		assertThat(reporte.porRutina()).containsExactly(new ConteoPorClave("10K", 1), new ConteoPorClave("20K", 1));
		assertThat(reporte.porLocalidad()).containsExactly(new ConteoPorClave("Tegucigalpa", 2));
	}

	@Test
	void un_periodo_sin_mantenimientos_da_un_reporte_vacio() {
		ReporteRealizados reporte = generar(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31), null, null, null);

		assertThat(reporte.filas()).isEmpty();
		assertThat(reporte.total()).isZero();
		assertThat(reporte.porRutina()).isEmpty();
		assertThat(reporte.porLocalidad()).isEmpty();
	}

	@Test
	void rechaza_un_rango_invertido_junto_al_campo_desde() {
		assertThatThrownBy(() -> generar(LocalDate.of(2026, 11, 1), LocalDate.of(2026, 10, 1), null, null, null))
			.isInstanceOfSatisfying(ConflictoDeDatosException.class, e -> assertThat(e.campo()).isEqualTo("desde"));
	}

	@Test
	void rechaza_una_fecha_final_futura_junto_al_campo_hasta() {
		assertThatThrownBy(() -> generar(null, HOY.plusDays(1), null, null, null))
			.isInstanceOfSatisfying(ConflictoDeDatosException.class, e -> assertThat(e.campo()).isEqualTo("hasta"));
	}

	@Test
	void rechaza_un_periodo_de_mas_de_cinco_anos_pero_acepta_el_maximo() {
		assertThat(generar(HOY.minusDays(1_826), HOY, null, null, null).rango().dias()).isEqualTo(1_827);
		assertThatThrownBy(() -> generar(HOY.minusDays(1_827), HOY, null, null, null))
			.isInstanceOfSatisfying(ConflictoDeDatosException.class, e -> assertThat(e.campo()).isEqualTo("desde"));
	}

	@Test
	void hasta_hoy_es_valido() {
		assertThat(generar(null, HOY, null, null, null).rango().hasta()).isEqualTo(HOY);
	}
}
