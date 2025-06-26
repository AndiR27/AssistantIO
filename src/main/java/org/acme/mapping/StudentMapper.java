package org.acme.mapping;


import org.acme.entity.Student;
import org.acme.enums.StudyType;
import org.acme.models.StudentDTO;
import org.mapstruct.*;

@Mapper(config = CentralConfig.class)
public interface StudentMapper {
    // on ignore la liste "coursInscrits" pour éviter que l'étudiant
    // ne contienne lui-même les cours, qui contiennent les étudiants, etc.
    @Mapping(target = "coursEtudiant", ignore = true)
    StudentDTO toDto(Student student);

    // ou inversement, on ignore dans l’autre sens
    @InheritInverseConfiguration
    Student toEntity(StudentDTO dto);

    // Gestion de typeEtude
    default StudyType toDto(StudyType studyType) {
        if (studyType == null) {
            return null;
        }
        // Supposons que SemesterType est un enum "miroir" de TypeSemestre
        return StudyType.valueOf(studyType.name());
    }

    default StudyType toEntity(StudyType typeEtudeDTO) {
        if (typeEtudeDTO == null) {
            return null;
        }
        return StudyType.valueOf(typeEtudeDTO.name());
    }
}
