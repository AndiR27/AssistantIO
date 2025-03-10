package org.acme.models;

public class ExamenDTO extends EvaluationDTO{

    private TypeSemestreDTO semestre;

    public ExamenDTO(){}

    public ExamenDTO(Long id, String nom, String date, CoursDTO cours, RenduDTO rendu, TypeSemestreDTO semestre) {
        super(id, nom, date, cours, rendu);
        this.semestre = semestre;
    }

    public TypeSemestreDTO getSemestre() { return semestre; }
    public void setSemestre(TypeSemestreDTO semestre) { this.semestre = semestre; }

}
