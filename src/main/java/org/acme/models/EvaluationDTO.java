package org.acme.models;

public class EvaluationDTO {
    private Long id;
    private String nom;
    private String date;
    private CourseDTO cours;
    private SubmissionDTO rendu;

    // Constructeurs
    public EvaluationDTO() {}

    public EvaluationDTO(Long id, String nom, String date, CourseDTO cours, SubmissionDTO rendu) {
        this.id = id;
        this.nom = nom;
        this.date = date;
        this.cours = cours;
        this.rendu = rendu;
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public CourseDTO getCours() { return cours; }
    public void setCours(CourseDTO cours) { this.cours = cours; }

    public SubmissionDTO getRendu() { return rendu; }
    public void setRendu(SubmissionDTO rendu) { this.rendu = rendu; }
}
