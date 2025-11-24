package heg.backendspring.rest;

import heg.backendspring.api.AdminCourseApi;
import heg.backendspring.models.CourseDto;
import heg.backendspring.service.ServiceCourse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ControllerAdmin implements AdminCourseApi {

    private final ServiceCourse serviceCourse;


    @Override
    public ResponseEntity<CourseDto> adminCreateCourse(CourseDto courseDto) {
        return ResponseEntity.status(201).body(serviceCourse.addCourse(courseDto));
    }

    @Override
    public ResponseEntity<Void> adminDeleteCourse(Long courseId) {
        return AdminCourseApi.super.adminDeleteCourse(courseId);
    }

    @Override
    public ResponseEntity<CourseDto> adminGetCourseById(Long courseId) {
        return AdminCourseApi.super.adminGetCourseById(courseId);
    }

    @Override
    public ResponseEntity<List<CourseDto>> adminGetCourses() {
        log.info("Getting all courses");
        return ResponseEntity.ok(serviceCourse.findAllCourses());
    }

    @Override
    public ResponseEntity<CourseDto> adminUpdateCourse(Long courseId, CourseDto courseDto) {
        return AdminCourseApi.super.adminUpdateCourse(courseId, courseDto);
    }
}
