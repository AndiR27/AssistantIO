package heg.backendspring.mapping;


import heg.backendspring.entity.Submission;
import heg.backendspring.models.SubmissionDto;
import org.mapstruct.Mapper;

@Mapper(config = CentralConfig.class)
public interface MapperSubmission {

    SubmissionDto toDto(Submission entity);

    Submission toEntity(SubmissionDto dto);

}
