package com.clinic.doctor.controller;

import com.clinic.doctor.dto.StudyRequest;
import com.clinic.doctor.dto.StudyResponse;
import com.clinic.doctor.service.StudyService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/doctors/{doctorId}/studies")
public class StudyController {

    private final StudyService studyService;

    public StudyController(StudyService studyService) {
        this.studyService = studyService;
    }

    @GetMapping
    public ResponseEntity<List<StudyResponse>> getDoctorStudies(
            @PathVariable Long doctorId
    ) {

        return ResponseEntity.ok(
                studyService.getDoctorStudies(doctorId)
        );
    }

    @PostMapping
    public ResponseEntity<StudyResponse> addStudy(
            @PathVariable Long doctorId,
            @Valid @RequestBody StudyRequest request
    ) {

        StudyResponse response =
                studyService.addStudy(
                        doctorId,
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PutMapping("/{studyId}")
    public ResponseEntity<StudyResponse> updateStudy(
            @PathVariable Long doctorId,
            @PathVariable Long studyId,
            @Valid @RequestBody StudyRequest request
    ) {

        return ResponseEntity.ok(
                studyService.updateStudy(
                        doctorId,
                        studyId,
                        request
                )
        );
    }

    @DeleteMapping("/{studyId}")
    public ResponseEntity<Void> deleteStudy(
            @PathVariable Long doctorId,
            @PathVariable Long studyId
    ) {

        studyService.deleteStudy(
                doctorId,
                studyId
        );

        return ResponseEntity.noContent().build();
    }
}