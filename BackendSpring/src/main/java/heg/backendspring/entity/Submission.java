package heg.backendspring.entity;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "submission")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Submission {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "path_storage")
    private String pathStorage;

    @Column(name = "path_file_structured")
    private String pathFileStructured;

    /** Chemin du fichier mapping.txt utilisé uniquement en mode LOCAL_WS (piloté par l'Evaluation). */
    @Column(name = "path_mapping_file")
    private String pathMappingFile;

    public Submission(String fileName, String pathStorage, String pathFileStructured) {
        this.fileName = fileName;
        this.pathStorage = pathStorage;
        this.pathFileStructured = pathFileStructured;
    }
}
