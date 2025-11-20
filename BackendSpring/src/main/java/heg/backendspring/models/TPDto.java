package heg.backendspring.models;

import java.util.Set;

public record TPDto(
        Long id,
        int no,
        Long courseId,
        SubmissionDto submission,
        Set<TPStatusDto> statusStudents
) {
}
