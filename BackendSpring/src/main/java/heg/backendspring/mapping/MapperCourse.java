package heg.backendspring.mapping;

import heg.backendspring.entity.Course;
import heg.backendspring.models.CourseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = CentralConfig.class,
        uses = {
                MapperTP.class,
                MapperStudent.class,
                MapperEvaluation.class
        })
public interface MapperCourse {


    CourseDto toDto(Course entity);

    @Mapping(target = "students", ignore = true)
    @Mapping(target = "tps", ignore = true)
    @Mapping(target = "evaluations", ignore = true)
    Course toEntity(CourseDto dto);

    /**
     * Update
     */
    @Mapping(target = "students", ignore = true)
    @Mapping(target = "tps", ignore = true)
    @Mapping(target = "evaluations", ignore = true)
    void updateEntity(CourseDto dto, @MappingTarget Course entity);
}
