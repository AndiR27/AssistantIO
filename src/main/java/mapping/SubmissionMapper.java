package mapping;

import entity.Submission;
import models.SubmissionDTO;
import org.mapstruct.Mapper;

@Mapper(config = CentralConfig.class)
public interface SubmissionMapper {
    SubmissionDTO toDto(Submission entity);
    Submission toEntity(SubmissionDTO dto);
}
