package com.clinic.doctor.dto;

public class ExperienceResponse {

    private Long id;
    private Integer startYear;
    private Integer endYear;
    private String position;
    private String hospital;
    private String location;

    public ExperienceResponse() {
    }

    public ExperienceResponse(
            Long id,
            Integer startYear,
            Integer endYear,
            String position,
            String hospital,
            String location
    ) {
        this.id = id;
        this.startYear = startYear;
        this.endYear = endYear;
        this.position = position;
        this.hospital = hospital;
        this.location = location;
    }

    public Long getId() {
        return id;
    }

    public Integer getStartYear() {
        return startYear;
    }

    public Integer getEndYear() {
        return endYear;
    }

    public String getPosition() {
        return position;
    }

    public String getHospital() {
        return hospital;
    }

    public String getLocation() {
        return location;
    }
}