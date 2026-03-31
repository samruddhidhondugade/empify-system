package com.empsys.service;

import com.empsys.dto.DepartmentDTO;
import com.empsys.entity.Department;
import com.empsys.exception.ResourceNotFoundException;
import com.empsys.repository.DepartmentRepository;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;
@Slf4j
@Service
public class DepartmentService {

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private ModelMapper modelMapper;

    public List<DepartmentDTO> getAllDepartments() {
        log.info("Fetching all departments");

        List<DepartmentDTO> departments = departmentRepository.findAll(Sort.by(Sort.Direction.ASC, "deptId"))
                .stream()
                .map(dept -> modelMapper.map(dept, DepartmentDTO.class))
                .collect(Collectors.toList());

        log.info("Total departments fetched: {}", departments.size());
        return departments;
    }

    public DepartmentDTO getDepartmentById(Long id) {
        log.info("Fetching department with ID {}", id);

        DepartmentDTO dto = departmentRepository.findById(id)
                .map(dept -> modelMapper.map(dept, DepartmentDTO.class))
                .orElseThrow(() -> {
                    log.error("Department not found with ID {}", id);
                    return new ResourceNotFoundException("Department not found with ID: " + id);
                });

        log.info("Department fetched successfully: ID {}", id);
        return dto;
    }

    public DepartmentDTO addDepartment(DepartmentDTO dto) {
        log.info("Adding new department: {}", dto.getDeptName());

        Department department = modelMapper.map(dto, Department.class);
        departmentRepository.save(department);

        log.info("Department created successfully with ID {}", department.getDeptId());
        return modelMapper.map(department, DepartmentDTO.class);
    }

    public void deleteDepartment(Long id) {
        log.warn("Attempting to delete department with ID {}", id);

        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Department not found with ID {}", id);
                    return new ResourceNotFoundException("Department not found with ID: " + id);
                });

        departmentRepository.delete(dept);
        log.info("Department deleted successfully with ID {}", id);
    }

    public DepartmentDTO updateDepartment(Long id, DepartmentDTO dto) {
        log.info("Updating department ID {} with new name {}", id, dto.getDeptName());

        Department existingDept = departmentRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Department not found with ID {}", id);
                    return new ResourceNotFoundException("Department not found with ID: " + id);
                });

        existingDept.setDeptName(dto.getDeptName());
        departmentRepository.save(existingDept);

        log.info("Department updated successfully ID {}", id);
        return modelMapper.map(existingDept, DepartmentDTO.class);
    }

    public long countDepartment() {
        log.info("Counting total departments");

        long count = departmentRepository.count();
        log.info("Total departments count = {}", count);

        return count;
    }
}