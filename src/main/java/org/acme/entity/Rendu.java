package org.acme.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

/**
 * Gestion d'un rendu qui permettra de stocker un fichier zip contenant
 * les fichiers rendus par les étudiants
 */
@Entity
public class Rendu extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public String nomFichier;

    public String cheminStockage;

    public String cheminFichierStructure;

    public Rendu() {
    }

    public Rendu(String nomFichier, String cheminStockage, String cheminFichierStructure) {
        this.nomFichier = nomFichier;
        this.cheminStockage = cheminStockage;
        this.cheminFichierStructure = cheminFichierStructure;
    }

}
