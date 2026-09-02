package com.clinic.doctor.dto;

public class StudyResponse {

    private Long id;
    private Integer startYear;
    private Integer endYear;
    private String degree;
    private String university;
    private String field;

    public StudyResponse() {
    }

    public StudyResponse(
            Long id,
            Integer startYear,
            Integer endYear,
            String degree,
            String university,
            String field
    ) {
        this.id = id;
        this.startYear = startYear;
        this.endYear = endYear;
        this.degree = degree;
        this.university = university;
        this.field = field;
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

    public String getDegree() {
        return degree;
    }

    public String getUniversity() {
        return university;
    }

    public String getField() {
        return field;
    }
}