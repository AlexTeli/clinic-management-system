package com.clinic.doctor.repository;

import com.clinic.doctor.entity.Experience;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExperienceRepository extends JpaRepository<Experience, Long> {

    List<Experience> findByDoctorId(Long doctorId);
}