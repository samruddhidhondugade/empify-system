package com.empsys.service;

import com.empsys.dto.EmployeeDTO;
import com.empsys.entity.Employee;
import com.empsys.exception.ResourceNotFoundException;
import com.empsys.repository.EmployeeRepository;
import com.empsys.repository.DepartmentRepository;
import com.empsys.repository.DesignationRepository;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Service
@Slf4j
public class EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private DesignationRepository designationRepository;

    @Autowired
    private ModelMapper modelMapper;

    //Get All Employees with Pagination
    public Page<EmployeeDTO> getAllEmployees(int page, int size) {
        log.info("Fetching all employees - page: {}, size: {}", page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by("empId").ascending());
        return employeeRepository.findAll(pageable)
                .map(emp -> {
                    EmployeeDTO dto = modelMapper.map(emp, EmployeeDTO.class);
                    dto.setDeptId(emp.getDepartment() != null ? emp.getDepartment().getDeptId() : null);
                    dto.setDesigId(emp.getDesignation() != null ? emp.getDesignation().getDesigId() : null);
                    return dto;
                });

    }

    public EmployeeDTO getEmployeeById(Long id) {
        log.info("Fetching employee with id: {}", id);
        Employee emp = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));

        EmployeeDTO dto = modelMapper.map(emp, EmployeeDTO.class);
        dto.setDeptId(emp.getDepartment() != null ? emp.getDepartment().getDeptId() : null);
        dto.setDesigId(emp.getDesignation() != null ? emp.getDesignation().getDesigId() : null);
        log.info("Employee found: {} {}", dto.getFirstName(), dto.getLastName());
        return dto;
    }

    public EmployeeDTO addEmployee(EmployeeDTO dto) {
        log.info("Adding new employee: {} {}", dto.getFirstName(), dto.getLastName());
        departmentRepository.findById(dto.getDeptId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + dto.getDeptId()));

        designationRepository.findById(dto.getDesigId())
                .orElseThrow(() -> new ResourceNotFoundException("Designation not found with id: " + dto.getDesigId()));

        Employee emp = modelMapper.map(dto, Employee.class);

        emp.setDepartment(departmentRepository.findById(dto.getDeptId()).get());
        emp.setDesignation(designationRepository.findById(dto.getDesigId()).get());

        employeeRepository.save(emp);

        EmployeeDTO result = modelMapper.map(emp, EmployeeDTO.class);
        result.setDeptId(emp.getDepartment().getDeptId());
        result.setDesigId(emp.getDesignation().getDesigId());
        log.info("Employee saved successfully with id: {}", emp.getEmpId());
        return result;
    }

    // Update Employee
    public EmployeeDTO updateEmployee(Long id, EmployeeDTO dto) {
        log.info("Updating employee with id: {}", id);

        Employee existingEmp = employeeRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Employee not found with id: {}", id);
                    return new ResourceNotFoundException("Employee not found with id: " + id);
                });

        existingEmp.setFirstName(dto.getFirstName());
        existingEmp.setLastName(dto.getLastName());
        existingEmp.setEmail(dto.getEmail());
        existingEmp.setPhone(dto.getPhone());
        existingEmp.setHireDate(dto.getHireDate());

        existingEmp.setDepartment(departmentRepository.findById(dto.getDeptId())
                .orElseThrow(() -> {
                    log.error("Department not found with id: {}", dto.getDeptId());
                    return new ResourceNotFoundException("Department not found with id: " + dto.getDeptId());
                }));

        existingEmp.setDesignation(designationRepository.findById(dto.getDesigId())
                .orElseThrow(() -> {
                    log.error("Designation not found with id: {}", dto.getDesigId());
                    return new ResourceNotFoundException("Designation not found with id: " + dto.getDesigId());
                }));

        employeeRepository.save(existingEmp);
        log.info("Employee updated successfully with id: {}", id);

        EmployeeDTO result = modelMapper.map(existingEmp, EmployeeDTO.class);
        result.setDeptId(existingEmp.getDepartment().getDeptId());
        result.setDesigId(existingEmp.getDesignation().getDesigId());
        return result;
    }

    // Delete Employee
    public void deleteEmployee(Long id) {
        log.info("Deleting employee with id: {}", id);
        employeeRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Employee not found with id: {}", id);
                    return new ResourceNotFoundException("Employee not found with id: " + id);
                });

        employeeRepository.deleteById(id);
        log.info("Employee deleted successfully with id: {}", id);
    }

    // Search Employees with Pagination
    public Page<EmployeeDTO> searchEmployees(String keyword, int page, int size) {
        log.info("Searching employees with keyword: '{}' - page: {}, size: {}", keyword, page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by("empId").ascending());
        Page<EmployeeDTO> result = employeeRepository.searchEmployees(keyword, pageable)
                .map(emp -> {
                    EmployeeDTO dto = modelMapper.map(emp, EmployeeDTO.class);
                    dto.setDeptId(emp.getDepartment() != null ? emp.getDepartment().getDeptId() : null);
                    dto.setDesigId(emp.getDesignation() != null ? emp.getDesignation().getDesigId() : null);
                    return dto;
                });
        log.info("Found {} employees matching keyword '{}'", result.getContent().size(), keyword);
        return result;
    }

    // Count total Employees
    public long countEmployee() {
        long count = employeeRepository.count();
        log.info("Total number of employees: {}", count);
        return count;
    }
}
