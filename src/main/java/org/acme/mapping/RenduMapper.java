package org.acme.mapping;

import org.acme.entity.Rendu;
import org.acme.models.RenduDTO;
import org.mapstruct.Mapper;

@Mapper(config = CentralConfig.class, componentModel = "cdi")
public interface RenduMapper {
    RenduDTO toDto(Rendu entity);
    Rendu toEntity(RenduDTO dto);
}
