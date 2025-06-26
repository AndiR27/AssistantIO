package org.acme.models;

import org.acme.enums.SemesterType;

public class ExamDTO extends EvaluationDTO{

    private SemesterType semestre;

    public ExamDTO(){}

    public ExamDTO(Long id, String nom, String date, CourseDTO cours, SubmissionDTO rendu, SemesterType semestre) {
        super(id, nom, date, cours, rendu);
        this.semestre = semestre;
    }

    public SemesterType getSemestre() { return semestre; }
    public void setSemestre(SemesterType semestre) { this.semestre = semestre; }

}
