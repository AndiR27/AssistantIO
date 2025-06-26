package org.acme.models;

import org.acme.enums.*;

import java.util.ArrayList;
import java.util.List;

public class StudentDTO {
    private Long id;
    private String nom;
    private String email;
    private StudyType typeEtude;
    private List<CourseDTO> coursEtudiant = new ArrayList<>();

    // Constructeurs
    public StudentDTO() {}

    public StudentDTO(Long id, String nom, String email, StudyType typeEtude, List<CourseDTO> coursEtudiant) {
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

    public StudyType getTypeEtude() { return typeEtude; }
    public void setTypeEtude(StudyType typeEtude) { this.typeEtude = typeEtude; }

    public List<CourseDTO> getCoursEtudiant() { return coursEtudiant; }
    public void setCoursEtudiant(List<CourseDTO> cours) { this.coursEtudiant = cours; }
}
