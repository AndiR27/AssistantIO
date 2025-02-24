package org.acme.entity;

import jakarta.persistence.*;

@Entity
public class ControleContinu extends Evaluation{

    public int coefficient;

    @Column(nullable = false)
    public int numero;

    public ControleContinu(String nom, String date, Cours cours, Rendu rendu,
                           int coefficient, int numero) {
        super(nom, date, cours, rendu);
        this.coefficient = coefficient;
        this.numero = numero;
    }
    public ControleContinu() {}

}
