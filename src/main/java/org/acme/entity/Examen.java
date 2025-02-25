package org.acme.entity;


import jakarta.persistence.*;

@Entity
public class Examen extends Evaluation{

    @Enumerated(EnumType.STRING)
    public TypeSemestre semestre;


    public Examen(String nom, String date, Cours cours, Rendu rendu,
                  TypeSemestre typeSemestre) {
        super(nom, date, cours, rendu);
        this.semestre = typeSemestre;
    }

    public Examen() {}
}
