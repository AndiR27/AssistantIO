package heg.backendspring.models;

import heg.backendspring.enums.CourseType;
import heg.backendspring.enums.SemesterType;

import java.util.Set;

public record CourseDto(
        Long id,
        String name,
        String code,
        SemesterType semester,
        CourseType courseType,
        int year_course,
        String teacher,
        Set<StudentDto> students,
        Set<TPDto> tps,
        Set<EvaluationDto> evaluations

) {
}
