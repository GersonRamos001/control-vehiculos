package com.gerson.ramos.control_vehiculos.mantenimiento.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.gerson.ramos.control_vehiculos.mantenimiento.application.port.in.MantenimientoRealizado;
import com.gerson.ramos.control_vehiculos.mantenimiento.domain.model.Mantenimiento;
import com.gerson.ramos.control_vehiculos.shared.domain.ReglaDeNegocioException;
import com.gerson.ramos.control_vehiculos.vehiculo.domain.model.CodigoEquipo;
import com.gerson.ramos.control_vehiculos.vehiculo.domain.model.Placa;
import com.gerson.ramos.control_vehiculos.vehiculo.domain.model.Vehiculo;

class ConsultarMantenimientosRealizadosServiceTest {

	private final EscenarioMantenimiento escenario = new EscenarioMantenimiento();

	private Vehiculo otroVehiculoActivo() {
		return escenario.vehiculosRepo.guardar(Vehiculo.registrar(new Placa("DEF-456"), new CodigoEquipo("EQ-010"),
				escenario.vehiculoActivo.localidadId(), escenario.vehiculoActivo.tipoUsoId()));
	}

	@Test
	void devuelve_los_mantenimientos_del_rango_con_sus_datos_resueltos_del_mas_reciente_al_mas_antiguo() {
		escenario.registrarMantenimiento(LocalDate.of(2026, 3, 1), 90_000, escenario.rutina10k);
		escenario.registrarMantenimiento(LocalDate.of(2026, 9, 1), 100_000, escenario.rutina20k);

		List<MantenimientoRealizado> filas = escenario.realizados.entre(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));

		assertThat(filas).extracting(MantenimientoRealizado::fecha).containsExactly(LocalDate.of(2026, 9, 1), LocalDate.of(2026, 3, 1));
		MantenimientoRealizado ultimo = filas.get(0);
		assertThat(ultimo.placa()).isEqualTo("ABC-123");
		assertThat(ultimo.codigoEquipo()).isEqualTo("EQ-001");
		assertThat(ultimo.localidad()).isEqualTo("San Pedro Sula");
		assertThat(ultimo.rutina()).isEqualTo("20K");
		assertThat(ultimo.kilometraje()).isEqualTo(100_000);
		assertThat(ultimo.localidadId()).isEqualTo(escenario.vehiculoActivo.localidadId().valor());
	}

	@Test
	void los_limites_del_rango_son_inclusivos() {
		escenario.registrarMantenimiento(LocalDate.of(2026, 3, 1), 90_000, escenario.rutina10k);
		escenario.registrarMantenimiento(LocalDate.of(2026, 6, 1), 95_000, escenario.rutina20k);
		escenario.registrarMantenimiento(LocalDate.of(2026, 9, 1), 100_000, escenario.rutina10k);

		List<MantenimientoRealizado> filas = escenario.realizados.entre(LocalDate.of(2026, 3, 1), LocalDate.of(2026, 9, 1));

		assertThat(filas).hasSize(3);
		assertThat(escenario.realizados.entre(LocalDate.of(2026, 3, 2), LocalDate.of(2026, 8, 31))).hasSize(1);
	}

	@Test
	void incluye_los_mantenimientos_de_toda_la_flota_y_de_vehiculos_inactivos() {
		Vehiculo otro = otroVehiculoActivo();
		escenario.registrarMantenimiento(LocalDate.of(2026, 5, 1), 90_000, escenario.rutina10k);
		escenario.mantenimientosRepo.guardar(Mantenimiento.registrar(otro.id(), escenario.rutina10k.id(), LocalDate.of(2026, 5, 2),
				40_000, null, EscenarioMantenimiento.HOY));
		escenario.mantenimientosRepo.guardar(Mantenimiento.registrar(escenario.vehiculoInactivo.id(), escenario.rutina20k.id(),
				LocalDate.of(2026, 5, 3), 70_000, "antes de darlo de baja", EscenarioMantenimiento.HOY));

		List<MantenimientoRealizado> filas = escenario.realizados.entre(LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 31));

		assertThat(filas).extracting(MantenimientoRealizado::placa).containsExactly("XYZ-999", "DEF-456", "ABC-123");
		assertThat(filas.get(0).observaciones()).isEqualTo("antes de darlo de baja");
	}

	@Test
	void un_rango_sin_mantenimientos_devuelve_una_lista_vacia() {
		assertThat(escenario.realizados.entre(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31))).isEmpty();
	}

	@Test
	void rechaza_un_rango_invertido_y_fechas_nulas() {
		assertThatThrownBy(() -> escenario.realizados.entre(LocalDate.of(2026, 2, 1), LocalDate.of(2026, 1, 1)))
			.isInstanceOf(ReglaDeNegocioException.class);
		assertThatThrownBy(() -> escenario.realizados.entre(null, LocalDate.of(2026, 1, 1))).isInstanceOf(ReglaDeNegocioException.class);
		assertThatThrownBy(() -> escenario.realizados.entre(LocalDate.of(2026, 1, 1), null)).isInstanceOf(ReglaDeNegocioException.class);
	}
}
