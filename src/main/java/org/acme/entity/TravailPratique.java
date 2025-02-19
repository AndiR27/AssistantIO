package org.acme.entity;


import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

@Entity
public class TravailPratique extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer id;

    public int no;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "cours_id")
    public Cours cours;

    @OneToOne(mappedBy = "travailPratique", cascade = CascadeType.ALL, fetch =
            FetchType.EAGER)
    public Rendu rendu;


}
