package org.acme.mapping;


import org.acme.entity.Student;
import org.acme.enums.StudyType;
import org.acme.models.StudentDTO;
import org.mapstruct.*;

@Mapper(config = CentralConfig.class)
public interface StudentMapper {
    // on ignore la liste "coursInscrits" pour éviter que l'étudiant
    // ne contienne lui-même les cours, qui contiennent les étudiants, etc.
    @Mapping(target = "courseStudentList", ignore = true)
    StudentDTO toDto(Student student);

    // ou inversement, on ignore dans l’autre sens
    @InheritInverseConfiguration
    Student toEntity(StudentDTO dto);
    
}
