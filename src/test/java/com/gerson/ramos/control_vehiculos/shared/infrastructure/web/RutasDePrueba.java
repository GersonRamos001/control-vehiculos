package com.gerson.ramos.control_vehiculos.shared.infrastructure.web;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Responde 200 en las rutas reales de la aplicación para probar solo las reglas de acceso de SecurityConfig. */
@RestController
class RutasDePrueba {

	@RequestMapping({ "/vehiculos", "/vehiculos/nuevo", "/vehiculos/5/mantenimientos", "/vehiculos/5/kilometraje", "/alertas",
			"/reportes/pendientes", "/reportes/pendientes/xlsx", "/mantenimientos/programacion", "/kilometraje/semanal",
			"/configuracion/parametros", "/configuracion/localidades", "/usuarios", "/usuarios/2/clave", "/cuenta/clave",
			"/js/tablas-interactivas.js" })
	String ok() {
		return "ok";
	}
}
