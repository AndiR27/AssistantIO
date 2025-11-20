package heg.backendspring.mapping;

import heg.backendspring.entity.Student;
import heg.backendspring.models.StudentDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralConfig.class)
public interface MapperStudent {

    @Mapping(target = "studentCourses", ignore = true)
    StudentDto tDto(Student entity);

    @Mapping(target = "studentCourses", ignore = true)
    Student toEntity(StudentDto dto);
}
