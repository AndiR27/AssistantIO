package org.acme.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.util.*;

/**
 * Classe représentant un étudiant et ses informations
 */
@Entity
public class Etudiant extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    //autres champs de la table
    @Column(nullable = false)
    public String nom;

    @Column(nullable = false, unique = true)
    public String email;

    @Enumerated(EnumType.STRING)
    public TypeEtude typeEtude;

    @ManyToMany(mappedBy ="etudiantsInscrits", cascade = {CascadeType.PERSIST,
            CascadeType.MERGE},
            fetch = FetchType.LAZY)
    public List<Cours> coursEtudiant;

    public Etudiant() {
    }

    public Etudiant(String nom, String email, TypeEtude typeEtude) {
        this.nom = nom;
        this.email = email;
        this.typeEtude = typeEtude;
        this.coursEtudiant = new ArrayList<>();
    }


    public void addCours(Cours cours) {
        this.coursEtudiant.add(cours);
    }
}
