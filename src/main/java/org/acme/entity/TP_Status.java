package org.acme.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

/**
 * Ce modèle permet de gérer le statut d'un travail pratique pour un étudiant
 * -> Solution d'une table supplémentaire a été préférée afin de pouvoir
 * gérer par la suite des informations supplémentaires sur le statut du rendu
 * Exemple : si rendu fait : mais qualité de code insuffisante, il faudra
 * ajouter un champ pour stocker cette information
 */
@Entity
@Table(name = "tp_etudiant_status")
public class TP_Status extends PanacheEntityBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @ManyToOne
    @JoinColumn(name = "etudiant_id")
    public Etudiant etudiant;

    @ManyToOne
    @JoinColumn(name = "travail_pratique_id")
    public TravailPratique travailPratique;

    public boolean renduEtudiant;

    public TP_Status(Etudiant etudiant, TravailPratique travailPratique, boolean renduEtudiant) {
        this.etudiant = etudiant;
        this.travailPratique = travailPratique;
        this.renduEtudiant = renduEtudiant;
    }

    public TP_Status() {
    }
}
