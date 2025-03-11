package org.acme.models;

import java.util.List;

public class EtudiantDTO {
    private Long id;
    private String nom;
    private String email;
    private TypeEtudeDTO typeEtude;
    private List<CoursDTO> coursEtudiant;

    // Constructeurs
    public EtudiantDTO() {}

    public EtudiantDTO(Long id, String nom, String email, TypeEtudeDTO typeEtude, List<CoursDTO> coursEtudiant) {
        this.id = id;
        this.nom = nom;
        this.email = email;
        this.typeEtude = typeEtude;
        this.coursEtudiant = coursEtudiant;
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public TypeEtudeDTO getTypeEtude() { return typeEtude; }
    public void setTypeEtude(TypeEtudeDTO typeEtude) { this.typeEtude = typeEtude; }

    public List<CoursDTO> getCoursEtudiant() { return coursEtudiant; }
    public void setCoursEtudiant(List<CoursDTO> cours) { this.coursEtudiant = cours; }
}
