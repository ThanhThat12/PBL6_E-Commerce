package com.PBL6.Ecommerce.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "vouchers")
public class Vouchers {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// Add fields as needed, e.g. code, description, etc.
	// For now, only id is required for JPA mapping

	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }
}
