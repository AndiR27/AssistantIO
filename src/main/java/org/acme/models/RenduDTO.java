package org.acme.models;

public class RenduDTO {
    private Long id;
    private String nomFichier;
    private String cheminStockage;
    private String cheminFichierStructure;

    // Constructeurs
    public RenduDTO() {}

    public RenduDTO(Long id, String nomFichier, String cheminStockage, String cheminFichierStructure) {
        this.id = id;
        this.nomFichier = nomFichier;
        this.cheminStockage = cheminStockage;
        this.cheminFichierStructure = cheminFichierStructure;
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNomFichier() { return nomFichier; }
    public void setNomFichier(String nomFichier) { this.nomFichier = nomFichier; }

    public String getCheminStockage() { return cheminStockage; }
    public void setCheminStockage(String cheminStockage) { this.cheminStockage = cheminStockage; }

    public String getCheminFichierStructure() { return cheminFichierStructure; }
    public void setCheminFichierStructure(String cheminFichierStructure) { this.cheminFichierStructure = cheminFichierStructure; }
}
