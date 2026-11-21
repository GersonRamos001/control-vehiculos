package com.gerson.ramos.control_vehiculos.testsupport;

import java.util.ArrayList;
import java.util.List;

import com.gerson.ramos.control_vehiculos.usuarios.application.port.out.SesionesPort;

/** Anota a quién se le pidió cerrar las sesiones. */
public class SesionesEspia implements SesionesPort {

	public final List<String> cerradas = new ArrayList<>();

	@Override
	public void cerrarSesionesDe(String username) {
		cerradas.add(username);
	}
}
