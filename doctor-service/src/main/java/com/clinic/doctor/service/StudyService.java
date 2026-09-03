package com.clinic.doctor.service;

import com.clinic.doctor.dto.StudyRequest;
import com.clinic.doctor.dto.StudyResponse;
import com.clinic.doctor.entity.Doctor;
import com.clinic.doctor.entity.Study;
import com.clinic.doctor.repository.DoctorRepository;
import com.clinic.doctor.repository.StudyRepository;
import org.springframework.stereotype.Service;
import com.clinic.doctor.exception.ResourceNotFoundException;
import com.clinic.doctor.exception.ForbiddenOperationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

@Service
public class StudyService {

    private final StudyRepository studyRepository;
    private final DoctorRepository doctorRepository;

    public StudyService(
            StudyRepository studyRepository,
            DoctorRepository doctorRepository
    ) {
        this.studyRepository = studyRepository;
        this.doctorRepository = doctorRepository;
    }

    public List<StudyResponse> getDoctorStudies(Long doctorId) {

        if (!doctorRepository.existsById(doctorId)) {
            throw new ResourceNotFoundException(
                    "Doctor not found with id: " + doctorId
            );
        }

        return studyRepository.findByDoctorId(doctorId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public StudyResponse addStudy(
            Long doctorId,
            StudyRequest request
    ) {
        checkOwnership(doctorId);

        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Doctor not found with id: " + doctorId
                        )
                );

        Study study = new Study();

        study.setStartYear(request.getStartYear());
        study.setEndYear(request.getEndYear());
        study.setDegree(request.getDegree());
        study.setUniversity(request.getUniversity());
        study.setField(request.getField());

        study.setDoctor(doctor);

        Study savedStudy = studyRepository.save(study);

        return toResponse(savedStudy);
    }

    public StudyResponse updateStudy(
            Long doctorId,
            Long studyId,
            StudyRequest request
    ) {
        checkOwnership(doctorId);

        Study study = studyRepository
                .findById(studyId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Study not found with id: " + studyId
                        )
                );

        if (!study.getDoctor().getId().equals(doctorId)) {
            throw new ResourceNotFoundException(
                    "Study does not belong to this doctor"
            );
        }

        study.setStartYear(request.getStartYear());
        study.setEndYear(request.getEndYear());
        study.setDegree(request.getDegree());
        study.setUniversity(request.getUniversity());
        study.setField(request.getField());

        Study updatedStudy = studyRepository.save(study);

        return toResponse(updatedStudy);
    }

    public void deleteStudy(
            Long doctorId,
            Long studyId
    ) {
        checkOwnership(doctorId);

        Study study = studyRepository
                .findById(studyId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Study not found with id: " + studyId
                        )
                );

        if (!study.getDoctor().getId().equals(doctorId)) {
            throw new ResourceNotFoundException(
                    "Study does not belong to this doctor"
            );
        }

        studyRepository.delete(study);
    }

    private void checkOwnership(Long doctorId) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        Long currentUserId = (Long) authentication.getPrincipal();

        boolean isAdmin = authentication.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals("ROLE_ADMIN")
                );

        if (isAdmin) {
            return;
        }

        Doctor doctor = doctorRepository.findByUserId(currentUserId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Doctor profile not found for current user"
                        )
                );

        if (!doctor.getId().equals(doctorId)) {
            throw new ForbiddenOperationException(
                    "You are not allowed to modify this doctor's data"
            );
        }
    }

    private StudyResponse toResponse(Study study) {

        return new StudyResponse(
                study.getId(),
                study.getStartYear(),
                study.getEndYear(),
                study.getDegree(),
                study.getUniversity(),
                study.getField()
        );
    }
}