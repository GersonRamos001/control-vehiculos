package com.gerson.ramos.control_vehiculos.vehiculo.infrastructure.adapter.out.persistence;

import com.gerson.ramos.control_vehiculos.shared.infrastructure.persistence.AuditableJpaEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;

/**
 * Las referencias a localidad, tipo de uso y rutina se guardan como ids (no como relaciones JPA) para no acoplar
 * este módulo a las entidades del módulo de configuración; la integridad la garantizan las FK de la base de datos.
 */
@Entity
@Table(name = "vehiculo")
@Getter
public class VehiculoJpaEntity extends AuditableJpaEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 10)
	private String placa;

	@Column(name = "codigo_equipo", nullable = false, length = 30)
	private String codigoEquipo;

	@Column(name = "localidad_id", nullable = false)
	private Long localidadId;

	@Column(name = "tipo_uso_id", nullable = false)
	private Long tipoUsoId;

	@Column(name = "ultima_rutina_id")
	private Long ultimaRutinaId;

	@Column(nullable = false)
	private boolean activo;

	@Version
	private Long version;

	protected VehiculoJpaEntity() {
		// requerido por JPA
	}

	VehiculoJpaEntity(String placa, String codigoEquipo, Long localidadId, Long tipoUsoId, Long ultimaRutinaId,
			boolean activo) {
		actualizar(placa, codigoEquipo, localidadId, tipoUsoId, ultimaRutinaId, activo);
	}

	final void actualizar(String placa, String codigoEquipo, Long localidadId, Long tipoUsoId, Long ultimaRutinaId,
			boolean activo) {
		this.placa = placa;
		this.codigoEquipo = codigoEquipo;
		this.localidadId = localidadId;
		this.tipoUsoId = tipoUsoId;
		this.ultimaRutinaId = ultimaRutinaId;
		this.activo = activo;
	}

	@Override
	public boolean equals(Object otro) {
		return this == otro || (otro instanceof VehiculoJpaEntity que && id != null && id.equals(que.id));
	}

	@Override
	public int hashCode() {
		return VehiculoJpaEntity.class.hashCode();
	}
}
