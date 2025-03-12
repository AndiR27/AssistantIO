package org.acme.mapping;

import org.acme.entity.*;
import org.acme.models.*;
import org.mapstruct.*;

@Mapper(config = CentralConfig.class, componentModel = "cdi")
public interface EvaluationMapper {
    @Mapping(target = "cours", ignore = true) // On ignore la référence au Cours
    ExamenDTO toDtoExamen(Examen entity);

    @InheritInverseConfiguration
    Examen toEntityExamen(ExamenDTO dto);

    @Mapping(target = "cours", ignore = true) // On ignore la référence au Cours
    ControleContinuDTO toDtoControleContinu(ControleContinu entity);

    @InheritInverseConfiguration
    ControleContinu toEntityControleContinu(ControleContinuDTO dto);

}
