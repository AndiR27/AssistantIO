package heg.backendspring.entity;


import heg.backendspring.enums.SubmissionMode;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Entity
@Table(name = "evaluation")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Evaluation implements Submittable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "evaluation_name", nullable = false)
    private String name;

    @Column(name = "evaluation_date", nullable = false)
    @DateTimeFormat(pattern = "dd/MM/yyyy")
    private LocalDate date;

    private int coefficient;

    /** Mode de traitement du rendu : CLASSIC (par défaut) ou LOCAL_WS (dossiers HEG-WS + mapping). */
    @Enumerated(EnumType.STRING)
    @Column(name = "evaluation_mode", nullable = false)
    private SubmissionMode mode = SubmissionMode.CLASSIC;

    //Une évaluation est associée à un cours
    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    //Une évaluation a une soumission
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "submission_id")
    private Submission submission;

    // ========================================
    // Submittable interface implementation
    // ========================================

    @Override
    public String getSubmissionName() {
        return this.name;
    }

    @Override
    public String getFolderName() {
        return this.name;
    }
}
