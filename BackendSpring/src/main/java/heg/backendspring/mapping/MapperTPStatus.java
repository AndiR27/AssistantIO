package heg.backendspring.mapping;

import heg.backendspring.entity.TPStatus;
import heg.backendspring.models.TPStatusDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralConfig.class)
public interface MapperTPStatus {

    @Mapping(source = "tp.id", target = "tpId")
    @Mapping(source = "student.id", target = "studentId")
    TPStatusDto toDto(TPStatus entity);

    @Mapping(target = "student", ignore = true)
    @Mapping(target = "tp", ignore = true)
    TPStatus toEntity(TPStatusDto dto);

}
