package com.clinic.doctor.service;

import com.clinic.doctor.dto.ExperienceRequest;
import com.clinic.doctor.dto.ExperienceResponse;
import com.clinic.doctor.entity.Doctor;
import com.clinic.doctor.entity.Experience;
import com.clinic.doctor.repository.DoctorRepository;
import com.clinic.doctor.repository.ExperienceRepository;
import org.springframework.stereotype.Service;
import com.clinic.doctor.exception.ResourceNotFoundException;

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