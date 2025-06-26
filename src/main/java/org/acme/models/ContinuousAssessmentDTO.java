package org.acme.models;

public class ContinuousAssessmentDTO extends EvaluationDTO {
    private int coefficient;
    private int numero;

    public ContinuousAssessmentDTO() {
    }

    public ContinuousAssessmentDTO(Long id, String nom, String date, CourseDTO cours, SubmissionDTO rendu, int coefficient, int numero) {
        super(id, nom, date, cours, rendu);
        this.coefficient = coefficient;
        this.numero = numero;
    }

    public int getCoefficient() {
        return coefficient;
    }

    public void setCoefficient(int coefficient) {
        this.coefficient = coefficient;
    }

    public int getNumero() {
        return numero;
    }

    public void setNumero(int numero) {
        this.numero = numero;
    }

}
