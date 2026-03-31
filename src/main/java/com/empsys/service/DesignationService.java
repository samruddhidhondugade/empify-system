package com.empsys.service;

import com.empsys.dto.DesignationDTO;
import com.empsys.entity.Designation;
import com.empsys.exception.ResourceNotFoundException;
import com.empsys.repository.DesignationRepository;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;
@Slf4j
@Service
public class DesignationService {

    @Autowired
    private DesignationRepository designationRepository;

    @Autowired
    private ModelMapper modelMapper;


    // Get all designations sorted
    public List<DesignationDTO> getAllDesignations() {
        log.info("Fetching all designations...");

        List<DesignationDTO> list = designationRepository.findAll(Sort.by(Sort.Direction.ASC, "desigId"))
                .stream()
                .map(desig -> modelMapper.map(desig, DesignationDTO.class))
                .collect(Collectors.toList());

        log.info("Total designations fetched: {}", list.size());
        return list;
    }

    // Get designation by ID
    public DesignationDTO getDesignationById(Long id) {
        log.info("Fetching designation by ID: {}", id);

        Designation desig = designationRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Designation not found with ID: {}", id);
                    return new ResourceNotFoundException("Designation not found with ID: " + id);
                });

        log.info("Designation found: {}", desig.getDesigName());
        return modelMapper.map(desig, DesignationDTO.class);
    }

    // Add designation
    public DesignationDTO addDesignation(DesignationDTO dto) {
        log.info("Adding new designation: {}", dto.getDesigName());

        Designation designation = modelMapper.map(dto, Designation.class);
        designationRepository.save(designation);

        log.info("Designation saved successfully with ID: {}", designation.getDesigId());
        return modelMapper.map(designation, DesignationDTO.class);
    }

    // Update designation
    public DesignationDTO updateDesignation(Long id, DesignationDTO dto) {
        log.info("Updating designation with ID: {}", id);

        Designation existingDesig = designationRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Designation not found with ID: {}", id);
                    return new ResourceNotFoundException("Designation not found with ID: " + id);
                });

        existingDesig.setDesigName(dto.getDesigName());
        designationRepository.save(existingDesig);

        log.info("Designation updated successfully for ID: {}", id);
        return modelMapper.map(existingDesig, DesignationDTO.class);
    }

    // Delete designation
    public void deleteDesignation(Long id) {
        log.warn("Deleting designation with ID: {}", id);

        designationRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Designation not found with ID: {}", id);
                    return new ResourceNotFoundException("Designation not found with ID: " + id);
                });

        designationRepository.deleteById(id);
        log.info("Designation deleted successfully for ID: {}", id);
    }

    // Count designation
    public long countDesignation() {
        long count = designationRepository.count();
        log.info("Total designations count: {}", count);
        return count;
    }
}
