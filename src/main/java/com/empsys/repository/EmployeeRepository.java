package com.empsys.repository;

import com.empsys.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    @Query("SELECT e FROM Employee e " +
           "WHERE LOWER(e.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(e.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(e.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(e.phone) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(e.department.deptName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(e.designation.desigName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Employee> searchEmployees(String keyword, Pageable pageable);
}
