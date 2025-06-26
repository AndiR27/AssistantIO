package org.acme.mapping;

import org.acme.entity.*;
import org.acme.models.*;
import org.mapstruct.*;

@Mapper(config = CentralConfig.class,
    uses = {SubmissionMapper.class})
public interface TPMapper {

    @Mapping(target = "cours", ignore = true) // On ignore la référence au Cours
    TP_DTO toDto(TP entity);

    @InheritInverseConfiguration
    TP toEntity(TP_DTO dto);
}
