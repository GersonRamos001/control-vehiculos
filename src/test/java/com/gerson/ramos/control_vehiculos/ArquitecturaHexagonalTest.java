package com.gerson.ramos.control_vehiculos;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/** Reglas que protegen la arquitectura hexagonal: el dominio y la aplicación no conocen la infraestructura. */
@AnalyzeClasses(packages = "com.gerson.ramos.control_vehiculos", importOptions = ImportOption.DoNotIncludeTests.class)
class ArquitecturaHexagonalTest {

	@ArchTest
	static final ArchRule dominio_es_java_puro = noClasses().that().resideInAPackage("..domain..")
		.should().dependOnClassesThat().resideInAnyPackage("org.springframework..", "jakarta..", "lombok..",
				"..application..", "..infrastructure..")
		.because("el dominio no depende de frameworks ni de capas externas");

	@ArchTest
	static final ArchRule apache_poi_solo_se_usa_en_infraestructura = noClasses().that().resideOutsideOfPackage("..infrastructure..")
		.should().dependOnClassesThat().resideInAnyPackage("org.apache.poi..", "org.openxmlformats..")
		.because("la generación de Excel es un detalle de infraestructura (adaptador de entrada web)");

	@ArchTest
	static final ArchRule aplicacion_no_depende_de_infraestructura =noClasses().that().resideInAPackage("..application..")
		.should().dependOnClassesThat().resideInAnyPackage("..infrastructure..", "jakarta.persistence..")
		.because("la aplicación solo conoce puertos y dominio");

	@ArchTest
	static final ArchRule infraestructura_solo_se_usa_desde_si_misma = noClasses().that().resideOutsideOfPackage("..infrastructure..")
		.should().dependOnClassesThat().resideInAPackage("..infrastructure..")
		.because("los adaptadores se alcanzan solo a través de los puertos");

	@ArchTest
	static final ArchRule adaptadores_de_entrada_no_usan_puertos_de_salida = noClasses().that().resideInAPackage("..adapter.in..")
		.should().dependOnClassesThat().resideInAnyPackage("..port.out..", "..adapter.out..")
		.because("la web habla con casos de uso (puertos de entrada), nunca con repositorios");

	@ArchTest
	static final ArchRule vehiculo_solo_consulta_catalogos_por_su_puerto_de_entrada = noClasses().that().resideInAPackage("..vehiculo..")
		.should().dependOnClassesThat().resideInAnyPackage("..configuracion.application.port.out..",
				"..configuracion.application.service..", "..configuracion.infrastructure..")
		.because("el módulo de vehículos lee catálogos solo a través de ConsultarCatalogosUseCase");

	@ArchTest
	static final ArchRule configuracion_no_depende_de_vehiculo = noClasses().that().resideInAPackage("..configuracion..")
		.should().dependOnClassesThat().resideInAnyPackage("..vehiculo..", "..mantenimiento..", "..kilometraje..", "..alertas..",
				"..reportes..")
		.because("los catálogos no deben conocer a quienes los consumen");

	@ArchTest
	static final ArchRule vehiculo_no_depende_de_modulos_superiores = noClasses().that().resideInAPackage("..vehiculo..")
		.should().dependOnClassesThat().resideInAnyPackage("..mantenimiento..", "..kilometraje..", "..alertas..", "..reportes..")
		.because("las dependencias van de reportes a alertas a kilometraje a mantenimiento a vehículo, nunca al revés");

	@ArchTest
	static final ArchRule mantenimiento_no_depende_de_modulos_superiores = noClasses().that().resideInAPackage("..mantenimiento..")
		.should().dependOnClassesThat().resideInAnyPackage("..kilometraje..", "..alertas..", "..reportes..")
		.because("kilometraje, alertas y reportes consumen la programación de mantenimiento, no al revés");

	@ArchTest
	static final ArchRule kilometraje_no_depende_de_alertas_ni_reportes = noClasses().that().resideInAPackage("..kilometraje..")
		.should().dependOnClassesThat().resideInAnyPackage("..alertas..", "..reportes..")
		.because("alertas y reportes consumen el seguimiento de kilometraje, no al revés");

	@ArchTest
	static final ArchRule alertas_no_depende_de_reportes = noClasses().that().resideInAPackage("..alertas..")
		.should().dependOnClassesThat().resideInAPackage("..reportes..")
		.because("reportes consume las alertas, no al revés");

	@ArchTest
	static final ArchRule reportes_solo_usa_otros_modulos_por_sus_puertos_de_entrada = noClasses().that().resideInAPackage("..reportes..")
		.should().dependOnClassesThat().resideInAnyPackage("..vehiculo.application.port.out..", "..vehiculo.application.service..",
				"..vehiculo.infrastructure..", "..mantenimiento.application.port.out..", "..mantenimiento.application.service..",
				"..mantenimiento.infrastructure..", "..kilometraje.application.port.out..", "..kilometraje.application.service..",
				"..kilometraje.infrastructure..", "..alertas.application.service..", "..alertas.infrastructure..",
				"..configuracion.application.port.out..", "..configuracion.application.service..", "..configuracion.infrastructure..")
		.because("reportes consulta otros módulos solo a través de sus casos de uso");

	@ArchTest
	static final ArchRule alertas_solo_usa_otros_modulos_por_sus_puertos_de_entrada = noClasses().that().resideInAPackage("..alertas..")
		.should().dependOnClassesThat().resideInAnyPackage("..vehiculo.application.port.out..", "..vehiculo.application.service..",
				"..vehiculo.infrastructure..", "..mantenimiento.application.port.out..", "..mantenimiento.application.service..",
				"..mantenimiento.infrastructure..", "..kilometraje.application.port.out..", "..kilometraje.application.service..",
				"..kilometraje.infrastructure..", "..configuracion.application.port.out..", "..configuracion.application.service..",
				"..configuracion.infrastructure..")
		.because("alertas consulta otros módulos solo a través de sus casos de uso");

	@ArchTest
	static final ArchRule kilometraje_solo_usa_otros_modulos_por_sus_puertos_de_entrada = noClasses().that().resideInAPackage("..kilometraje..")
		.should().dependOnClassesThat().resideInAnyPackage("..vehiculo.application.port.out..", "..vehiculo.application.service..",
				"..vehiculo.infrastructure..", "..mantenimiento.application.port.out..", "..mantenimiento.application.service..",
				"..mantenimiento.infrastructure..", "..configuracion.application.port.out..", "..configuracion.application.service..",
				"..configuracion.infrastructure..")
		.because("kilometraje consulta otros módulos solo a través de sus casos de uso");

	@ArchTest
	static final ArchRule mantenimiento_solo_usa_otros_modulos_por_sus_puertos_de_entrada = noClasses().that().resideInAPackage("..mantenimiento..")
		.should().dependOnClassesThat().resideInAnyPackage("..vehiculo.application.port.out..", "..vehiculo.application.service..",
				"..vehiculo.infrastructure..", "..configuracion.application.port.out..", "..configuracion.application.service..",
				"..configuracion.infrastructure..")
		.because("mantenimiento consulta vehículos y catálogos solo a través de sus casos de uso");

	@ArchTest
	static final ArchRule compartido_no_depende_de_modulos = noClasses().that().resideInAPackage("..shared..")
		.should().dependOnClassesThat().resideInAnyPackage("..vehiculo..", "..configuracion..", "..usuarios..")
		.because("shared es un kernel mínimo sin dependencias de módulos");

	@ArchTest
	static final ArchRule usuarios_no_depende_de_otros_modulos = noClasses().that().resideInAPackage("..usuarios..")
		.should().dependOnClassesThat().resideInAnyPackage("..vehiculo..", "..configuracion..", "..mantenimiento..", "..kilometraje..",
				"..alertas..", "..reportes..")
		.because("la gestión de usuarios y roles es independiente de los módulos operativos");

	@ArchTest
	static final ArchRule nadie_depende_del_modulo_de_usuarios = noClasses().that().resideOutsideOfPackage("..usuarios..")
		.should().dependOnClassesThat().resideInAPackage("..usuarios..")
		.because("los permisos se aplican en SecurityConfig por nombre de rol; ningún módulo conoce a usuarios");

	@ArchTest
	static final ArchRule spring_security_solo_en_infraestructura = noClasses().that().resideInAnyPackage("..domain..", "..application..")
		.should().dependOnClassesThat().resideInAPackage("org.springframework.security..")
		.because("el dominio y la aplicación no conocen Spring Security; lo aíslan los adaptadores");
}
