package org.acme.models;

import org.acme.enums.SemesterType;

import java.time.LocalDateTime;

public class ExamDTO extends EvaluationDTO{

    private SemesterType semester;

    public ExamDTO(){}

    public ExamDTO(Long id, String name, LocalDateTime date, CourseDTO course, SubmissionDTO rendu, SemesterType semester) {
        super(id, name, date, course, rendu);
        this.semester = semester;
    }

    public SemesterType getSemester() { return semester; }
    public void setSemester(SemesterType semester) { this.semester = semester; }

}
