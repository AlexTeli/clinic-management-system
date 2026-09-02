package com.clinic.doctor.repository;

import com.clinic.doctor.entity.Study;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudyRepository extends JpaRepository<Study, Long> {

    List<Study> findByDoctorId(Long doctorId);
}