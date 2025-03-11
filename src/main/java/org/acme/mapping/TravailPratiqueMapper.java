package org.acme.mapping;

import org.acme.entity.*;
import org.acme.models.*;
import org.mapstruct.*;

@Mapper(config = CentralConfig.class, componentModel = "cdi",
    uses = {RenduMapper.class})
public interface TravailPratiqueMapper {
    @Mapping(target = "cours", ignore = true) // On ignore la référence au Cours
    TravailPratiqueDTO toDto(TravailPratique entity);

    @InheritInverseConfiguration
    TravailPratique toEntity(TravailPratiqueDTO dto);
}
