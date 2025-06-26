package org.acme.models;

public class TPStatusDTO {
    private Long id;
    private StudentDTO etudiant;
    private TP_DTO travailPratique;
    private boolean renduEtudiant;

    // Constructeurs
    public TPStatusDTO() {}

    public TPStatusDTO(Long id, StudentDTO etudiant, TP_DTO travailPratique, boolean renduEtudiant) {
        this.id = id;
        this.etudiant = etudiant;
        this.travailPratique = travailPratique;
        this.renduEtudiant = renduEtudiant;
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public StudentDTO getEtudiant() { return etudiant; }
    public void setEtudiant(StudentDTO etudiant) { this.etudiant = etudiant; }

    public TP_DTO getTravailPratique() { return travailPratique; }
    public void setTravailPratique(TP_DTO travailPratique) { this.travailPratique = travailPratique; }

    public boolean isRenduEtudiant() { return renduEtudiant; }
    public void setRenduEtudiant(boolean renduEtudiant) { this.renduEtudiant = renduEtudiant; }
}
