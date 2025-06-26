package org.acme.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Différents types d'évaluations : CC1, CC2, Examen, Rattrappage
 * Le tag inheritance permet de spécifier la stratégie de mapping de l'héritage
 * Ici, on utilise la stratégie TABLE_PER_CLASS : chaque classe fille a sa propre table
 * Les champs de la classe mère sont présents dans chaque table fille
 * La generation de la clé primaire est automatique : GenerationType.AUTO, parce
 * que la classe est abstraite et ne sera pas instanciée
 */
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Table(name = "evaluation")
public class Evaluation extends PanacheEntityBase {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Long id;

    @Column(name = "evaluation_name")
    public String name;

    @Column(name = "evaluation_date")
    public LocalDateTime date;

    /**
     * Note : penser à ajouter un champ pour le % de l'evaluation dans la note finale
     * --> Une table Module est-elle nécessaire pour stocker le tout ? à voir...
     */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    public Course course;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "submission_id")
    public Submission submission;

    public Evaluation() {
    }

    public Evaluation(String name, LocalDateTime date, Course course, Submission submission) {
        this.name = name;
        this.date = date;
        this.course = course;
        this.submission = submission;
    }

}
