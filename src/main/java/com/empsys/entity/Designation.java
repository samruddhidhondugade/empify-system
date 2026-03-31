package com.empsys.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Designation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long desigId;

    @Column(unique = true, nullable = false)
    private String desigName;

	public Long getDesigId() {
		return desigId;
	}

	public void setDesigId(Long desigId) {
		this.desigId = desigId;
	}

	public String getDesigName() {
		return desigName;
	}

	public void setDesigName(String desigName) {
		this.desigName = desigName;
	}
    
    
}
