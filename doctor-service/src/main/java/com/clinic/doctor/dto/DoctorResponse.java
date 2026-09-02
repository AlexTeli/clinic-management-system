package com.clinic.doctor.dto;

public class DoctorResponse {

    private Long id;
    private Long userId;
    private String firstName;
    private String lastName;
    private String specialization;
    private String licenseNumber;
    private String phone;

    public DoctorResponse() {
    }

    public DoctorResponse(
            Long id,
            Long userId,
            String firstName,
            String lastName,
            String specialization,
            String licenseNumber,
            String phone
    ) {
        this.id = id;
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.specialization = specialization;
        this.licenseNumber = licenseNumber;
        this.phone = phone;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getSpecialization() {
        return specialization;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public String getPhone() {
        return phone;
    }
}