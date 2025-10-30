package mapping;

import models.*;
import entity.*;
import org.mapstruct.*;

@Mapper(config = CentralConfig.class,
    uses = {SubmissionMapper.class, TPStatusMapper.class})
public interface TPMapper {

    //@Mapping(source = "submission", target = "submission")
    @Mapping(target = "course", ignore = true)
    TP_DTO toDto(TP entity);

    @InheritInverseConfiguration
    TP toEntity(TP_DTO dto);
}
