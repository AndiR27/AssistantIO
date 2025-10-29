package org.acme.mapping;

import org.acme.entity.TPStatus;
import org.acme.models.TPStatusDTO;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(config = CentralConfig.class)
public interface TPStatusMapper {
     // Prevent recursion
     @Mapping(source = "student.id",target = "studentId")
     @Mapping(source = "tp.id", target = "tpId")
     TPStatusDTO toDTO(TPStatus tpStatus);

    @InheritInverseConfiguration
    TPStatus toEntity(TPStatusDTO tpStatusDTO);

}
