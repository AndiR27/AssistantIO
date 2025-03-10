package org.acme.models;
import java.util.Set;

/**
 * Classe POJO représentant un Travail Pratique sans exposer l'entité JPA.
 */
public class TravailPratiqueDTO {

    private Long id;
    private int no;
    private CoursDTO cours;
    private RenduDTO rendu;
    private Set<TPStatusDTO> statusEtudiants;

    // Constructeurs
    public TravailPratiqueDTO() {}

    public TravailPratiqueDTO(Long id, int no, CoursDTO cours, RenduDTO rendu, Set<TPStatusDTO> statusEtudiants) {
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

    public CoursDTO getCours() { return cours; }
    public void setCours(CoursDTO cours) { this.cours = cours; }

    public RenduDTO getRendu() { return rendu; }
    public void setRendu(RenduDTO rendu) { this.rendu = rendu; }

    public Set<TPStatusDTO> getStatusEtudiants() { return statusEtudiants; }
    public void setStatusEtudiants(Set<TPStatusDTO> statusEtudiants) { this.statusEtudiants = statusEtudiants; }
}
