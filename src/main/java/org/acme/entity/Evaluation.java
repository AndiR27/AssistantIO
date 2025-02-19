package org.acme.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

@Entity
public abstract class Evaluation extends PanacheEntityBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public String nom;

    public String date;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "cours_id")
    public Cours cours;

    @OneToOne(mappedBy = "evaluation", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    public Rendu rendu;

}
