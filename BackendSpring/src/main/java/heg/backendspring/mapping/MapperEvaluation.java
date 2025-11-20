package heg.backendspring.mapping;

import heg.backendspring.entity.Evaluation;
import heg.backendspring.models.EvaluationDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = CentralConfig.class)
public interface MapperEvaluation {

    @Mapping(source = "course.id", target = "courseId")
    EvaluationDto toDto(Evaluation entity);

    @Mapping(target = "course", ignore = true)
    Evaluation toEntity(EvaluationDto dto);

    // === DTO -> Entity (update partiel) ===
    @Mapping(target = "course", ignore = true)
    void updateEntityFromDto(EvaluationDto dto, @MappingTarget Evaluation evaluation);

}
