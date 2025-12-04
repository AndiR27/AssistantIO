package heg.backendspring.entity;


import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Entity
@Table(name = "tp")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class TP {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "TP_number", nullable = false, unique = false)
    private int no;

    //Un TP est associé à un cours
    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    //Un TP a une soumission
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "submission_id")
    private Submission submission;

    //Un TP a une liste de status pour le suivi
    @OneToMany(mappedBy = "tp", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<TPStatus> statusStudents;

    public TP(int no, Course course, Submission submission) {
        this.no = no;
        this.course = course;
        this.submission = submission;
    }
}
