package com.gerson.ramos.control_vehiculos.reportes.infrastructure.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class CsvWriterTest {

	private static String contenido(CsvWriter csv) {
		return new String(csv.bytes(), StandardCharsets.UTF_8);
	}

	@Test
	void empieza_con_bom_utf8_para_que_excel_muestre_bien_los_acentos() {
		byte[] bytes = new CsvWriter(';').fila("Próximo").bytes();

		assertThat(bytes[0]).isEqualTo((byte) 0xEF);
		assertThat(bytes[1]).isEqualTo((byte) 0xBB);
		assertThat(bytes[2]).isEqualTo((byte) 0xBF);
		assertThat(contenido(new CsvWriter(';').fila("Próximo"))).isEqualTo("﻿Próximo\r\n");
	}

	@Test
	void separa_las_celdas_con_el_separador_configurado_y_termina_cada_fila_con_crlf() {
		assertThat(contenido(new CsvWriter(';').fila("a", "b", "c").fila("d", "e", "f"))).isEqualTo("﻿a;b;c\r\nd;e;f\r\n");
		assertThat(contenido(new CsvWriter(',').fila("a", "b"))).isEqualTo("﻿a,b\r\n");
	}

	@Test
	void escribe_numeros_y_fechas_sin_formato_regional_y_deja_vacios_los_nulos() {
		String csv = contenido(new CsvWriter(';').fila(LocalDate.of(2026, 11, 21), 107_500, 2_500L, -50L, 1.5, true, null, "x"));

		assertThat(csv).isEqualTo("﻿2026-11-21;107500;2500;-50;1.5;true;;x\r\n");
	}

	@Test
	void pone_comillas_cuando_el_texto_tiene_el_separador_comillas_o_saltos_de_linea() {
		assertThat(contenido(new CsvWriter(';').fila("a;b"))).isEqualTo("﻿\"a;b\"\r\n");
		assertThat(contenido(new CsvWriter(';').fila("dijo \"hola\""))).isEqualTo("﻿\"dijo \"\"hola\"\"\"\r\n");
		assertThat(contenido(new CsvWriter(';').fila("línea 1\nlínea 2"))).isEqualTo("﻿\"línea 1\nlínea 2\"\r\n");
		assertThat(contenido(new CsvWriter(';').fila("línea 1\r\nlínea 2"))).isEqualTo("﻿\"línea 1\r\nlínea 2\"\r\n");
	}

	@Test
	void una_coma_no_obliga_comillas_si_el_separador_es_punto_y_coma_y_viceversa() {
		assertThat(contenido(new CsvWriter(';').fila("a,b"))).isEqualTo("﻿a,b\r\n");
		assertThat(contenido(new CsvWriter(',').fila("a,b"))).isEqualTo("﻿\"a,b\"\r\n");
		assertThat(contenido(new CsvWriter(',').fila("a;b"))).isEqualTo("﻿a;b\r\n");
	}

	@Test
	void neutraliza_la_inyeccion_de_formulas_en_textos() {
		assertThat(contenido(new CsvWriter(';').fila("=1+1"))).isEqualTo("﻿'=1+1\r\n");
		assertThat(contenido(new CsvWriter(';').fila("+cmd"))).isEqualTo("﻿'+cmd\r\n");
		assertThat(contenido(new CsvWriter(';').fila("-2+3"))).isEqualTo("﻿'-2+3\r\n");
		assertThat(contenido(new CsvWriter(';').fila("@SUMA(A1)"))).isEqualTo("﻿'@SUMA(A1)\r\n");
		assertThat(contenido(new CsvWriter(';').fila("\t=1"))).isEqualTo("﻿'\t=1\r\n");
	}

	@Test
	void la_proteccion_de_formulas_se_combina_con_el_entrecomillado() {
		assertThat(contenido(new CsvWriter(';').fila("=A1;B1"))).isEqualTo("﻿\"'=A1;B1\"\r\n");
	}

	@Test
	void un_numero_negativo_sigue_siendo_numero_y_un_texto_normal_no_se_toca() {
		assertThat(contenido(new CsvWriter(';').fila(-1_200L, "ABC-123", "texto normal"))).isEqualTo("﻿-1200;ABC-123;texto normal\r\n");
	}

	@Test
	void texto_vacio_se_queda_vacio() {
		assertThat(contenido(new CsvWriter(';').fila("", "x"))).isEqualTo("﻿;x\r\n");
	}

	@Test
	void rechaza_separadores_que_romperian_el_formato() {
		assertThatThrownBy(() -> new CsvWriter('"')).isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> new CsvWriter('\n')).isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> new CsvWriter('\r')).isInstanceOf(IllegalArgumentException.class);
	}
}
