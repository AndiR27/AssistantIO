package heg.backendspring.rest;

import heg.backendspring.api.AdminCourseApi;
import heg.backendspring.models.CourseDto;
import heg.backendspring.service.ServiceCourse;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

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
        log.info("Getting all courses");
        return ResponseEntity.ok(serviceCourse.findAllCourses());
    }

    @Override
    public ResponseEntity<CourseDto> adminUpdateCourse(CourseDto courseDto) {
        Optional<CourseDto> courseOpt = serviceCourse.updateCourse(courseDto);
        return courseOpt.map(ResponseEntity::ok).orElseThrow(() -> new EntityNotFoundException("Course not found"));

    }



}
