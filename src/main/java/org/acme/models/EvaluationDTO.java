package org.acme.models;

public abstract class EvaluationDTO {
    private Long id;
    private String nom;
    private String date;
    private CoursDTO cours;
    private RenduDTO rendu;

    // Constructeurs
    public EvaluationDTO() {}

    public EvaluationDTO(Long id, String nom, String date, CoursDTO cours, RenduDTO rendu) {
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

    public CoursDTO getCours() { return cours; }
    public void setCours(CoursDTO cours) { this.cours = cours; }

    public RenduDTO getRendu() { return rendu; }
    public void setRendu(RenduDTO rendu) { this.rendu = rendu; }
}
