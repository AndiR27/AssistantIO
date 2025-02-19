package org.acme.entity;


import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.util.*;

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
    public String prof;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @JoinTable(name = "etudiant_cours",
            joinColumns = @JoinColumn(name = "cours_id"),
            inverseJoinColumns = @JoinColumn(name = "etudiant_id"))
    public List<Etudiant> etudiantsInscrits;

    public @OneToMany(mappedBy = "cours", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    List<TravailPratique> travauxPratiques;

    public @OneToMany(mappedBy = "cours", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    List<Evaluation> evaluations;

    public Cours() {
    }

}
