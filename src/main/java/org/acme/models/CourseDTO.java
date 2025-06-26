package org.acme.models;

import org.acme.enums.CourseType;
import org.acme.enums.SemesterType;

import java.util.List;

public class CourseDTO {
    private Long id;
    private String nom;
    private String code;
    private SemesterType semestre;
    private int annee;
    private String prof;
    private CourseType typeCours;
    private List<StudentDTO> etudiantsInscrits;
    private List<TP_DTO> travauxPratiques;
    private List<EvaluationDTO> evaluations;

    // Constructeurs
    public CourseDTO() {}

    public CourseDTO(Long id, String nom, String code, SemesterType semestre, int annee, String prof, CourseType typeCours,
                     List<StudentDTO> etudiantsInscrits, List<TP_DTO> travauxPratiques, List<EvaluationDTO> evaluations) {
        this.id = id;
        this.nom = nom;
        this.code = code;
        this.semestre = semestre;
        this.annee = annee;
        this.prof = prof;
        this.typeCours = typeCours;
        this.etudiantsInscrits = etudiantsInscrits;
        this.travauxPratiques = travauxPratiques;
        this.evaluations = evaluations;
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public SemesterType getSemestre() { return semestre; }
    public void setSemestre(SemesterType semestre) { this.semestre = semestre; }

    public int getAnnee() { return annee; }
    public void setAnnee(int annee) { this.annee = annee; }

    public String getProf() { return prof; }
    public void setProf(String prof) { this.prof = prof; }

    public CourseType getTypeCours() { return typeCours; }
    public void setTypeCours(CourseType typeCours) { this.typeCours = typeCours; }

    public List<StudentDTO> getEtudiantsInscrits() { return etudiantsInscrits; }
    public void setEtudiantsInscrits(List<StudentDTO> etudiantsInscrits) { this.etudiantsInscrits = etudiantsInscrits; }

    public List<TP_DTO> getTravauxPratiques() { return travauxPratiques; }
    public void setTravauxPratiques(List<TP_DTO> travauxPratiques) { this.travauxPratiques = travauxPratiques; }

    public List<EvaluationDTO> getEvaluations() { return evaluations; }
    public void setEvaluations(List<EvaluationDTO> evaluations) { this.evaluations = evaluations; }
}
