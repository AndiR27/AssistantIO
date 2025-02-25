package org.acme.entity;


import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.util.*;
/**
 * Entité pour les cours
 * Servira à stocker les informations des cours et faire le lien entre les étudiants
 * et leurs différents rendus
 */
@Entity
public class Cours extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public String nom;
    @Column(nullable = false)
    public String code;

    @Enumerated(EnumType.STRING)
    public TypeSemestre semestre;

    public int annee;

    @Column(nullable = true)
    public String prof;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @JoinTable(name = "etudiant_cours",
            joinColumns = @JoinColumn(name = "cours_id"),
            inverseJoinColumns = @JoinColumn(name = "etudiant_id"))
    public List<Etudiant> etudiantsInscrits;

    //Avec le cascade ALL : la persistance des entités enfants possible en passant par
    //l'entité cours
    public @OneToMany(mappedBy = "cours", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    List<TravailPratique> travauxPratiques;

    public @OneToMany(mappedBy = "cours", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    List<Evaluation> evaluations;

    @Enumerated(EnumType.STRING)
    public TypeCours typeCours;

    public Cours() {
    }

    public Cours(String nom, String code, TypeSemestre semestre, int annee, TypeCours typeCours) {
        this.nom = nom;
        this.code = code;
        this.semestre = semestre;
        this.annee = annee;
        this.typeCours = typeCours;

        //initialisation des listes
        this.etudiantsInscrits = new ArrayList<>();
        this.travauxPratiques = new ArrayList<>();
        this.evaluations = new ArrayList<>();
    }

    public void addEtudiant(Etudiant etudiant) {
        this.etudiantsInscrits.add(etudiant);
    }

    public void addTravailPratique(TravailPratique tp) {
        this.travauxPratiques.add(tp);
    }
}
