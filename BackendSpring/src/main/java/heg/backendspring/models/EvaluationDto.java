package heg.backendspring.models;

import heg.backendspring.enums.SubmissionMode;

import java.time.LocalDate;

public record EvaluationDto(
        Long id,
        String name,
        LocalDate date,
        int coefficient,
        Long courseId,
        SubmissionDto submission,
        SubmissionMode mode
) {
}
