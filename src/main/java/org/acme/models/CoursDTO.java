package org.acme.models;

import java.util.List;

public class CoursDTO {
    private Long id;
    private String nom;
    private String code;
    private TypeSemestreDTO semestre;
    private int annee;
    private String prof;
    private TypeCoursDTO typeCours;
    private List<EtudiantDTO> etudiantsInscrits;
    private List<TravailPratiqueDTO> travauxPratiques;
    private List<EvaluationDTO> evaluations;

    // Constructeurs
    public CoursDTO() {}

    public CoursDTO(Long id, String nom, String code, TypeSemestreDTO semestre, int annee, String prof, TypeCoursDTO typeCours,
                    List<EtudiantDTO> etudiantsInscrits, List<TravailPratiqueDTO> travauxPratiques, List<EvaluationDTO> evaluations) {
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

    public TypeSemestreDTO getSemestre() { return semestre; }
    public void setSemestre(TypeSemestreDTO semestre) { this.semestre = semestre; }

    public int getAnnee() { return annee; }
    public void setAnnee(int annee) { this.annee = annee; }

    public String getProf() { return prof; }
    public void setProf(String prof) { this.prof = prof; }

    public TypeCoursDTO getTypeCours() { return typeCours; }
    public void setTypeCours(TypeCoursDTO typeCours) { this.typeCours = typeCours; }

    public List<EtudiantDTO> getEtudiantsInscrits() { return etudiantsInscrits; }
    public void setEtudiantsInscrits(List<EtudiantDTO> etudiantsInscrits) { this.etudiantsInscrits = etudiantsInscrits; }

    public List<TravailPratiqueDTO> getTravauxPratiques() { return travauxPratiques; }
    public void setTravauxPratiques(List<TravailPratiqueDTO> travauxPratiques) { this.travauxPratiques = travauxPratiques; }

    public List<EvaluationDTO> getEvaluations() { return evaluations; }
    public void setEvaluations(List<EvaluationDTO> evaluations) { this.evaluations = evaluations; }
}
