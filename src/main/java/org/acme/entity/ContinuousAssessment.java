package org.acme.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Entité représentant un contrôle continu dans le système de gestion des cours.
 * Un contrôle continu est une évaluation périodique qui est noté
 * et qui est associé à un cours et un rendu.
 */
@Entity
@Table(name = "continuous_assessment")
public class ContinuousAssessment extends Evaluation {

    public int coefficient;

    @Column(nullable = false)
    public int number;

    public ContinuousAssessment(String nom, LocalDateTime date, Course course, Submission submission,
                                int coefficient, int number) {
        super(nom, date, course, submission);
        this.coefficient = coefficient;
        this.number = number;
    }

    public ContinuousAssessment() {
    }

}
