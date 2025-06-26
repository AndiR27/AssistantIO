package org.acme.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

/**
 * Ce modèle permet de gérer le statut d'un travail pratique pour un étudiant
 * -> Solution d'une table supplémentaire a été préférée afin de pouvoir
 * gérer par la suite des informations supplémentaires sur le statut du rendu
 * Exemple : si rendu fait : mais qualité de code insuffisante, il faudra
 * ajouter un champ pour stocker cette information
 */
@Entity
@Table(name = "tp_student_status")
public class TPStatus extends PanacheEntityBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @ManyToOne
    @JoinColumn(name = "student_id")
    public Student student;

    @ManyToOne
    @JoinColumn(name = "travail_pratique_id")
    public TP TP;

    @Column(name = "student_submitted")
    public boolean studentSubmission;

    public TPStatus(Student student, TP TP, boolean studentSubmission) {
        this.student = student;
        this.TP = TP;
        this.studentSubmission = studentSubmission;
    }

    public TPStatus() {
    }
}
