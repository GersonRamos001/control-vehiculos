package com.gerson.ramos.control_vehiculos.vehiculo.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import com.gerson.ramos.control_vehiculos.shared.domain.ReglaDeNegocioException;

class CodigoEquipoTest {

	@Test
	void normaliza_espacios_y_mayusculas() {
		assertThat(new CodigoEquipo(" eq-001/a ").valor()).isEqualTo("EQ-001/A");
	}

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = { "   ", "EQ 001", "-EQ001", "EQ#001", "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789" })
	void rechaza_codigos_invalidos(String valor) {
		assertThatThrownBy(() -> new CodigoEquipo(valor)).isInstanceOf(ReglaDeNegocioException.class);
	}
}
