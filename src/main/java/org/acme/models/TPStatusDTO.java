package org.acme.models;

public class TPStatusDTO {
    private Long id;
    private EtudiantDTO etudiant;
    private TravailPratiqueDTO travailPratique;
    private boolean renduEtudiant;

    // Constructeurs
    public TPStatusDTO() {}

    public TPStatusDTO(Long id, EtudiantDTO etudiant, TravailPratiqueDTO travailPratique, boolean renduEtudiant) {
        this.id = id;
        this.etudiant = etudiant;
        this.travailPratique = travailPratique;
        this.renduEtudiant = renduEtudiant;
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public EtudiantDTO getEtudiant() { return etudiant; }
    public void setEtudiant(EtudiantDTO etudiant) { this.etudiant = etudiant; }

    public TravailPratiqueDTO getTravailPratique() { return travailPratique; }
    public void setTravailPratique(TravailPratiqueDTO travailPratique) { this.travailPratique = travailPratique; }

    public boolean isRenduEtudiant() { return renduEtudiant; }
    public void setRenduEtudiant(boolean renduEtudiant) { this.renduEtudiant = renduEtudiant; }
}
