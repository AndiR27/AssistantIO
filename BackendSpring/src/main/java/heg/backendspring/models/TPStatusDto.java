package heg.backendspring.models;

import heg.backendspring.enums.StudentSubmissionType;

public record TPStatusDto(
        Long id,
        Long studentId,
        Long tpId,
        StudentSubmissionType studentSubmission
) {
}
