package mapping;


import models.*;
import entity.*;
import org.mapstruct.*;

/**
 * @Mapper(componentModel = "cdi") :
 * indique à MapStruct et à Quarkus que cette interface doit être gérée par le conteneur CDI.
 */
@Mapper(config = CentralConfig.class,
        uses = {StudentMapper.class, TPMapper.class})
public interface CourseMapper {
    /**
     * Convertit un Cours (entité) en CoursDto (DTO).
     */
    CourseDTO toDto(Course course);

    /**
     * Convertit un CoursDto (DTO) en Cours (entité).
     */
    Course toEntity(CourseDTO dto);

    /**
     * Met à jour un Cours (entité) à partir d'un CoursDto (DTO).
     */
    @Mapping(target = "id", ignore = true)
    void updateEntity(CourseDTO dto, @MappingTarget Course entity);


}
