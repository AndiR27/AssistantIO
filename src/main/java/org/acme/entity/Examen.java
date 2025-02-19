package org.acme.entity;


import jakarta.persistence.*;

@Entity
public class Examen extends Evaluation{

    @Enumerated(EnumType.STRING)
    public TypeSemestre semestre;


}
