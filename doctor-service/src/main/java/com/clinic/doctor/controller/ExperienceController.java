package com.clinic.doctor.controller;

import com.clinic.doctor.dto.ExperienceRequest;
import com.clinic.doctor.dto.ExperienceResponse;
import com.clinic.doctor.service.ExperienceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/doctors/{doctorId}/experiences")
public class ExperienceController {

    private final ExperienceService experienceService;

    public ExperienceController(ExperienceService experienceService) {
        this.experienceService = experienceService;
    }

    @GetMapping
    public ResponseEntity<List<ExperienceResponse>> getDoctorExperiences(
            @PathVariable Long doctorId
    ) {

        return ResponseEntity.ok(
                experienceService.getDoctorExperiences(doctorId)
        );
    }

    @PostMapping
    public ResponseEntity<ExperienceResponse> addExperience(
            @PathVariable Long doctorId,
            @Valid @RequestBody ExperienceRequest request
    ) {

        ExperienceResponse response =
                experienceService.addExperience(
                        doctorId,
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PutMapping("/{experienceId}")
    public ResponseEntity<ExperienceResponse> updateExperience(
            @PathVariable Long doctorId,
            @PathVariable Long experienceId,
            @Valid @RequestBody ExperienceRequest request
    ) {

        return ResponseEntity.ok(
                experienceService.updateExperience(
                        doctorId,
                        experienceId,
                        request
                )
        );
    }

    @DeleteMapping("/{experienceId}")
    public ResponseEntity<Void> deleteExperience(
            @PathVariable Long doctorId,
            @PathVariable Long experienceId
    ) {

        experienceService.deleteExperience(
                doctorId,
                experienceId
        );

        return ResponseEntity.noContent().build();
    }
}