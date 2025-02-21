package org.acme.entity;


import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Entity
public class TravailPratique extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer id;

    public int no;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "cours_id")
    public Cours cours;

    @OneToOne(cascade = CascadeType.ALL, fetch =
            FetchType.EAGER)
    @JoinColumn(name = "rendu_id")
    public Rendu rendu;

    @OneToMany(mappedBy = "travailPratique", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    public Set<TP_Status> statusEtudiants;

    public TravailPratique() {
    }

    public TravailPratique(int no, Cours cours, Rendu rendu) {
        this.no = no;
        this.cours = cours;
        this.rendu = rendu;

        this.statusEtudiants = new HashSet<>();
    }
}
