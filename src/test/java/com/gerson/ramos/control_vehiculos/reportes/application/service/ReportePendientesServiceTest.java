package com.gerson.ramos.control_vehiculos.reportes.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.gerson.ramos.control_vehiculos.alertas.domain.model.Severidad;
import com.gerson.ramos.control_vehiculos.reportes.application.port.in.EstadoPendiente;
import com.gerson.ramos.control_vehiculos.reportes.application.port.in.FilaPendiente;
import com.gerson.ramos.control_vehiculos.reportes.application.port.in.FiltroPendientes;
import com.gerson.ramos.control_vehiculos.reportes.application.port.in.ReportePendientes;
import com.gerson.ramos.control_vehiculos.vehiculo.domain.model.Vehiculo;

class ReportePendientesServiceTest {

	private final EscenarioReportes escenario = new EscenarioReportes();

	private Vehiculo a; // San Pedro, operativo: vencido por km y por fecha
	private Vehiculo b; // Tegucigalpa, operativo: próximo
	private Vehiculo c; // San Pedro, relevo: próximo

	@BeforeEach
	void preparar() {
		a = escenario.vehiculo("AAA-111", "EQ-1", escenario.sanPedro, escenario.operativo);
		b = escenario.vehiculo("BBB-222", "EQ-2", escenario.tegucigalpa, escenario.operativo);
		c = escenario.vehiculo("CCC-333", "EQ-3", escenario.sanPedro, escenario.relevo);
		Vehiculo alDia = escenario.vehiculo("DDD-444", "EQ-4", escenario.sanPedro, escenario.operativo);
		Vehiculo inactivo = escenario.vehiculoInactivo("ZZZ-999", "EQ-9", escenario.sanPedro, escenario.operativo);

		escenario.seguimientoDe(a, escenario.sanPedro, -100L, -5L, true);
		escenario.seguimientoDe(b, escenario.tegucigalpa, 800L, 20L, true);
		escenario.seguimientoDe(c, escenario.sanPedro, 500L, 10L, true);
		escenario.seguimientoDe(alDia, escenario.sanPedro, 5_000L, 100L, true);

		// Las alertas llegan ya ordenadas: vencidas, próximas, informativas.
		escenario.alertaDe(a, escenario.sanPedro, Severidad.VENCIDA, "Vencido: se pasó por 100 km y venció hace 5 días", -100L, -5L);
		escenario.alertaDe(inactivo, escenario.sanPedro, Severidad.VENCIDA, "Vencido (vehículo dado de baja)", -1L, -1L);
		escenario.alertaDe(b, escenario.tegucigalpa, Severidad.PROXIMA, "Próximo: faltan 800 km · faltan 20 días", 800L, 20L);
		escenario.alertaDe(c, escenario.sanPedro, Severidad.PROXIMA, "Próximo: faltan 500 km · faltan 10 días", 500L, 10L);
		escenario.alertaDe(alDia, escenario.sanPedro, Severidad.INFORMATIVA, "Última lectura hace 9 días", null, null);
	}

	private ReportePendientes generar(EstadoPendiente estado, Long localidadId, Long tipoUsoId) {
		return escenario.pendientes.generar(new FiltroPendientes(estado, localidadId, tipoUsoId));
	}

	@Test
	void incluye_solo_vencidos_y_proximos_de_vehiculos_activos_con_los_vencidos_primero() {
		ReportePendientes reporte = generar(EstadoPendiente.TODOS, null, null);

		assertThat(reporte.filas()).extracting(FilaPendiente::placa).containsExactly("AAA-111", "BBB-222", "CCC-333");
		assertThat(reporte.vencidos()).isEqualTo(1);
		assertThat(reporte.proximos()).isEqualTo(2);
	}

	@Test
	void cada_fila_trae_el_detalle_del_mantenimiento_y_la_fecha_de_referencia() {
		FilaPendiente fila = generar(EstadoPendiente.TODOS, null, null).filas().get(0);

		assertThat(fila.vencido()).isTrue();
		assertThat(fila.estado()).isEqualTo(Severidad.VENCIDA);
		assertThat(fila.motivo()).isEqualTo("Vencido: se pasó por 100 km y venció hace 5 días");
		assertThat(fila.localidad()).isEqualTo("San Pedro Sula");
		assertThat(fila.tipoUso()).isNotBlank();
		assertThat(fila.ultimoMantenimientoFecha()).isEqualTo(LocalDate.of(2026, 5, 1));
		assertThat(fila.ultimoMantenimientoKm()).isEqualTo(100_000);
		assertThat(fila.proximoKm()).isEqualTo(110_000L);
		assertThat(fila.kmActual()).isEqualTo(107_500L);
		assertThat(fila.kmRestantes()).isEqualTo(-100L);
		assertThat(fila.diasRestantes()).isEqualTo(-5L);
		assertThat(fila.fechaReferencia()).isEqualTo(EscenarioReportes.HOY.minusDays(5));
	}

	@Test
	void la_fecha_de_referencia_de_un_proximo_es_futura() {
		FilaPendiente fila = generar(EstadoPendiente.PROXIMOS, null, null).filas().get(0);

		assertThat(fila.fechaReferencia()).isEqualTo(EscenarioReportes.HOY.plusDays(20));
		assertThat(fila.vencido()).isFalse();
	}

	@Test
	void filtra_por_estado() {
		assertThat(generar(EstadoPendiente.VENCIDOS, null, null).filas()).extracting(FilaPendiente::placa).containsExactly("AAA-111");
		ReportePendientes proximos = generar(EstadoPendiente.PROXIMOS, null, null);
		assertThat(proximos.filas()).extracting(FilaPendiente::placa).containsExactly("BBB-222", "CCC-333");
		assertThat(proximos.vencidos()).isZero();
		assertThat(proximos.proximos()).isEqualTo(2);
	}

	@Test
	void filtra_por_localidad_y_ajusta_los_totales() {
		ReportePendientes reporte = generar(EstadoPendiente.TODOS, escenario.sanPedro.id().valor(), null);

		assertThat(reporte.filas()).extracting(FilaPendiente::placa).containsExactly("AAA-111", "CCC-333");
		assertThat(reporte.vencidos()).isEqualTo(1);
		assertThat(reporte.proximos()).isEqualTo(1);
	}

	@Test
	void filtra_por_tipo_de_uso() {
		ReportePendientes reporte = generar(EstadoPendiente.TODOS, null, escenario.relevo.id().valor());

		assertThat(reporte.filas()).extracting(FilaPendiente::placa).containsExactly("CCC-333");
	}

	@Test
	void combina_los_filtros() {
		ReportePendientes reporte = generar(EstadoPendiente.PROXIMOS, escenario.sanPedro.id().valor(), escenario.operativo.id().valor());

		assertThat(reporte.filas()).isEmpty();
		assertThat(reporte.vencidos()).isZero();
		assertThat(reporte.proximos()).isZero();
	}

	@Test
	void sin_alertas_el_reporte_esta_vacio() {
		EscenarioReportes vacio = new EscenarioReportes();

		ReportePendientes reporte = vacio.pendientes.generar(new FiltroPendientes(EstadoPendiente.TODOS, null, null));

		assertThat(reporte.filas()).isEmpty();
	}
}
