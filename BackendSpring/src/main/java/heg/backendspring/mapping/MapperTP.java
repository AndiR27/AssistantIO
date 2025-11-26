package heg.backendspring.mapping;

import heg.backendspring.entity.Course;
import heg.backendspring.entity.TP;
import heg.backendspring.models.CourseDto;
import heg.backendspring.models.TPDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = CentralConfig.class,
        uses = {
        MapperTPStatus.class
})
public interface MapperTP {

    @Mapping(source = "course.id", target = "courseId")
    TPDto toDto(TP entity);

    @Mapping(target = "course", ignore = true)
    TP toEntity(TPDto dto);

    //Update entity from dto
    @Mapping(target = "course", ignore = true)
    void updateEntityFromDto(TPDto dto, @MappingTarget TP entity);
}
