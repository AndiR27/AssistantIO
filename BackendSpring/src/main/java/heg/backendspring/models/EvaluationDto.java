package heg.backendspring.models;

import java.time.LocalDate;

public record EvaluationDto(
        Long id,
        String name,
        LocalDate date,
        int coefficient,
        Long courseId
) {
}
