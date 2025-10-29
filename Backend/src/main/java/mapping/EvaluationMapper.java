package mapping;

import models.*;
import entity.*;
import org.mapstruct.*;

@Mapper(config = CentralConfig.class)
public interface EvaluationMapper {
    @Mapping(target = "course", ignore = true) // On ignore la référence au Cours
    ExamDTO toDtoExamen(Exam entity);

    @InheritInverseConfiguration
    Exam toEntityExamen(ExamDTO dto);

    @Mapping(target = "course", ignore = true) // On ignore la référence au Cours
    ContinuousAssessmentDTO toDtoControleContinu(ContinuousAssessment entity);

    @InheritInverseConfiguration
    ContinuousAssessment toEntityControleContinu(ContinuousAssessmentDTO dto);

}
