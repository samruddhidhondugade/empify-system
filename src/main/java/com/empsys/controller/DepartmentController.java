package com.empsys.controller;

import com.empsys.dto.DepartmentDTO;
import com.empsys.service.DepartmentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@Slf4j
@RestController
@RequestMapping("/api/departments")
@CrossOrigin(origins = "*")
public class DepartmentController {

    @Autowired
    private DepartmentService departmentService;

    @GetMapping
    public ResponseEntity<List<DepartmentDTO>> getAllDepartments() {
        log.info("API Call: Get all departments");
        List<DepartmentDTO> list = departmentService.getAllDepartments();
        log.info("Returning {} departments", list.size());
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DepartmentDTO> getDepartmentById(@PathVariable Long id) {
        log.info("API Call: Get department by ID {}", id);
        DepartmentDTO dto = departmentService.getDepartmentById(id);
        log.info("Department found for ID {}", id);
        return ResponseEntity.ok(dto);
    }

    @PostMapping
    public ResponseEntity<DepartmentDTO> addDepartment(@RequestBody DepartmentDTO dto) {
        log.info("API Call: Add new department {}", dto.getDeptName());
        DepartmentDTO created = departmentService.addDepartment(dto);
        log.info("Department created successfully with ID {}", created.getDeptId());
        return ResponseEntity.status(201).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DepartmentDTO> updateDepartment(@PathVariable Long id, @RequestBody DepartmentDTO dto) {
        log.info("API Call: Update department ID {}", id);
        dto.setDeptId(id);
        DepartmentDTO updated = departmentService.updateDepartment(id, dto);
        log.info("Department updated successfully with ID {}", id);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteDepartment(@PathVariable Long id) {
        log.warn("API Call: Delete department ID {}", id);
        departmentService.deleteDepartment(id);
        log.info("Department deleted successfully with ID {}", id);
        return ResponseEntity.ok("Department deleted successfully");
    }

    @GetMapping("/count")
    public ResponseEntity<Long> countDepartments() {
        log.info("API Call: Count departments");
        long count = departmentService.countDepartment();
        log.info("Total departments: {}", count);
        return ResponseEntity.ok(count);
    }
}