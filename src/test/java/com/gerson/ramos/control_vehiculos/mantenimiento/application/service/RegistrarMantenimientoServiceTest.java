package com.gerson.ramos.control_vehiculos.mantenimiento.application.service;

import static com.gerson.ramos.control_vehiculos.mantenimiento.application.service.EscenarioMantenimiento.HOY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import com.gerson.ramos.control_vehiculos.mantenimiento.application.port.in.RegistrarMantenimientoComando;
import com.gerson.ramos.control_vehiculos.mantenimiento.domain.model.Mantenimiento;
import com.gerson.ramos.control_vehiculos.shared.domain.ConflictoDeDatosException;
import com.gerson.ramos.control_vehiculos.shared.domain.RecursoNoEncontradoException;
import com.gerson.ramos.control_vehiculos.shared.domain.ReglaDeNegocioException;
import com.gerson.ramos.control_vehiculos.vehiculo.domain.model.CodigoEquipo;
import com.gerson.ramos.control_vehiculos.vehiculo.domain.model.Placa;
import com.gerson.ramos.control_vehiculos.vehiculo.domain.model.Vehiculo;

class RegistrarMantenimientoServiceTest {

	private final EscenarioMantenimiento escenario = new EscenarioMantenimiento();

	private static void assertCampo(Throwable error, String campo) {
		assertThat(error).isInstanceOfSatisfying(ConflictoDeDatosException.class, e -> assertThat(e.campo()).isEqualTo(campo));
	}

	@Test
	void registra_el_mantenimiento_y_actualiza_la_ultima_rutina_del_vehiculo() {
		Mantenimiento guardado = escenario.registrar.registrar(new RegistrarMantenimientoComando(escenario.idVehiculo(),
				LocalDate.of(2026, 10, 1), 100_000, escenario.rutina10k.id().valor(), "  cambio de aceite "));

		assertThat(guardado.id()).isNotNull();
		assertThat(guardado.kilometraje()).isEqualTo(100_000);
		assertThat(guardado.observaciones()).isEqualTo("cambio de aceite");
		assertThat(escenario.vehiculos.buscarPorId(escenario.idVehiculo()).ultimaRutinaId()).isEqualTo(escenario.rutina10k.id());
	}

	@Test
	void permite_registrar_con_fecha_de_hoy() {
		Mantenimiento guardado = escenario.registrarMantenimiento(HOY, 100_000, escenario.rutina10k);

		assertThat(guardado.fecha()).isEqualTo(HOY);
	}

	@Test
	void rechaza_una_fecha_futura() {
		assertThatThrownBy(() -> escenario.registrarMantenimiento(HOY.plusDays(1), 100_000, escenario.rutina10k))
			.satisfies(e -> assertCampo(e, "fecha"));
		assertThat(escenario.mantenimientosRepo.historialDe(escenario.vehiculoActivo.id())).isEmpty();
	}

	@Test
	void rechaza_un_vehiculo_inactivo() {
		RegistrarMantenimientoComando comando = new RegistrarMantenimientoComando(escenario.vehiculoInactivo.id().valor(),
				HOY, 100_000, escenario.rutina10k.id().valor(), null);

		assertThatThrownBy(() -> escenario.registrar.registrar(comando))
			.isInstanceOf(ReglaDeNegocioException.class)
			.isNotInstanceOf(ConflictoDeDatosException.class);
	}

	@Test
	void falla_si_el_vehiculo_no_existe() {
		RegistrarMantenimientoComando comando = new RegistrarMantenimientoComando(999, HOY, 100_000,
				escenario.rutina10k.id().valor(), null);

		assertThatThrownBy(() -> escenario.registrar.registrar(comando)).isInstanceOf(RecursoNoEncontradoException.class);
	}

	@Test
	void rechaza_una_rutina_inactiva_o_inexistente() {
		assertThatThrownBy(() -> escenario.registrarMantenimiento(HOY, 100_000, escenario.rutina30kInactiva))
			.satisfies(e -> assertCampo(e, "rutinaId"));
		assertThatThrownBy(() -> escenario.registrar.registrar(
				new RegistrarMantenimientoComando(escenario.idVehiculo(), HOY, 100_000, 999, null)))
			.satisfies(e -> assertCampo(e, "rutinaId"));
	}

	@Test
	void rechaza_un_kilometraje_menor_al_de_un_mantenimiento_anterior() {
		escenario.registrarMantenimiento(LocalDate.of(2026, 5, 1), 100_000, escenario.rutina10k);

		assertThatThrownBy(() -> escenario.registrarMantenimiento(LocalDate.of(2026, 10, 1), 99_999, escenario.rutina20k))
			.satisfies(e -> assertCampo(e, "kilometraje"));
	}

	@Test
	void rechaza_un_kilometraje_mayor_al_de_un_mantenimiento_posterior() {
		escenario.registrarMantenimiento(LocalDate.of(2026, 10, 1), 110_000, escenario.rutina20k);

		assertThatThrownBy(() -> escenario.registrarMantenimiento(LocalDate.of(2026, 5, 1), 110_001, escenario.rutina10k))
			.satisfies(e -> assertCampo(e, "kilometraje"));
	}

	@Test
	void acepta_un_registro_retroactivo_coherente_sin_cambiar_la_ultima_rutina() {
		escenario.registrarMantenimiento(LocalDate.of(2026, 10, 1), 110_000, escenario.rutina20k);

		escenario.registrarMantenimiento(LocalDate.of(2026, 5, 1), 100_000, escenario.rutina10k);

		assertThat(escenario.mantenimientosRepo.historialDe(escenario.vehiculoActivo.id())).hasSize(2);
		assertThat(escenario.vehiculos.buscarPorId(escenario.idVehiculo()).ultimaRutinaId()).isEqualTo(escenario.rutina20k.id());
	}

	@Test
	void el_mantenimiento_mas_reciente_actualiza_la_ultima_rutina() {
		escenario.registrarMantenimiento(LocalDate.of(2026, 5, 1), 100_000, escenario.rutina10k);
		escenario.registrarMantenimiento(LocalDate.of(2026, 10, 1), 110_000, escenario.rutina20k);

		assertThat(escenario.vehiculos.buscarPorId(escenario.idVehiculo()).ultimaRutinaId()).isEqualTo(escenario.rutina20k.id());
	}

	@Test
	void permite_misma_fecha_y_mismo_kilometraje() {
		escenario.registrarMantenimiento(LocalDate.of(2026, 10, 1), 110_000, escenario.rutina10k);

		Mantenimiento segundo = escenario.registrarMantenimiento(LocalDate.of(2026, 10, 1), 110_000, escenario.rutina20k);

		assertThat(segundo.id()).isNotNull();
		assertThat(escenario.vehiculos.buscarPorId(escenario.idVehiculo()).ultimaRutinaId()).isEqualTo(escenario.rutina20k.id());
	}

	@Test
	void el_historial_de_otro_vehiculo_no_afecta_la_coherencia() {
		Vehiculo otro = escenario.vehiculosRepo.guardar(Vehiculo.registrar(new Placa("DEF-456"), new CodigoEquipo("EQ-003"),
				escenario.vehiculoActivo.localidadId(), escenario.vehiculoActivo.tipoUsoId()));
		escenario.registrarMantenimiento(LocalDate.of(2026, 10, 1), 500_000, escenario.rutina10k);

		Mantenimiento delOtro = escenario.registrar.registrar(new RegistrarMantenimientoComando(otro.id().valor(),
				LocalDate.of(2026, 10, 1), 50_000, escenario.rutina10k.id().valor(), null));

		assertThat(delOtro.vehiculoId()).isEqualTo(otro.id());
		assertThat(escenario.mantenimientosRepo.historialDe(escenario.vehiculoActivo.id())).hasSize(1);
	}
}
