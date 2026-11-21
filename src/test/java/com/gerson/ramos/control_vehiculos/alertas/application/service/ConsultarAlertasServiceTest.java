package com.gerson.ramos.control_vehiculos.alertas.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

import com.gerson.ramos.control_vehiculos.alertas.application.port.in.AlertaDetalle;
import com.gerson.ramos.control_vehiculos.alertas.application.port.in.PanelDeAlertas;
import com.gerson.ramos.control_vehiculos.alertas.domain.model.Severidad;
import com.gerson.ramos.control_vehiculos.alertas.domain.model.TipoAlerta;
import com.gerson.ramos.control_vehiculos.configuracion.application.service.GestionarParametrosAlertasService;
import com.gerson.ramos.control_vehiculos.kilometraje.application.port.in.FilaSeguimiento;
import com.gerson.ramos.control_vehiculos.testsupport.InMemoryParametrosAlertasRepository;
import com.gerson.ramos.control_vehiculos.testsupport.SeguimientoEnMemoria;

/** Alertas a partir del seguimiento simulado. Umbrales por defecto: 1,000 km, 30 días, 7 días. */
class ConsultarAlertasServiceTest {

	private final SeguimientoEnMemoria seguimiento = new SeguimientoEnMemoria();
	private final GestionarParametrosAlertasService parametros = new GestionarParametrosAlertasService(new InMemoryParametrosAlertasRepository());
	private final ConsultarAlertasService servicio = new ConsultarAlertasService(seguimiento, parametros);

	/**
	 * @param programada si el vehículo tiene mantenimientos registrados
	 * @param diasSinLectura días desde su última lectura; nulo si nunca tuvo
	 */
	private static FilaSeguimiento fila(long id, String placa, @Nullable Long kmRestantes, @Nullable Long diasRestantes,
			boolean programada, @Nullable Long diasSinLectura, boolean pendiente) {
		return new FilaSeguimiento(id, placa, "EQ-" + id, "San Pedro Sula", programada ? LocalDate.of(2026, 5, 1) : null,
				programada ? Integer.valueOf(100_000) : null, programada ? Long.valueOf(110_000) : null, diasRestantes,
				diasSinLectura == null ? null : LocalDate.of(2026, 11, 19), diasSinLectura == null ? null : Integer.valueOf(107_500),
				diasSinLectura, programada ? Long.valueOf(107_500) : null, kmRestantes, pendiente);
	}

	private static FilaSeguimiento alDia(long id, String placa) {
		return fila(id, placa, 5_000L, 100L, true, 2L, false);
	}

	private AlertaDetalle unica() {
		PanelDeAlertas panel = servicio.consultar();
		assertThat(panel.alertas()).hasSize(1);
		return panel.alertas().get(0);
	}

	@Test
	void sin_vehiculos_o_con_todos_al_dia_no_hay_alertas() {
		assertThat(servicio.consultar().alertas()).isEmpty();

		seguimiento.con(alDia(1, "ABC-123"));

		PanelDeAlertas panel = servicio.consultar();
		assertThat(panel.alertas()).isEmpty();
		assertThat(panel.resumen().total()).isZero();
		assertThat(panel.resumen().activas()).isZero();
	}

	@Test
	void ordena_vencidas_primero_luego_proximas_e_informativas_y_cada_grupo_por_placa() {
		seguimiento
			.con(fila(1, "ZZZ-111", null, null, false, 2L, false)) // sin mantenimiento (informativa)
			.con(fila(2, "BBB-222", 800L, 100L, true, 2L, false)) // próxima
			.con(fila(3, "DDD-444", -10L, 100L, true, 2L, false)) // vencida
			.con(fila(4, "AAA-111", 700L, 100L, true, 2L, false)) // próxima
			.con(fila(5, "CCC-333", 5_000L, -3L, true, 2L, false)); // vencida

		PanelDeAlertas panel = servicio.consultar();

		assertThat(panel.alertas()).extracting(AlertaDetalle::placa).containsExactly("CCC-333", "DDD-444", "AAA-111", "BBB-222", "ZZZ-111");
		assertThat(panel.alertas()).extracting(AlertaDetalle::severidad)
			.containsExactly(Severidad.VENCIDA, Severidad.VENCIDA, Severidad.PROXIMA, Severidad.PROXIMA, Severidad.INFORMATIVA);
		assertThat(panel.resumen().vencidas()).isEqualTo(2);
		assertThat(panel.resumen().proximas()).isEqualTo(2);
		assertThat(panel.resumen().informativas()).isEqualTo(1);
		assertThat(panel.resumen().activas()).isEqualTo(4);
		assertThat(panel.deSeveridad(Severidad.VENCIDA)).hasSize(2);
	}

	@Test
	void el_mensaje_de_vencido_explica_por_km_por_fecha_o_ambos() {
		seguimiento.con(fila(1, "ABC-123", -1_200L, -20L, true, 2L, false));
		assertThat(unica().mensaje()).isEqualTo("Vencido: se pasó por 1,200 km y venció hace 20 días");
		assertThat(unica().tipo()).isEqualTo(TipoAlerta.MANTENIMIENTO_VENCIDO);
	}

	@Test
	void el_mensaje_de_vencido_solo_por_km_o_solo_por_fecha() {
		SeguimientoEnMemoria soloKm = new SeguimientoEnMemoria().con(fila(1, "ABC-123", -50L, 100L, true, 2L, false));
		SeguimientoEnMemoria soloFecha = new SeguimientoEnMemoria().con(fila(1, "ABC-123", 5_000L, -1L, true, 2L, false));

		assertThat(new ConsultarAlertasService(soloKm, parametros).consultar().alertas().get(0).mensaje())
			.isEqualTo("Vencido: se pasó por 50 km");
		assertThat(new ConsultarAlertasService(soloFecha, parametros).consultar().alertas().get(0).mensaje())
			.isEqualTo("Vencido: venció hace 1 día");
	}

	@Test
	void el_mensaje_de_proximo_indica_km_y_dias_que_faltan() {
		seguimiento.con(fila(1, "ABC-123", 800L, 25L, true, 2L, false));

		AlertaDetalle alerta = unica();

		assertThat(alerta.mensaje()).isEqualTo("Próximo: faltan 800 km · faltan 25 días");
		assertThat(alerta.severidad()).isEqualTo(Severidad.PROXIMA);
		assertThat(alerta.kmRestantes()).isEqualTo(800L);
		assertThat(alerta.diasRestantes()).isEqualTo(25L);
	}

	@Test
	void el_mensaje_de_proximo_trata_hoy_y_un_dia() {
		SeguimientoEnMemoria hoy = new SeguimientoEnMemoria().con(fila(1, "ABC-123", 5_000L, 0L, true, 2L, false));
		SeguimientoEnMemoria manana = new SeguimientoEnMemoria().con(fila(1, "ABC-123", 5_000L, 1L, true, 2L, false));

		assertThat(new ConsultarAlertasService(hoy, parametros).consultar().alertas().get(0).mensaje()).isEqualTo("Próximo: vence hoy");
		assertThat(new ConsultarAlertasService(manana, parametros).consultar().alertas().get(0).mensaje()).isEqualTo("Próximo: faltan 1 día");
	}

	@Test
	void sin_mantenimientos_registrados_es_informativa() {
		seguimiento.con(fila(1, "ABC-123", null, null, false, 2L, false));

		AlertaDetalle alerta = unica();

		assertThat(alerta.tipo()).isEqualTo(TipoAlerta.SIN_MANTENIMIENTO);
		assertThat(alerta.severidad()).isEqualTo(Severidad.INFORMATIVA);
		assertThat(alerta.mensaje()).isEqualTo("Sin mantenimientos registrados: no se puede programar el siguiente");
		assertThat(alerta.kmRestantes()).isNull();
		assertThat(alerta.diasRestantes()).isNull();
	}

	@Test
	void la_lectura_pendiente_distingue_sin_lecturas_de_lectura_antigua() {
		SeguimientoEnMemoria sinLecturas = new SeguimientoEnMemoria().con(fila(1, "ABC-123", 5_000L, 100L, true, null, true));
		SeguimientoEnMemoria antigua = new SeguimientoEnMemoria().con(fila(1, "ABC-123", 5_000L, 100L, true, 9L, true));

		assertThat(new ConsultarAlertasService(sinLecturas, parametros).consultar().alertas().get(0).mensaje())
			.isEqualTo("Sin lecturas de kilometraje");
		assertThat(new ConsultarAlertasService(antigua, parametros).consultar().alertas().get(0).mensaje())
			.isEqualTo("Última lectura hace 9 días");
	}

	@Test
	void un_vehiculo_puede_tener_dos_alertas_la_urgente_primero() {
		seguimiento.con(fila(1, "ABC-123", -10L, 100L, true, 9L, true));

		List<AlertaDetalle> alertas = servicio.consultar().alertas();

		assertThat(alertas).extracting(AlertaDetalle::tipo).containsExactly(TipoAlerta.MANTENIMIENTO_VENCIDO, TipoAlerta.LECTURA_PENDIENTE);
	}

	@Test
	void la_insignia_cuenta_solo_vencidas_y_proximas() {
		seguimiento
			.con(fila(1, "AAA-111", -10L, 100L, true, 9L, true)) // vencida + lectura pendiente
			.con(fila(2, "BBB-222", 500L, 100L, true, 2L, false)) // próxima
			.con(fila(3, "CCC-333", null, null, false, 2L, false)); // informativa

		assertThat(servicio.consultar().resumen().activas()).isEqualTo(2);
		assertThat(servicio.consultar().resumen().total()).isEqualTo(4);
	}

	@Test
	void respeta_los_umbrales_configurados_y_los_expone() {
		seguimiento.con(fila(1, "ABC-123", 2_500L, 100L, true, 2L, false));

		assertThat(servicio.consultar().alertas()).isEmpty();

		parametros.actualizar(3_000, 45, 7);

		PanelDeAlertas panel = servicio.consultar();
		assertThat(panel.alertas()).hasSize(1);
		assertThat(panel.alertas().get(0).tipo()).isEqualTo(TipoAlerta.MANTENIMIENTO_PROXIMO);
		assertThat(panel.umbrales().umbralKm()).isEqualTo(3_000);
		assertThat(panel.umbrales().umbralDias()).isEqualTo(45);
	}

	@Test
	void la_alerta_trae_los_datos_del_vehiculo_para_actuar() {
		seguimiento.con(fila(7, "ABC-123", -10L, 100L, true, 2L, false));

		AlertaDetalle alerta = unica();

		assertThat(alerta.vehiculoId()).isEqualTo(7L);
		assertThat(alerta.placa()).isEqualTo("ABC-123");
		assertThat(alerta.codigoEquipo()).isEqualTo("EQ-7");
		assertThat(alerta.localidad()).isEqualTo("San Pedro Sula");
	}
}
