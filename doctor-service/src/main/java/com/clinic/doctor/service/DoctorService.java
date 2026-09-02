package com.clinic.doctor.service;

import com.clinic.doctor.dto.DoctorRequest;
import com.clinic.doctor.dto.DoctorResponse;
import com.clinic.doctor.entity.Doctor;
import com.clinic.doctor.repository.DoctorRepository;
import org.springframework.stereotype.Service;
import com.clinic.doctor.exception.DuplicateResourceException;
import com.clinic.doctor.exception.ResourceNotFoundException;

import java.util.List;

@Service
public class DoctorService {

    private final DoctorRepository doctorRepository;

    public DoctorService(DoctorRepository doctorRepository) {
        this.doctorRepository = doctorRepository;
    }

    public DoctorResponse createDoctor(DoctorRequest request) {

        if (doctorRepository.existsByUserId(request.getUserId())) {
            throw new DuplicateResourceException(
                    "Doctor profile already exists for this user"
            );
        }

        if (doctorRepository.existsByLicenseNumber(request.getLicenseNumber())) {
            throw new DuplicateResourceException(
                    "License number already exists"
            );
        }

        Doctor doctor = new Doctor();

        doctor.setUserId(request.getUserId());
        doctor.setFirstName(request.getFirstName());
        doctor.setLastName(request.getLastName());
        doctor.setSpecialization(request.getSpecialization());
        doctor.setLicenseNumber(request.getLicenseNumber());
        doctor.setPhone(request.getPhone());

        Doctor savedDoctor = doctorRepository.save(doctor);

        return toResponse(savedDoctor);
    }

    public List<DoctorResponse> getAllDoctors() {

        return doctorRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public DoctorResponse getDoctorById(Long id) {

        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Doctor not found with id: " + id
                        )
                );

        return toResponse(doctor);
    }

    public DoctorResponse getDoctorByUserId(Long userId) {

        Doctor doctor = doctorRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Doctor not found for user id: " + userId
                        )
                );

        return toResponse(doctor);
    }

    public DoctorResponse updateDoctor(
            Long id,
            DoctorRequest request
    ) {

        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Doctor not found with id: " + id
                        )
                );

        doctor.setFirstName(request.getFirstName());
        doctor.setLastName(request.getLastName());
        doctor.setSpecialization(request.getSpecialization());
        doctor.setLicenseNumber(request.getLicenseNumber());
        doctor.setPhone(request.getPhone());

        Doctor updatedDoctor = doctorRepository.save(doctor);

        return toResponse(updatedDoctor);
    }

    public void deleteDoctor(Long id) {

        if (!doctorRepository.existsById(id)) {
            throw new ResourceNotFoundException(
                    "Doctor not found with id: " + id
            );
        }

        doctorRepository.deleteById(id);
    }

    private DoctorResponse toResponse(Doctor doctor) {

        return new DoctorResponse(
                doctor.getId(),
                doctor.getUserId(),
                doctor.getFirstName(),
                doctor.getLastName(),
                doctor.getSpecialization(),
                doctor.getLicenseNumber(),
                doctor.getPhone()
        );
    }
}