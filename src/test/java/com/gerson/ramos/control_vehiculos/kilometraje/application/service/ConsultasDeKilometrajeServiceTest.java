package com.gerson.ramos.control_vehiculos.kilometraje.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.gerson.ramos.control_vehiculos.kilometraje.application.port.in.FilaSeguimiento;
import com.gerson.ramos.control_vehiculos.kilometraje.application.port.in.LecturaDetalle;
import com.gerson.ramos.control_vehiculos.shared.domain.RecursoNoEncontradoException;

/** Historial con diferencias y seguimiento (reloj fijo en 2026-11-21). */
class ConsultasDeKilometrajeServiceTest {

	private final EscenarioKilometraje escenario = new EscenarioKilometraje();

	@Test
	void el_historial_calcula_recorridos_y_dias_desde_la_lectura_anterior() {
		escenario.registrarLectura(LocalDate.of(2026, 11, 1), 106_000);
		escenario.registrarLectura(LocalDate.of(2026, 11, 8), 106_900);
		escenario.registrarLectura(LocalDate.of(2026, 11, 20), 107_500);

		List<LecturaDetalle> historial = escenario.historial.historialDe(escenario.idActivo());

		assertThat(historial).extracting(LecturaDetalle::fecha)
			.containsExactly(LocalDate.of(2026, 11, 20), LocalDate.of(2026, 11, 8), LocalDate.of(2026, 11, 1));
		assertThat(historial).extracting(LecturaDetalle::kmRecorridos).containsExactly(600, 900, null);
		assertThat(historial).extracting(LecturaDetalle::diasDesdeAnterior).containsExactly(12L, 7L, null);
	}

	@Test
	void el_historial_falla_si_el_vehiculo_no_existe() {
		assertThatThrownBy(() -> escenario.historial.historialDe(999)).isInstanceOf(RecursoNoEncontradoException.class);
		assertThatThrownBy(() -> escenario.seguimiento.seguimientoDe(999)).isInstanceOf(RecursoNoEncontradoException.class);
	}

	@Test
	void reproduce_el_ejemplo_del_excel() {
		// Último mantenimiento 100,000 km -> próximo 110,000; lectura semanal 107,500 -> quedan 2,500.
		escenario.programacion.establecer(escenario.idActivo(), LocalDate.of(2026, 5, 1), 100_000, 110_000, 161);
		escenario.registrarLectura(LocalDate.of(2026, 11, 19), 107_500);

		FilaSeguimiento fila = escenario.seguimiento.seguimientoDe(escenario.idActivo());

		assertThat(fila.ultimoMantenimientoKm()).isEqualTo(100_000);
		assertThat(fila.proximoKm()).isEqualTo(110_000L);
		assertThat(fila.ultimaLecturaKm()).isEqualTo(107_500);
		assertThat(fila.kmActual()).isEqualTo(107_500L);
		assertThat(fila.kmRestantes()).isEqualTo(2_500L);
		assertThat(fila.vencidaPorKm()).isFalse();
		assertThat(fila.diasRestantes()).isEqualTo(161L);
	}

	@Test
	void si_la_lectura_pasa_del_proximo_km_los_restantes_son_negativos() {
		escenario.programacion.establecer(escenario.idActivo(), LocalDate.of(2026, 5, 1), 100_000, 110_000, 161);
		escenario.registrarLectura(LocalDate.of(2026, 11, 19), 111_200);

		FilaSeguimiento fila = escenario.seguimiento.seguimientoDe(escenario.idActivo());

		assertThat(fila.kmRestantes()).isEqualTo(-1_200L);
		assertThat(fila.vencidaPorKm()).isTrue();
	}

	@Test
	void sin_lecturas_el_km_actual_es_el_del_ultimo_mantenimiento_y_queda_pendiente() {
		escenario.programacion.establecer(escenario.idActivo(), LocalDate.of(2026, 11, 1), 100_000, 110_000, 161);

		FilaSeguimiento fila = escenario.seguimiento.seguimientoDe(escenario.idActivo());

		assertThat(fila.sinLectura()).isTrue();
		assertThat(fila.kmActual()).isEqualTo(100_000L);
		assertThat(fila.kmRestantes()).isEqualTo(10_000L);
		assertThat(fila.diasSinLectura()).isNull();
		assertThat(fila.pendiente()).isTrue();
	}

	@Test
	void el_km_actual_es_el_mayor_entre_la_lectura_y_el_mantenimiento() {
		// La lectura es de antes del mantenimiento y marca menos km: manda el del mantenimiento.
		escenario.registrarLectura(LocalDate.of(2026, 10, 20), 98_000);
		escenario.programacion.establecer(escenario.idActivo(), LocalDate.of(2026, 11, 1), 100_000, 110_000, 161);

		FilaSeguimiento fila = escenario.seguimiento.seguimientoDe(escenario.idActivo());

		assertThat(fila.kmActual()).isEqualTo(100_000L);
		assertThat(fila.kmRestantes()).isEqualTo(10_000L);
	}

	@Test
	void sin_mantenimientos_no_hay_programacion_ni_km_restantes() {
		escenario.registrarLectura(LocalDate.of(2026, 11, 19), 50_000);

		FilaSeguimiento fila = escenario.seguimiento.seguimientoDe(escenario.idActivo());

		assertThat(fila.sinProgramacion()).isTrue();
		assertThat(fila.kmActual()).isEqualTo(50_000L);
		assertThat(fila.kmRestantes()).isNull();
		assertThat(fila.vencidaPorKm()).isFalse();
	}

	@Test
	void un_vehiculo_sin_nada_no_tiene_km_actual() {
		FilaSeguimiento fila = escenario.seguimiento.seguimientoDe(escenario.idActivo());

		assertThat(fila.kmActual()).isNull();
		assertThat(fila.pendiente()).isTrue();
	}

	@Test
	void queda_pendiente_cuando_la_ultima_lectura_tiene_mas_de_7_dias() {
		escenario.registrarLectura(escenario.idActivo(), LocalDate.of(2026, 11, 14), 100_000); // hace 7 días
		escenario.registrarLectura(escenario.idOtro(), LocalDate.of(2026, 11, 13), 50_000); // hace 8 días

		FilaSeguimiento alDia = escenario.seguimiento.seguimientoDe(escenario.idActivo());
		FilaSeguimiento atrasado = escenario.seguimiento.seguimientoDe(escenario.idOtro());

		assertThat(alDia.diasSinLectura()).isEqualTo(7L);
		assertThat(alDia.pendiente()).isFalse();
		assertThat(atrasado.diasSinLectura()).isEqualTo(8L);
		assertThat(atrasado.pendiente()).isTrue();
	}

	@Test
	void el_umbral_de_lectura_pendiente_es_configurable() {
		escenario.registrarLectura(escenario.idActivo(), LocalDate.of(2026, 11, 14), 100_000); // hace 7 días
		escenario.parametrosAlertas.actualizar(1_000, 30, 3);

		assertThat(escenario.seguimiento.seguimientoDe(escenario.idActivo()).pendiente()).isTrue();

		escenario.parametrosAlertas.actualizar(1_000, 30, 10);

		assertThat(escenario.seguimiento.seguimientoDe(escenario.idActivo()).pendiente()).isFalse();
	}

	@Test
	void el_seguimiento_semanal_pone_primero_los_pendientes_y_omite_inactivos() {
		escenario.registrarLectura(escenario.idActivo(), LocalDate.of(2026, 11, 20), 100_000); // al día
		// otroActivo sin lecturas: pendiente, aunque su placa (DEF-456) sea posterior a ABC-123

		List<FilaSeguimiento> filas = escenario.seguimiento.seguimientoSemanal();

		assertThat(filas).extracting(FilaSeguimiento::placa).containsExactly("DEF-456", "ABC-123");
		assertThat(filas).extracting(FilaSeguimiento::pendiente).containsExactly(true, false);
		assertThat(filas).extracting(FilaSeguimiento::vehiculoId).doesNotContain(escenario.vehiculoInactivo.id().valor());
	}

	@Test
	void entre_vehiculos_del_mismo_estado_ordena_por_placa() {
		escenario.registrarLectura(escenario.idActivo(), LocalDate.of(2026, 11, 20), 100_000);
		escenario.registrarLectura(escenario.idOtro(), LocalDate.of(2026, 11, 20), 50_000);

		assertThat(escenario.seguimiento.seguimientoSemanal()).extracting(FilaSeguimiento::placa).containsExactly("ABC-123", "DEF-456");
	}
}
