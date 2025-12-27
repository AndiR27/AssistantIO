package heg.backendspring.rest;

import heg.backendspring.api.AdminCourseApi;
import heg.backendspring.models.CourseDto;
import heg.backendspring.security.SecurityUtils;
import heg.backendspring.security.course_access.ServiceCourseAccess;
import heg.backendspring.service.ServiceCourse;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.NativeWebRequest;

import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequiredArgsConstructor

public class ControllerAdmin implements AdminCourseApi {

    private final ServiceCourse serviceCourse;
    private final ServiceCourseAccess serviceCourseAccess;
    private final SecurityUtils securityUtils;



    @Override
    public Optional<NativeWebRequest> getRequest() {
        return AdminCourseApi.super.getRequest();
    }

    @Override
    public ResponseEntity<CourseDto> adminCreateCourse(CourseDto courseDto) {
        return ResponseEntity.status(201).body(serviceCourse.addCourse(courseDto));
    }

    @Override
    public ResponseEntity<Void> adminDeleteCourse(Long courseId) {
        serviceCourse.deleteCourse(courseId);
        return ResponseEntity.status(204).build();
    }

    @Override
    public ResponseEntity<CourseDto> adminGetCourseById(Long courseId) {
        Optional<CourseDto> courseDto = serviceCourse.findCourseById(courseId);
        return courseDto.map(ResponseEntity::ok).orElseThrow(() -> new EntityNotFoundException("Course not found"));
    }

    @Override
    public ResponseEntity<List<CourseDto>> adminGetCourses() {
        List<CourseDto> courses;

        if (securityUtils.isGlobalAdmin()) {
            courses = serviceCourse.findAllCourses();
        } else {
            List<Long> ids = serviceCourseAccess.getAccessibleCourseIds(
                    securityUtils.getCurrentUserId()
            );
            courses = ids.isEmpty() ? List.of() : serviceCourse.findAllById(ids);
        }
        return ResponseEntity.ok(courses);
    }

    @Override
    public ResponseEntity<CourseDto> adminUpdateCourse(CourseDto courseDto) {
        Optional<CourseDto> courseOpt = serviceCourse.updateCourse(courseDto);
        return courseOpt.map(ResponseEntity::ok).orElseThrow(() -> new EntityNotFoundException("Course not found"));

    }


}
