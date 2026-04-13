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
public class TP implements Submittable {

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

    // Un TP a une liste de statuts pour le suivi
    // LAZY pour éviter N+1 queries - charger explicitement quand nécessaire
    @OneToMany(mappedBy = "tp", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<TPStatus> statusStudents;

    public TP(int no, Course course, Submission submission) {
        this.no = no;
        this.course = course;
        this.submission = submission;
    }

    // ========================================
    // Submittable interface implementation
    // ========================================

    @Override
    public String getSubmissionName() {
        return "TP" + this.no;
    }

    @Override
    public String getFolderName() {
        return "TP" + this.no;
    }
}
