package heg.backendspring.mapping;

import heg.backendspring.entity.Evaluation;
import heg.backendspring.models.EvaluationDto;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(config = CentralConfig.class)
public interface MapperEvaluation {

    @Mapping(source = "course.id", target = "courseId")
    EvaluationDto toDto(Evaluation entity);

    @Mapping(target = "course", ignore = true)
    Evaluation toEntity(EvaluationDto dto);

    // === DTO -> Entity (update partiel) ===
    // IGNORE : les champs null du DTO ne doivent pas écraser les valeurs existantes
    // (ex. un update "renommage" n'envoie pas le mode → on ne doit pas le remettre à null).
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "course", ignore = true)
    void updateEntityFromDto(EvaluationDto dto, @MappingTarget Evaluation evaluation);


}
