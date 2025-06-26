package org.acme.entity;


import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import org.acme.enums.CourseType;
import org.acme.enums.SemesterType;

import java.util.*;

/**
 * Entité pour les cours
 * Servira à stocker les informations des cours et faire le lien entre les étudiants
 * et leurs différents rendus
 */
@Entity
@Table(name = "course")
public class Course extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public String name;

    @Column(nullable = false)
    public String code;

    @Enumerated(EnumType.STRING)
    public SemesterType semester;

    @Column(name = "year_course")
    public int year_course;

    @Column(nullable = true)
    public String teacher;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "student_course",
            joinColumns = @JoinColumn(name = "course_id"),
            inverseJoinColumns = @JoinColumn(name = "student_id"))
    public List<Student> studentList;

    //Avec le cascade ALL : la persistance des entités enfants possible en passant par
    //l'entité cours
    public @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    List<TP> tpsList;

    public @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    List<Evaluation> evaluations;

    @Enumerated(EnumType.STRING)
    public CourseType courseType;

    public Course() {
        this.studentList = new ArrayList<>();
        this.tpsList = new ArrayList<>();
        this.evaluations = new ArrayList<>();
    }

    public void addEtudiant(Student student) {
        this.studentList.add(student);
    }

    public void addTravailPratique(TP tp) {
        this.tpsList.add(tp);
    }
}
