package models;


import java.util.ArrayList;
import java.util.List;

import enums.*;

public class StudentDTO {
    private Long id;
    private String name;
    private String email;
    private StudyType studyType;
    private List<CourseDTO> courseStudentList = new ArrayList<>();

    // Constructeurs
    public StudentDTO() {}

    public StudentDTO(Long id, String name, String email, StudyType studyType, List<CourseDTO> courseStudentList) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.studyType = studyType;
        this.courseStudentList = courseStudentList;
    }

    public StudentDTO(Long id, String name, String email, StudyType studyType) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.studyType = studyType;
        this.courseStudentList = new ArrayList<>();
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public StudyType getStudyType() { return studyType; }
    public void setStudyType(StudyType studyType) { this.studyType = studyType; }

    public List<CourseDTO> getCourseStudentList() { return courseStudentList; }
    public void setCourseStudentList(List<CourseDTO> cours) { this.courseStudentList = cours; }
}
