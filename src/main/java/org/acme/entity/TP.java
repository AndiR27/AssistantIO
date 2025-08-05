package org.acme.entity;


import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Entité pour les travaux pratiques : permet de stocker les infos
 * sur un travail pratique d'un cours : sur un semestre, il y a généralement une
 * dizaine de travaux pratiques (normalement non notés).
 * Le nom "TP" a été choisi pour "Travail Pratique" et au détriment de "LabWork" ou
 * "PracticalWork" pour des raisons de simplicité et de clarté.
 */
@Entity
@Table(name = "travail_pratique")
public class TP extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "TP_number" , nullable = false, unique = false)
    public int no;

    //pas de cascade : on veut eviter que l'entité enfant ne "pilote" pas la persistance
    //du parent
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    public Course course;

    @OneToOne(cascade = CascadeType.ALL, fetch =
            FetchType.EAGER)
    @JoinColumn(name = "submission_id")
    public Submission submission;

    @OneToMany(mappedBy = "tp", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    public Set<TPStatus> statusStudents;

    public TP() {
    }

    public TP(int no, Course course, Submission submission) {
        this.no = no;
        this.course = course;
        this.submission = submission;

        this.statusStudents = new HashSet<>();
    }

    public void addTPStatus(TPStatus tpStatus) {
        this.statusStudents.add(tpStatus);
    }
}
