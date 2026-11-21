package com.gerson.ramos.control_vehiculos.vehiculo.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import com.gerson.ramos.control_vehiculos.shared.domain.ReglaDeNegocioException;

class PlacaTest {

	@Test
	void normaliza_espacios_y_mayusculas() {
		assertThat(new Placa("  abc-123 ").valor()).isEqualTo("ABC-123");
	}

	@Test
	void placas_equivalentes_son_iguales() {
		assertThat(new Placa("abc123")).isEqualTo(new Placa(" ABC123 "));
	}

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = { "   ", "AB", "ABCDEFGHIJK", "ABC 123", "ABC_123", "ÑAB-123" })
	void rechaza_placas_invalidas(String valor) {
		assertThatThrownBy(() -> new Placa(valor)).isInstanceOf(ReglaDeNegocioException.class);
	}
}
