package com.gerson.ramos.control_vehiculos.kilometraje.infrastructure.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import com.gerson.ramos.control_vehiculos.TestcontainersConfiguration;
import com.gerson.ramos.control_vehiculos.configuracion.application.port.out.LocalidadRepositoryPort;
import com.gerson.ramos.control_vehiculos.configuracion.application.port.out.TipoUsoRepositoryPort;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.CodigoCatalogo;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.Localidad;
import com.gerson.ramos.control_vehiculos.kilometraje.application.port.out.LecturaKilometrajeRepositoryPort;
import com.gerson.ramos.control_vehiculos.kilometraje.domain.model.LecturaKilometraje;
import com.gerson.ramos.control_vehiculos.vehiculo.application.port.out.VehiculoRepositoryPort;
import com.gerson.ramos.control_vehiculos.vehiculo.domain.model.CodigoEquipo;
import com.gerson.ramos.control_vehiculos.vehiculo.domain.model.Placa;
import com.gerson.ramos.control_vehiculos.vehiculo.domain.model.Vehiculo;
import com.gerson.ramos.control_vehiculos.vehiculo.domain.model.VehiculoId;

/** Integración contra MySQL real (Testcontainers): migración V5, mapeo JPA y consultas de "última lectura". */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
class LecturaKilometrajePersistenceAdapterTest {

	private static final LocalDate HOY = LocalDate.of(2026, 11, 21);

	@Autowired
	LecturaKilometrajeRepositoryPort lecturas;

	@Autowired
	VehiculoRepositoryPort vehiculos;

	@Autowired
	LocalidadRepositoryPort localidades;

	@Autowired
	TipoUsoRepositoryPort tiposUso;

	private Vehiculo vehiculo;

	@BeforeEach
	void preparar() {
		Localidad localidad = localidades.guardar(Localidad.crear(new CodigoCatalogo("SPS"), "San Pedro Sula"));
		var operativo = tiposUso.buscarPorCodigo(new CodigoCatalogo("OPERATIVO")).orElseThrow();
		vehiculo = vehiculos.guardar(Vehiculo.registrar(new Placa("ABC-123"), new CodigoEquipo("EQ-001"), localidad.id(), operativo.id()));
	}

	private LecturaKilometraje guardar(VehiculoId vehiculoId, LocalDate fecha, int km) {
		return lecturas.guardar(LecturaKilometraje.registrar(vehiculoId, fecha, km, HOY));
	}

	@Test
	void guarda_y_recupera_el_historial_de_la_mas_reciente_a_la_mas_antigua() {
		guardar(vehiculo.id(), LocalDate.of(2026, 11, 1), 106_000);
		LecturaKilometraje reciente = guardar(vehiculo.id(), LocalDate.of(2026, 11, 14), 107_500);

		assertThat(reciente.id()).isNotNull();
		assertThat(lecturas.historialDe(vehiculo.id())).extracting(LecturaKilometraje::kilometraje).containsExactly(107_500, 106_000);
		assertThat(lecturas.ultimaDe(vehiculo.id())).hasValueSatisfying(l -> assertThat(l.id()).isEqualTo(reciente.id()));
	}

	@Test
	void sin_lecturas_no_hay_ultima() {
		assertThat(lecturas.ultimaDe(vehiculo.id())).isEmpty();
		assertThat(lecturas.historialDe(vehiculo.id())).isEmpty();
	}

	@Test
	void busca_por_vehiculo_y_fecha() {
		guardar(vehiculo.id(), LocalDate.of(2026, 11, 14), 107_500);

		assertThat(lecturas.buscarPorVehiculoYFecha(vehiculo.id(), LocalDate.of(2026, 11, 14))).isPresent();
		assertThat(lecturas.buscarPorVehiculoYFecha(vehiculo.id(), LocalDate.of(2026, 11, 15))).isEmpty();
	}

	@Test
	void guardar_con_id_actualiza_el_kilometraje_de_la_misma_lectura() {
		LecturaKilometraje original = guardar(vehiculo.id(), LocalDate.of(2026, 11, 14), 175_000);

		LecturaKilometraje corregida = lecturas.guardar(original.conKilometraje(107_500));

		assertThat(corregida.id()).isEqualTo(original.id());
		assertThat(corregida.kilometraje()).isEqualTo(107_500);
		assertThat(lecturas.historialDe(vehiculo.id())).hasSize(1);
	}

	@Test
	void la_base_de_datos_rechaza_dos_lecturas_del_mismo_vehiculo_y_fecha() {
		guardar(vehiculo.id(), LocalDate.of(2026, 11, 14), 107_500);

		assertThatThrownBy(() -> guardar(vehiculo.id(), LocalDate.of(2026, 11, 14), 108_000))
			.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	void la_ultima_de_cada_vehiculo_devuelve_una_por_vehiculo() {
		Vehiculo otro = vehiculos.guardar(Vehiculo.registrar(new Placa("DEF-456"), new CodigoEquipo("EQ-002"),
				vehiculo.localidadId(), vehiculo.tipoUsoId()));
		Vehiculo sinLecturas = vehiculos.guardar(Vehiculo.registrar(new Placa("GHI-789"), new CodigoEquipo("EQ-003"),
				vehiculo.localidadId(), vehiculo.tipoUsoId()));
		guardar(vehiculo.id(), LocalDate.of(2026, 11, 1), 106_000);
		LecturaKilometraje ultimaPrimero = guardar(vehiculo.id(), LocalDate.of(2026, 11, 14), 107_500);
		LecturaKilometraje ultimaOtro = guardar(otro.id(), LocalDate.of(2026, 11, 7), 50_000);

		var ultimas = lecturas.ultimasDeTodosLosVehiculos();

		assertThat(ultimas).extracting(LecturaKilometraje::id).containsExactlyInAnyOrder(ultimaPrimero.id(), ultimaOtro.id());
		assertThat(ultimas).extracting(LecturaKilometraje::vehiculoId).doesNotContain(sinLecturas.id());
	}

	@Test
	void elimina_una_lectura() {
		LecturaKilometraje primera = guardar(vehiculo.id(), LocalDate.of(2026, 11, 1), 106_000);
		LecturaKilometraje segunda = guardar(vehiculo.id(), LocalDate.of(2026, 11, 14), 107_500);

		lecturas.eliminar(segunda.id());

		assertThat(lecturas.historialDe(vehiculo.id())).extracting(LecturaKilometraje::id).containsExactly(primera.id());
		assertThat(lecturas.ultimaDe(vehiculo.id())).hasValueSatisfying(l -> assertThat(l.id()).isEqualTo(primera.id()));
	}

	@Test
	void la_base_de_datos_rechaza_un_vehiculo_inexistente() {
		assertThatThrownBy(() -> guardar(new VehiculoId(999_999), LocalDate.of(2026, 11, 14), 107_500))
			.isInstanceOf(DataIntegrityViolationException.class);
	}
}
