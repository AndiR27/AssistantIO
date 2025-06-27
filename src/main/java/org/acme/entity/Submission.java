package org.acme.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

/**
 * Gestion d'un rendu qui permettra de stocker un fichier zip contenant
 * les fichiers rendus par les étudiants
 */
@Entity
public class Submission extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    //TODO : réfléchir aux contraintes de ces champs
    @Column(name = "file_name", nullable = false)
    public String fileName;

    public String pathStorage;

    public String pathFileStructured;

    public Submission() {
    }

    public Submission(String fileName, String pathStorage, String pathFileStructured) {
        this.fileName = fileName;
        this.pathStorage = pathStorage;
        this.pathFileStructured = pathFileStructured;
    }

}
