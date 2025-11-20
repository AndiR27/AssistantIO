package heg.backendspring.mapping;

import heg.backendspring.entity.Course;
import heg.backendspring.models.CourseDto;
import org.mapstruct.Mapper;

@Mapper(config = CentralConfig.class,
    uses = {
        MapperTP.class,
        MapperStudent.class,
        MapperEvaluation.class
    })
public interface MapperCourse {


    CourseDto toDto(Course entity);

    Course toEntity(CourseDto dto);
}
