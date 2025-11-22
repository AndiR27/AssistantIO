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

    public Submission(String fileName, String pathStorage, String pathFileStructured) {
        this.fileName = fileName;
        this.pathStorage = pathStorage;
        this.pathFileStructured = pathFileStructured;
    }
}
