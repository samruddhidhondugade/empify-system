package com.empsys.controller;

import com.empsys.dto.DesignationDTO;
import com.empsys.service.DesignationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/designations")
@CrossOrigin(origins = "*")
@Slf4j
public class DesignationController {

    @Autowired
    private DesignationService designationService;


    @GetMapping
    public ResponseEntity<List<DesignationDTO>> getAllDesignations() {
        log.info("Request received: Get all designations");
        List<DesignationDTO> designations = designationService.getAllDesignations();
        log.info("Response: {} designations sent", designations.size());
        return ResponseEntity.ok(designations);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DesignationDTO> getDesignationById(@PathVariable Long id) {
        log.info("Request received: Get designation by ID {}", id);
        DesignationDTO dto = designationService.getDesignationById(id);
        log.info("Response: Designation fetched for ID {}", id);
        return ResponseEntity.ok(dto);
    }

    @PostMapping
    public ResponseEntity<DesignationDTO> addDesignation(@RequestBody DesignationDTO dto) {
        log.info("Request received: Add designation {}", dto.getDesigName());
        DesignationDTO createdDto = designationService.addDesignation(dto);
        log.info("Designation created successfully with ID {}", createdDto.getDesigId());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteDesignation(@PathVariable Long id) {
        log.warn("Request received: Delete designation with ID {}", id);
        designationService.deleteDesignation(id);
        log.info("Designation deleted successfully, ID {}", id);
        return ResponseEntity.ok("Designation deleted successfully");
    }

    @PutMapping("/{id}")
    public ResponseEntity<DesignationDTO> updateDesignation(@PathVariable Long id, @RequestBody DesignationDTO dto) {
        log.info("Request received: Update designation with ID {} and new name {}", id, dto.getDesigName());
        dto.setDesigId(id);
        DesignationDTO updatedDto = designationService.updateDesignation(id, dto);
        log.info("Designation updated successfully for ID {}", id);
        return ResponseEntity.ok(updatedDto);
    }

    @GetMapping("/count")
    public ResponseEntity<Long> countDesignation() {
        log.info("Request received: Count total designations");
        long count = designationService.countDesignation();
        log.info("Total designations count = {}", count);
        return ResponseEntity.ok(count);
    }
}