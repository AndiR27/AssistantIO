package org.acme.entity;

import jakarta.persistence.*;
import org.acme.enums.SemesterType;

import java.time.LocalDateTime;

/**
 * Entité représentant un examen dans le système de gestion des cours.
 * Un examen est une évaluation finale qui est notée : il n'y a normalement
 * qu'un seul examen par cours, mais il peut y avoir des exceptions où
 * un cours/module ne comporte pas d'examen
 */
@Entity
@Table(name = "exam")
public class Exam extends Evaluation {

    @Enumerated(EnumType.STRING)
    public SemesterType semester;


    public Exam(String nom, LocalDateTime date, Course course, Submission submission,
                SemesterType semesterType) {
        super(nom, date, course, submission);
        this.semester = semesterType;
    }

    public Exam() {
    }
}
