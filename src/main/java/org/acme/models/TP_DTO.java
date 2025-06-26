package org.acme.models;
import java.util.Set;

/**
 * Classe POJO représentant un Travail Pratique sans exposer l'entité JPA.
 */
public class TP_DTO {

    private Long id;
    private int no;
    private CourseDTO cours;
    private SubmissionDTO rendu;
    private Set<TPStatusDTO> statusEtudiants;

    // Constructeurs
    public TP_DTO() {}

    public TP_DTO(Long id, int no, CourseDTO cours, SubmissionDTO rendu, Set<TPStatusDTO> statusEtudiants) {
        this.id = id;
        this.no = no;
        this.cours = cours;
        this.rendu = rendu;
        this.statusEtudiants = statusEtudiants;
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public int getNo() { return no; }
    public void setNo(int no) { this.no = no; }

    public CourseDTO getCours() { return cours; }
    public void setCours(CourseDTO cours) { this.cours = cours; }

    public SubmissionDTO getRendu() { return rendu; }
    public void setRendu(SubmissionDTO rendu) { this.rendu = rendu; }

    public Set<TPStatusDTO> getStatusEtudiants() { return statusEtudiants; }
    public void setStatusEtudiants(Set<TPStatusDTO> statusEtudiants) { this.statusEtudiants = statusEtudiants; }
}
