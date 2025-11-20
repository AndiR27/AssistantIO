package heg.backendspring.entity;


import heg.backendspring.enums.SemesterType;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity

@Table(name = "course",
        uniqueConstraints = @UniqueConstraint(columnNames = {"name", "code", "semester", "year_course"}))
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SemesterType semester;

    @Column(nullable = false)
    private int year_course;

    private String teacher;

    //Un cours a plusieurs Student
    @ManyToMany(mappedBy = "studentCourses")
    private Set<Student> students = new HashSet<>();

    //Un cours a plusieurs TP
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL)
    private Set<TP> tps = new HashSet<>();

    //Un cours a plusieurs Evaluations
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL)
    private Set<Evaluation> evaluations = new HashSet<>();

}
