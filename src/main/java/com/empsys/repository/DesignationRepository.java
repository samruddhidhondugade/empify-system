package com.empsys.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.empsys.entity.*;

public interface DesignationRepository extends JpaRepository<Designation, Long> {
	
	
}