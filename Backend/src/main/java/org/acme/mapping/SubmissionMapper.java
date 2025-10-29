package org.acme.mapping;

import org.acme.entity.Submission;
import org.acme.models.SubmissionDTO;
import org.mapstruct.Mapper;

@Mapper(config = CentralConfig.class)
public interface SubmissionMapper {
    SubmissionDTO toDto(Submission entity);
    Submission toEntity(SubmissionDTO dto);
}
