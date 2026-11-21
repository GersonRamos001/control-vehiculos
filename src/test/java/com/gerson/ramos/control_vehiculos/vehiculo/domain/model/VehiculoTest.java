package com.gerson.ramos.control_vehiculos.vehiculo.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.gerson.ramos.control_vehiculos.configuracion.domain.model.LocalidadId;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.RutinaMantenimientoId;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.TipoUsoId;
import com.gerson.ramos.control_vehiculos.shared.domain.ReglaDeNegocioException;

class VehiculoTest {

	private static Vehiculo vehiculoNuevo() {
		return Vehiculo.registrar(new Placa("ABC-123"), new CodigoEquipo("EQ-001"), new LocalidadId(1), new TipoUsoId(1));
	}

	@Test
	void vehiculo_nuevo_nace_activo_sin_id_ni_rutina() {
		Vehiculo vehiculo = vehiculoNuevo();

		assertThat(vehiculo.id()).isNull();
		assertThat(vehiculo.version()).isNull();
		assertThat(vehiculo.activo()).isTrue();
		assertThat(vehiculo.ultimaRutinaId()).isNull();
	}

	@Test
	void registra_la_ultima_rutina_realizada() {
		Vehiculo vehiculo = vehiculoNuevo();

		vehiculo.registrarRutinaRealizada(new RutinaMantenimientoId(2));

		assertThat(vehiculo.ultimaRutinaId()).isEqualTo(new RutinaMantenimientoId(2));
	}

	@Test
	void permite_reasignar_localidad_y_cambiar_tipo_de_uso() {
		Vehiculo vehiculo = vehiculoNuevo();

		vehiculo.reasignarLocalidad(new LocalidadId(7));
		vehiculo.cambiarTipoUso(new TipoUsoId(2));

		assertThat(vehiculo.localidadId()).isEqualTo(new LocalidadId(7));
		assertThat(vehiculo.tipoUsoId()).isEqualTo(new TipoUsoId(2));
	}

	@Test
	void vehiculo_inactivo_no_puede_modificarse() {
		Vehiculo vehiculo = vehiculoNuevo();
		vehiculo.desactivar();

		assertThat(vehiculo.activo()).isFalse();
		assertThatThrownBy(() -> vehiculo.reasignarLocalidad(new LocalidadId(2))).isInstanceOf(ReglaDeNegocioException.class);
		assertThatThrownBy(() -> vehiculo.cambiarTipoUso(new TipoUsoId(2))).isInstanceOf(ReglaDeNegocioException.class);
		assertThatThrownBy(() -> vehiculo.registrarRutinaRealizada(new RutinaMantenimientoId(1)))
			.isInstanceOf(ReglaDeNegocioException.class);
		assertThatThrownBy(() -> vehiculo.corregirIdentificacion(new Placa("XYZ-999"), new CodigoEquipo("EQ-002")))
			.isInstanceOf(ReglaDeNegocioException.class);
	}

	@Test
	void reactivar_permite_modificar_de_nuevo() {
		Vehiculo vehiculo = vehiculoNuevo();
		vehiculo.desactivar();
		vehiculo.activar();

		vehiculo.reasignarLocalidad(new LocalidadId(3));

		assertThat(vehiculo.localidadId()).isEqualTo(new LocalidadId(3));
	}

	@Test
	void corregir_identificacion_actualiza_placa_y_codigo() {
		Vehiculo vehiculo = vehiculoNuevo();

		vehiculo.corregirIdentificacion(new Placa("xyz-999"), new CodigoEquipo("eq-002"));

		assertThat(vehiculo.placa().valor()).isEqualTo("XYZ-999");
		assertThat(vehiculo.codigoEquipo().valor()).isEqualTo("EQ-002");
	}

	@Test
	void exige_datos_obligatorios() {
		assertThatThrownBy(() -> Vehiculo.registrar(null, new CodigoEquipo("EQ-001"), new LocalidadId(1), new TipoUsoId(1)))
			.isInstanceOf(ReglaDeNegocioException.class);
		assertThatThrownBy(() -> Vehiculo.registrar(new Placa("ABC-123"), new CodigoEquipo("EQ-001"), null, new TipoUsoId(1)))
			.isInstanceOf(ReglaDeNegocioException.class);
	}

	@Test
	void ids_deben_ser_positivos() {
		assertThatThrownBy(() -> new LocalidadId(0)).isInstanceOf(ReglaDeNegocioException.class);
		assertThatThrownBy(() -> new VehiculoId(-1)).isInstanceOf(ReglaDeNegocioException.class);
	}
}
