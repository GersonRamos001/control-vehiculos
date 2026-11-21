package com.gerson.ramos.control_vehiculos.mantenimiento.infrastructure.adapter.out.persistence;

import java.time.LocalDate;

import com.gerson.ramos.control_vehiculos.shared.infrastructure.persistence.AuditableJpaEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

/**
 * Registro inmutable del historial: no hay métodos de actualización. Vehículo y rutina se guardan como ids
 * (la integridad la garantizan las FK de la base de datos).
 */
@Entity
@Table(name = "mantenimiento")
@Getter
public class MantenimientoJpaEntity extends AuditableJpaEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "vehiculo_id", nullable = false, updatable = false)
	private Long vehiculoId;

	@Column(name = "rutina_id", nullable = false, updatable = false)
	private Long rutinaId;

	@Column(nullable = false, updatable = false)
	private LocalDate fecha;

	@Column(nullable = false, updatable = false)
	private int kilometraje;

	@Column(length = 500, updatable = false)
	private String observaciones;

	protected MantenimientoJpaEntity() {
		// requerido por JPA
	}

	MantenimientoJpaEntity(Long vehiculoId, Long rutinaId, LocalDate fecha, int kilometraje, String observaciones) {
		this.vehiculoId = vehiculoId;
		this.rutinaId = rutinaId;
		this.fecha = fecha;
		this.kilometraje = kilometraje;
		this.observaciones = observaciones;
	}

	@Override
	public boolean equals(Object otro) {
		return this == otro || (otro instanceof MantenimientoJpaEntity que && id != null && id.equals(que.id));
	}

	@Override
	public int hashCode() {
		return MantenimientoJpaEntity.class.hashCode();
	}
}
