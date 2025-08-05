package org.acme.models;

import jakarta.validation.constraints.NotBlank;
import org.acme.enums.CourseType;
import org.acme.enums.SemesterType;

import java.util.ArrayList;
import java.util.List;

public class CourseDTO {
    private Long id;
    private String name;

    @NotBlank(message = "Code is mandatory")
    private String code;
    private SemesterType semester;
    private int year_course;
    private String teacher;
    private CourseType courseType;
    private List<StudentDTO> studentList;
    private List<TP_DTO> tpsList;
    private List<EvaluationDTO> evaluations;

    // Constructeurs
    public CourseDTO() {}

    public CourseDTO(Long id, String name, String code, SemesterType semester, int year_course, String teacher, CourseType typeCours,
                     List<StudentDTO> studentList, List<TP_DTO> tpsList, List<EvaluationDTO> evaluations) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.semester = semester;
        this.year_course = year_course;
        this.teacher = teacher;
        this.courseType = typeCours;
        this.studentList = studentList;
        this.tpsList = tpsList;
        this.evaluations = evaluations;
    }

    public CourseDTO(Long id, String name, String code, SemesterType semester, int year_course, String teacher, CourseType courseType) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.semester = semester;
        this.year_course = year_course;
        this.teacher = teacher;
        this.courseType = courseType;
        this.studentList = new ArrayList<>();
        this.tpsList = new ArrayList<>();
        this.evaluations = new ArrayList<>();
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public SemesterType getSemester() { return semester; }
    public void setSemester(SemesterType semester) { this.semester = semester; }

    public int getYear_course() { return year_course; }
    public void setYear_course(int year_course) { this.year_course = year_course; }

    public String getTeacher() { return teacher; }
    public void setTeacher(String teacher) { this.teacher = teacher; }

    public CourseType getCourseType() { return courseType; }
    public void setCourseType(CourseType courseType) { this.courseType = courseType; }

    public List<StudentDTO> getStudentList() { return studentList; }
    public void setStudentList(List<StudentDTO> studentList) { this.studentList = studentList; }

    public List<TP_DTO> getTpsList() { return tpsList; }
    public void setTpsList(List<TP_DTO> tpsList) { this.tpsList = tpsList; }

    public List<EvaluationDTO> getEvaluations() { return evaluations; }
    public void setEvaluations(List<EvaluationDTO> evaluations) { this.evaluations = evaluations; }
}
