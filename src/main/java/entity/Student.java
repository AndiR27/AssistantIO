package entity;

import jakarta.persistence.*;
import enums.StudyType;

import java.util.*;

/**
 * Entité représentant un étudiant et ses informations
 */
@Entity
@Table(name = "student")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    //autres champs de la table
    @Column(name = "student_name", nullable = false)
    public String name;

    @Column(nullable = false, unique = true)
    public String email;


    @Enumerated(EnumType.STRING)
    public StudyType studyType;

    @ManyToMany(mappedBy = "studentList",
            fetch = FetchType.LAZY)
    public List<Course> courseStudentList = new ArrayList<>();

    public Student() {
    }

    public void addCours(Course course) {
        this.courseStudentList.add(course);
    }
}
