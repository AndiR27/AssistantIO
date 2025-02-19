package org.acme.entity;

import jakarta.persistence.*;

@Entity
public class ControleContinu extends Evaluation{

    public int coefficient;

    @Column(nullable = false)
    public int numero;
}
