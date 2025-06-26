package org.acme.mapping;

import org.acme.entity.*;
import org.acme.models.*;
import org.mapstruct.*;

@Mapper(config = CentralConfig.class)
public interface EvaluationMapper {
    @Mapping(target = "cours", ignore = true) // On ignore la référence au Cours
    ExamDTO toDtoExamen(Exam entity);

    @InheritInverseConfiguration
    Exam toEntityExamen(ExamDTO dto);

    @Mapping(target = "cours", ignore = true) // On ignore la référence au Cours
    ContinuousAssessmentDTO toDtoControleContinu(ContinuousAssessment entity);

    @InheritInverseConfiguration
    ContinuousAssessment toEntityControleContinu(ContinuousAssessmentDTO dto);

}
