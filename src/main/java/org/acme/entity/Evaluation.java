package org.acme.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

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
public abstract class Evaluation extends PanacheEntityBase {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Long id;

    public String nom;

    public String date;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "cours_id")
    public Cours cours;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "rendu_id")
    public Rendu rendu;

}
