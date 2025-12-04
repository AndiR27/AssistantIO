package heg.backendspring.rest;

import heg.backendspring.api.*;
import heg.backendspring.models.*;
import heg.backendspring.service.ServiceCourse;
import heg.backendspring.service.ServiceTP;
import heg.backendspring.utils.FileUploadForm;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ControllerCourse implements CourseApi, SubmissionApi {
    private final ServiceCourse serviceCourse;

    @Override
    public Optional<NativeWebRequest> getRequest() {
        return CourseApi.super.getRequest();
    }

    @Override
    public ResponseEntity<StudentDto> addStudent(Long courseId, StudentDto studentDto) {
        return ResponseEntity.status(201).body(serviceCourse.addStudent(courseId, studentDto));
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
    public ResponseEntity<List<StudentDto>> addStudentsFromFile(Long courseId, MultipartFile file) {
        log.info("Received file: {}", file.getOriginalFilename());
        try {
            return ResponseEntity.status(201).body(serviceCourse.addAllStudentsFromFile(courseId, FileUploadForm.transformData(file)));
        } catch (IOException e) {
            return ResponseEntity.status(500).build();
        }
    }

    @Override
    public ResponseEntity<StudentDto> getStudentFromCourse(Long courseId, Long studentId) {
        Optional<StudentDto> studentDto = serviceCourse.getStudentFromCourse(courseId, studentId);
        return studentDto.map(ResponseEntity::ok).orElseThrow(() -> new EntityNotFoundException("Student not found"));
    }

    @Override
    public ResponseEntity<List<StudentDto>> getStudentsByCourse(Long courseId) {
        return ResponseEntity.ok(serviceCourse.getAllStudentsFromCourse(courseId));
    }

    @Override
    public ResponseEntity<Void> removeStudentFromCourse(Long courseId, Long studentId) {
        serviceCourse.removeStudentFromCourse(courseId, studentId);
        return ResponseEntity.noContent().build();
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
    public ResponseEntity<Resource> downloadStructuredSubmission(Long courseId, Integer tpNo) {
        // Récupère le fichier zip restructuré
        File file = serviceCourse.getTPSubmissionRestructurated(courseId, tpNo);

        if (file == null || !file.exists()) {
            throw new EntityNotFoundException(
                    "Aucun fichier restructuré trouvé pour le cours " + courseId + " et le TP " + tpNo
            );
        }

        Resource resource = new FileSystemResource(file);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"")
                .contentLength(file.length())
                .contentType(MediaType.APPLICATION_OCTET_STREAM) // ou MediaType.valueOf("application/zip")
                .body(resource);
    }

    @Override
    public ResponseEntity<TPDto> addTPByNumber(Long courseId, Integer tpNumber) {
        return ResponseEntity.status(201).body(serviceCourse.addTPtoCourse(courseId, tpNumber));
    }

    @Override
    public ResponseEntity<Void> deleteTP(Long courseId, Integer tpNumber) {
        serviceCourse.deleteTPFromCourseByNo(courseId, tpNumber);
        return ResponseEntity.status(204).build();
    }

    @Override
    public ResponseEntity<TPDto> getTPById(Long courseId, Integer tpNumber) {
        Optional<TPDto> tpDto = serviceCourse.findTPFromCourseByNo(courseId, tpNumber);
        return tpDto.map(ResponseEntity::ok).orElseThrow(() -> new EntityNotFoundException("TP not found"));
    }


    @Override
    public ResponseEntity<List<TPDto>> getTPsByCourse(Long courseId) {
        return ResponseEntity.ok(serviceCourse.getAllTPsFromCourse(courseId));
    }

    @Override
    public ResponseEntity<TPDto> updateTP(Long courseId, Integer tpNumber, TPDto tpDto) {
        Optional<TPDto> updatedTpOpt = serviceCourse.updateTPfromCourseByNo(courseId, tpNumber, tpDto);
        return updatedTpOpt.map(ResponseEntity::ok).orElseThrow(() -> new EntityNotFoundException("TP not found"));

    }


}
