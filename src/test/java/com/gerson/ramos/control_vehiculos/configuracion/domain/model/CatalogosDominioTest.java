package com.gerson.ramos.control_vehiculos.configuracion.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.gerson.ramos.control_vehiculos.shared.domain.ReglaDeNegocioException;

class CatalogosDominioTest {

	@Test
	void codigo_se_normaliza_y_valida_formato() {
		assertThat(new CodigoCatalogo(" operativo ").valor()).isEqualTo("OPERATIVO");
		assertThat(new CodigoCatalogo("10k").valor()).isEqualTo("10K");
		assertThatThrownBy(() -> new CodigoCatalogo("con espacio")).isInstanceOf(ReglaDeNegocioException.class);
		assertThatThrownBy(() -> new CodigoCatalogo("")).isInstanceOf(ReglaDeNegocioException.class);
	}

	@Test
	void localidad_nueva_nace_activa_y_puede_desactivarse() {
		Localidad localidad = Localidad.crear(new CodigoCatalogo("SPS"), "  San Pedro Sula ");

		assertThat(localidad.id()).isNull();
		assertThat(localidad.nombre()).isEqualTo("San Pedro Sula");
		assertThat(localidad.activo()).isTrue();

		localidad.desactivar();
		assertThat(localidad.activo()).isFalse();
	}

	@Test
	void nombre_es_obligatorio() {
		assertThatThrownBy(() -> Localidad.crear(new CodigoCatalogo("SPS"), " ")).isInstanceOf(ReglaDeNegocioException.class);
		assertThatThrownBy(() -> TipoUso.crear(new CodigoCatalogo("RELEVO"), null)).isInstanceOf(ReglaDeNegocioException.class);
	}

	@Test
	void rutina_nueva_nace_activa_y_se_puede_renombrar_y_desactivar() {
		RutinaMantenimiento rutina = RutinaMantenimiento.crear(new CodigoCatalogo("10K"), "Preventivo 10K");

		assertThat(rutina.id()).isNull();
		assertThat(rutina.activo()).isTrue();

		rutina.renombrar("  Preventivo 10,000 km ");
		rutina.desactivar();

		assertThat(rutina.nombre()).isEqualTo("Preventivo 10,000 km");
		assertThat(rutina.activo()).isFalse();
		assertThatThrownBy(() -> rutina.renombrar(" ")).isInstanceOf(ReglaDeNegocioException.class);
	}

	@Test
	void parametros_de_alertas_exigen_valores_positivos() {
		ParametrosAlertas parametros = new ParametrosAlertas(1_000, 30, 7);

		assertThat(parametros.umbralKm()).isEqualTo(1_000);
		assertThat(parametros.umbralDias()).isEqualTo(30);
		assertThat(parametros.diasLecturaPendiente()).isEqualTo(7);
		assertThatThrownBy(() -> new ParametrosAlertas(0, 30, 7)).isInstanceOf(ReglaDeNegocioException.class);
		assertThatThrownBy(() -> new ParametrosAlertas(1_000, -1, 7)).isInstanceOf(ReglaDeNegocioException.class);
		assertThatThrownBy(() -> new ParametrosAlertas(1_000, 30, 0)).isInstanceOf(ReglaDeNegocioException.class);
	}

	@Test
	void parametros_de_mantenimiento_exigen_intervalos_positivos() {
		ParametrosMantenimiento parametros = new ParametrosMantenimiento(10_000, 6);

		assertThat(parametros.intervaloKm()).isEqualTo(10_000);
		assertThat(parametros.intervaloMeses()).isEqualTo(6);
		assertThatThrownBy(() -> new ParametrosMantenimiento(0, 6)).isInstanceOf(ReglaDeNegocioException.class);
		assertThatThrownBy(() -> new ParametrosMantenimiento(10_000, -1)).isInstanceOf(ReglaDeNegocioException.class);
	}
}
