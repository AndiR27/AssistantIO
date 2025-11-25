package heg.backendspring.rest;

import heg.backendspring.api.*;
import heg.backendspring.models.*;
import heg.backendspring.service.ServiceCourse;
import heg.backendspring.service.ServiceTP;
import heg.backendspring.utils.FileUploadForm;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class ControllerCourse implements CourseApi, ProcessingApi, StudentApi, SubmissionApi, TpApi {
    private final ServiceCourse serviceCourse;
    private final ServiceTP serviceTP;


    @Override
    public Optional<NativeWebRequest> getRequest() {
        return CourseApi.super.getRequest();
    }

    @Override
    public ResponseEntity<CourseDto> getCourseById(Long courseId) {
        Optional<CourseDto> courseDto = serviceCourse.findCourseById(courseId);
        return courseDto.map(ResponseEntity::ok).orElseThrow(() -> new EntityNotFoundException("Course not found"));
    }

    @Override
    public ResponseEntity<List<CourseDto>> getCourses() {
        return ResponseEntity.ok(serviceCourse.findAllCourses());
    }


    @Override
    public ResponseEntity<TPDto> manageTP(Long courseId, Integer tpNo) {
        return ResponseEntity.ok(serviceCourse.manageSubmissionsTP(courseId, tpNo));
    }

    @Override
    public ResponseEntity<TPDto> startProcessSubmission(Long courseId, Integer tpNo) {
        try {
            serviceCourse.startZipProcess(courseId, tpNo);
            return ResponseEntity.accepted().build();
        } catch (IOException e) {
            return ResponseEntity.status(500).build();
        }
    }

    @Override
    public ResponseEntity<StudentDto> addStudentLegacy(Long courseId, StudentDto studentDto) {
        return StudentApi.super.addStudentLegacy(courseId, studentDto);
    }

    @Override
    public ResponseEntity<StudentDto> addStudentToCourse(Long courseId, StudentDto studentDto) {
        return ResponseEntity.status(201).body(serviceCourse.addStudent(courseId, studentDto));
    }

    @Override
    public ResponseEntity<List<StudentDto>> addStudentsFromFile(Long courseId, MultipartFile file) {
        try {
            return ResponseEntity.status(201).body(serviceCourse.addAllStudentsFromFile(courseId, FileUploadForm.transformData(file)));
        } catch (IOException e) {
            return ResponseEntity.status(500).build();
        }
    }

    @Override
    public ResponseEntity<List<StudentDto>> getStudentsByCourse(Long courseId) {
        return ResponseEntity.ok(serviceCourse.getAllStudentsFromCourse(courseId));
    }

    @Override
    public ResponseEntity<Void> removeStudentFromCourse(Long courseId, Long id) {
        serviceCourse.removeStudentFromCourse(courseId, id);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> removeStudentLegacy(Long courseId, Long id) {
        return StudentApi.super.removeStudentLegacy(courseId, id);
    }

    @Override
    public ResponseEntity<SubmissionDto> addSubmission(Long courseId, Integer tpNo, MultipartFile file) {
        try {
            return ResponseEntity.status(201).body(serviceCourse.addSubmissionToTP(courseId, tpNo, FileUploadForm.getFileInputStream(file)));
        } catch (IOException e) {
            return ResponseEntity.status(500).build();
        }
    }

    @Override
    public ResponseEntity<TPDto> addTPByNumber(Long courseId, Integer tpNumber) {
        return ResponseEntity.status(201).body(serviceCourse.addTPtoCourse(courseId, tpNumber));
    }

    @Override
    public ResponseEntity<Void> deleteTP(Long courseId, Integer tpNumber) {
        return TpApi.super.deleteTP(courseId, tpNumber);
    }

    @Override
    public ResponseEntity<TPDto> getTPById(Long courseId, Integer tpNumber) {
        return TpApi.super.getTPById(courseId, tpNumber);
    }


    @Override
    public ResponseEntity<List<TPDto>> getTPsByCourse(Long courseId) {
        return TpApi.super.getTPsByCourse(courseId);
    }

    @Override
    public ResponseEntity<TPDto> updateTP(Long courseId, Integer tpNumber, TPDto tpDto) {
        return TpApi.super.updateTP(courseId, tpNumber, tpDto);
    }

}
