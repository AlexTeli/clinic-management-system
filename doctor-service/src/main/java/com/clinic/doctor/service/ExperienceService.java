package com.clinic.doctor.service;

import com.clinic.doctor.dto.ExperienceRequest;
import com.clinic.doctor.dto.ExperienceResponse;
import com.clinic.doctor.entity.Doctor;
import com.clinic.doctor.entity.Experience;
import com.clinic.doctor.repository.DoctorRepository;
import com.clinic.doctor.repository.ExperienceRepository;
import org.springframework.stereotype.Service;
import com.clinic.doctor.exception.ResourceNotFoundException;
import com.clinic.doctor.exception.ForbiddenOperationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

@Service
public class ExperienceService {

    private final ExperienceRepository experienceRepository;
    private final DoctorRepository doctorRepository;

    public ExperienceService(
            ExperienceRepository experienceRepository,
            DoctorRepository doctorRepository
    ) {
        this.experienceRepository = experienceRepository;
        this.doctorRepository = doctorRepository;
    }

    public List<ExperienceResponse> getDoctorExperiences(Long doctorId) {

        if (!doctorRepository.existsById(doctorId)) {
            throw new ResourceNotFoundException(
                    "Doctor not found with id: " + doctorId
            );
        }

        return experienceRepository.findByDoctorId(doctorId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public ExperienceResponse addExperience(
            Long doctorId,
            ExperienceRequest request
    ) {
        checkOwnership(doctorId);

        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Doctor not found with id: " + doctorId
                        )
                );

        Experience experience = new Experience();

        experience.setStartYear(request.getStartYear());
        experience.setEndYear(request.getEndYear());
        experience.setPosition(request.getPosition());
        experience.setHospital(request.getHospital());
        experience.setLocation(request.getLocation());

        experience.setDoctor(doctor);

        Experience savedExperience =
                experienceRepository.save(experience);

        return toResponse(savedExperience);
    }

    public ExperienceResponse updateExperience(
            Long doctorId,
            Long experienceId,
            ExperienceRequest request
    ) {
        checkOwnership(doctorId);

        Experience experience = experienceRepository
                .findById(experienceId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Experience not found with id: "
                                        + experienceId
                        )
                );

        if (!experience.getDoctor().getId().equals(doctorId)) {
            throw new ResourceNotFoundException(
                    "Experience does not belong to this doctor"
            );
        }

        experience.setStartYear(request.getStartYear());
        experience.setEndYear(request.getEndYear());
        experience.setPosition(request.getPosition());
        experience.setHospital(request.getHospital());
        experience.setLocation(request.getLocation());

        Experience updatedExperience =
                experienceRepository.save(experience);

        return toResponse(updatedExperience);
    }

    public void deleteExperience(
            Long doctorId,
            Long experienceId
    ) {
        checkOwnership(doctorId);

        Experience experience = experienceRepository
                .findById(experienceId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Experience not found with id: "
                                        + experienceId
                        )
                );

        if (!experience.getDoctor().getId().equals(doctorId)) {
            throw new ResourceNotFoundException(
                    "Experience does not belong to this doctor"
            );
        }

        experienceRepository.delete(experience);
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

    private ExperienceResponse toResponse(Experience experience) {

        return new ExperienceResponse(
                experience.getId(),
                experience.getStartYear(),
                experience.getEndYear(),
                experience.getPosition(),
                experience.getHospital(),
                experience.getLocation()
        );
    }
}