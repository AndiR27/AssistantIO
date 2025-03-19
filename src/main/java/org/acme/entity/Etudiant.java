package org.acme.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.util.*;

/**
 * Classe représentant un étudiant et ses informations
 */
@Entity
public class Etudiant {

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

    @ManyToMany(mappedBy ="etudiantsInscrits",
            fetch = FetchType.LAZY)
    public List<Cours> coursEtudiant = new ArrayList<>();

    public Etudiant() {
    }

    public void addCours(Cours cours) {
        this.coursEtudiant.add(cours);
    }
}
