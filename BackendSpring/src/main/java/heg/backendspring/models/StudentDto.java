package heg.backendspring.models;

import heg.backendspring.enums.StudyType;

import java.util.Set;

public record StudentDto(
        Long id,
        String name,
        String email,
        StudyType studyType,
        Set<CourseDto> studentCourses  //
) {
}
