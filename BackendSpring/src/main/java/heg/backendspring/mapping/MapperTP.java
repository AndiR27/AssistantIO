package heg.backendspring.mapping;

import heg.backendspring.entity.TP;
import heg.backendspring.models.TPDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralConfig.class,
        uses = {
        MapperTPStatus.class
})
public interface MapperTP {

    @Mapping(source = "course.id", target = "courseId")
    TPDto toDto(TP entity);

    @Mapping(target = "course", ignore = true)
    TP toEntity(TPDto dto);
}
