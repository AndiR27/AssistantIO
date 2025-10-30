package models;

import java.time.LocalDateTime;

public class ContinuousAssessmentDTO extends EvaluationDTO {
    private int coefficient;
    private int number;

    public ContinuousAssessmentDTO() {
    }

    public ContinuousAssessmentDTO(Long id, String nom, LocalDateTime date, CourseDTO cours, SubmissionDTO rendu, int coefficient, int number) {
        super(id, nom, date, cours, rendu);
        this.coefficient = coefficient;
        this.number = number;
    }

    public int getCoefficient() {
        return coefficient;
    }

    public void setCoefficient(int coefficient) {
        this.coefficient = coefficient;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

}
