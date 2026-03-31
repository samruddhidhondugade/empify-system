package com.empsys.controller;

import com.empsys.dto.EmployeeDTO;
import com.empsys.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;

@RestController
@RequestMapping("/api/employees")
@CrossOrigin(origins = "*")
@Slf4j
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    //  Get all employees with pagination
    @GetMapping
    public ResponseEntity<Page<EmployeeDTO>> getAllEmployees(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<EmployeeDTO> employees = employeeService.getAllEmployees(page, size);
        log.info("Returning {} employees", employees.getContent().size());
        return ResponseEntity.ok(employees);
    }

    //Get employee by ID
    @GetMapping("/{id}")
    public ResponseEntity<EmployeeDTO> getEmployeeById(@PathVariable Long id) {

        EmployeeDTO emp = employeeService.getEmployeeById(id);
        log.info("Returning employee: {} {}", emp.getFirstName(), emp.getLastName());
        return ResponseEntity.ok(emp);
    }

    // Add employee
    @PostMapping
    public ResponseEntity<EmployeeDTO> addEmployee(@RequestBody EmployeeDTO dto) {
        EmployeeDTO createdEmployee = employeeService.addEmployee(dto);
        log.info("Employee created with id: {}", createdEmployee.getEmpId());
        return new ResponseEntity<>(createdEmployee, HttpStatus.CREATED);
    }

    // Update employee
    @PutMapping("/{id}")
    public ResponseEntity<EmployeeDTO> updateEmployee(@PathVariable Long id, @RequestBody EmployeeDTO dto) {
        dto.setEmpId(id);
        EmployeeDTO updatedEmployee = employeeService.updateEmployee(id, dto);
        log.info("Employee updated with id: {}", id);
        return ResponseEntity.ok(updatedEmployee);
    }

    //Delete employee
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteEmployee(@PathVariable Long id) {

        employeeService.deleteEmployee(id);
        log.info("Employee deleted successfully with id: {}", id);
        return ResponseEntity.ok("Employee deleted successfully with id: " + id);
    }

    // Search employees with pagination
    @GetMapping("/search")
    public ResponseEntity<Page<EmployeeDTO>> searchEmployees(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<EmployeeDTO> result = employeeService.searchEmployees(keyword, page, size);
        log.info("Found {} employees matching keyword '{}'", result.getContent().size(), keyword);
        return ResponseEntity.ok(result);
    }

    // Count employees API
    @GetMapping("/count")
    public ResponseEntity<Long> countEmployees() {
        long count = employeeService.countEmployee();
        log.info("Total number of employees: {}", count);
        return ResponseEntity.ok(count);
    }
}