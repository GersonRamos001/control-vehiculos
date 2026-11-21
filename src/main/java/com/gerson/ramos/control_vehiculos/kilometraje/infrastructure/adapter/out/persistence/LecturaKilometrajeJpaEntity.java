package com.gerson.ramos.control_vehiculos.kilometraje.infrastructure.adapter.out.persistence;

import java.time.LocalDate;

import com.gerson.ramos.control_vehiculos.shared.infrastructure.persistence.AuditableJpaEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

/** Vehículo y fecha no cambian; solo el kilometraje se puede corregir. El vehículo se guarda como id (FK en la BD). */
@Entity
@Table(name = "lectura_kilometraje")
@Getter
public class LecturaKilometrajeJpaEntity extends AuditableJpaEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "vehiculo_id", nullable = false, updatable = false)
	private Long vehiculoId;

	@Column(nullable = false, updatable = false)
	private LocalDate fecha;

	@Column(nullable = false)
	private int kilometraje;

	protected LecturaKilometrajeJpaEntity() {
		// requerido por JPA
	}

	LecturaKilometrajeJpaEntity(Long vehiculoId, LocalDate fecha, int kilometraje) {
		this.vehiculoId = vehiculoId;
		this.fecha = fecha;
		this.kilometraje = kilometraje;
	}

	void actualizarKilometraje(int kilometraje) {
		this.kilometraje = kilometraje;
	}

	@Override
	public boolean equals(Object otro) {
		return this == otro || (otro instanceof LecturaKilometrajeJpaEntity que && id != null && id.equals(que.id));
	}

	@Override
	public int hashCode() {
		return LecturaKilometrajeJpaEntity.class.hashCode();
	}
}
