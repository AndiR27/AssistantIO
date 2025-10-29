package mapping;

import entity.TPStatus;
import models.TPStatusDTO;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralConfig.class)
public interface TPStatusMapper {
     // Prevent recursion
     @Mapping(source = "student.id",target = "studentId")
     @Mapping(source = "tp.id", target = "tpId")
     TPStatusDTO toDTO(TPStatus tpStatus);

    @InheritInverseConfiguration
    TPStatus toEntity(TPStatusDTO tpStatusDTO);

}
