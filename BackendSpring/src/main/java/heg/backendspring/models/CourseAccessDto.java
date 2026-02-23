package heg.backendspring.models;

import heg.backendspring.security.course_access.CoursePermission;

import java.time.LocalDateTime;

public record CourseAccessDto(
        Long id,
        String userId,
        Long courseId,
        CoursePermission permission,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
