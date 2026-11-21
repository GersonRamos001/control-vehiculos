package com.gerson.ramos.control_vehiculos.configuracion.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.gerson.ramos.control_vehiculos.configuracion.domain.model.Localidad;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.RutinaMantenimiento;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.TipoUso;
import com.gerson.ramos.control_vehiculos.shared.domain.ConflictoDeDatosException;
import com.gerson.ramos.control_vehiculos.shared.domain.RecursoNoEncontradoException;
import com.gerson.ramos.control_vehiculos.shared.domain.ReglaDeNegocioException;
import com.gerson.ramos.control_vehiculos.testsupport.InMemoryLocalidadRepository;
import com.gerson.ramos.control_vehiculos.testsupport.InMemoryRutinaMantenimientoRepository;
import com.gerson.ramos.control_vehiculos.testsupport.InMemoryTipoUsoRepository;

class GestionarCatalogosServicesTest {

	private final GestionarLocalidadesService localidades = new GestionarLocalidadesService(new InMemoryLocalidadRepository());
	private final GestionarTiposUsoService tiposUso = new GestionarTiposUsoService(new InMemoryTipoUsoRepository());
	private final GestionarRutinasService rutinas = new GestionarRutinasService(new InMemoryRutinaMantenimientoRepository());

	@Test
	void crea_una_localidad_y_rechaza_el_codigo_repetido() {
		Localidad creada = localidades.crear("sps", "San Pedro Sula");

		assertThat(creada.id()).isNotNull();
		assertThat(creada.codigo().valor()).isEqualTo("SPS");
		assertThat(creada.activo()).isTrue();
		assertThatThrownBy(() -> localidades.crear(" SPS ", "Otra"))
			.isInstanceOfSatisfying(ConflictoDeDatosException.class, e -> assertThat(e.campo()).isEqualTo("codigo"));
	}

	@Test
	void renombra_y_cambia_el_estado_de_una_localidad() {
		long id = localidades.crear("SPS", "San Pedro Sula").id().valor();

		assertThat(localidades.renombrar(id, "SPS Centro").nombre()).isEqualTo("SPS Centro");
		assertThat(localidades.cambiarEstado(id, false).activo()).isFalse();
		assertThat(localidades.cambiarEstado(id, false).activo()).isFalse();
		assertThat(localidades.cambiarEstado(id, true).activo()).isTrue();
		assertThat(localidades.listar()).hasSize(1);
	}

	@Test
	void falla_si_la_localidad_no_existe() {
		assertThatThrownBy(() -> localidades.buscar(99)).isInstanceOf(RecursoNoEncontradoException.class);
		assertThatThrownBy(() -> localidades.renombrar(99, "x")).isInstanceOf(RecursoNoEncontradoException.class);
	}

	@Test
	void gestiona_tipos_de_uso() {
		TipoUso creado = tiposUso.crear("operativo", "Operativo");

		assertThat(creado.codigo().valor()).isEqualTo("OPERATIVO");
		assertThatThrownBy(() -> tiposUso.crear("OPERATIVO", "Duplicado")).isInstanceOf(ConflictoDeDatosException.class);
		assertThat(tiposUso.cambiarEstado(creado.id().valor(), false).activo()).isFalse();
		assertThat(tiposUso.renombrar(creado.id().valor(), "Uso operativo").nombre()).isEqualTo("Uso operativo");
	}

	@Test
	void gestiona_rutinas() {
		RutinaMantenimiento creada = rutinas.crear("10k", "Preventivo 10K");

		assertThat(creada.codigo().valor()).isEqualTo("10K");
		assertThatThrownBy(() -> rutinas.crear("10K", "Duplicada")).isInstanceOf(ConflictoDeDatosException.class);
		assertThat(rutinas.renombrar(creada.id().valor(), "Preventivo 10K (nuevo)").nombre()).isEqualTo("Preventivo 10K (nuevo)");
		assertThat(rutinas.cambiarEstado(creada.id().valor(), false).activo()).isFalse();
	}

	@Test
	void rechaza_rutinas_con_datos_invalidos() {
		assertThatThrownBy(() -> rutinas.crear("10K", " ")).isInstanceOf(ReglaDeNegocioException.class);
		assertThatThrownBy(() -> rutinas.crear("con espacio", "Preventivo")).isInstanceOf(ReglaDeNegocioException.class);
	}
}
