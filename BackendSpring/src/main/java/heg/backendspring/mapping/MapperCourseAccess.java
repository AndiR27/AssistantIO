package heg.backendspring.mapping;

import heg.backendspring.models.CourseAccessDto;
import heg.backendspring.security.course_access.CourseAccess;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralConfig.class)
public interface MapperCourseAccess {

    @Mapping(source = "userEmail", target = "userId")
    CourseAccessDto toDto(CourseAccess entity);

    @Mapping(source = "userId", target = "userEmail")
    CourseAccess toEntity(CourseAccessDto dto);
}
