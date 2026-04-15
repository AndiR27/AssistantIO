package heg.backendspring.service;


import heg.backendspring.models.*;
import heg.backendspring.service.serviceCourse.ServiceCourseCore;
import heg.backendspring.service.serviceCourse.ServiceCourseEvaluation;
import heg.backendspring.service.serviceCourse.ServiceCourseStudents;
import heg.backendspring.service.serviceCourse.ServiceCourseTP;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

/**
 * Facade service for Course operations.
 * Delegates to specialized services for better separation of concerns.
 *
 * @see ServiceCourseCore for Course CRUD operations
 * @see ServiceCourseStudents for student management
 * @see ServiceCourseTP for TP management
 * @see ServiceCourseEvaluation for evaluation management
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ServiceCourse {

    private final ServiceCourseCore serviceCourseCore;
    private final ServiceCourseStudents serviceCourseStudents;
    private final ServiceCourseTP serviceCourseTP;
    private final ServiceCourseEvaluation serviceCourseEvaluation;

    //==============================
    //    COURSE CRUD (delegated)
    //==============================

    public Optional<CourseDto> findCourseById(Long courseId) {
        return serviceCourseCore.findCourseById(courseId);
    }

    public List<CourseDto> findAllCourses() {
        return serviceCourseCore.findAllCourses();
    }

    public List<CourseDto> findAllById(List<Long> ids) {
        return serviceCourseCore.findAllById(ids);
    }

    public CourseDto addCourse(CourseDto courseDto) {
        return serviceCourseCore.addCourse(courseDto);
    }

    public Optional<CourseDto> updateCourse(CourseDto courseDto) {
        return serviceCourseCore.updateCourse(courseDto);
    }

    public void deleteCourse(Long courseId) {
        serviceCourseCore.deleteCourse(courseId);
    }

    //==============================
    //    STUDENTS (delegated)
    //==============================

    public StudentDto addStudent(Long idCourse, StudentDto studentDto) {
        return serviceCourseStudents.addStudent(idCourse, studentDto);
    }

    public Optional<StudentDto> getStudentFromCourse(Long courseId, Long studentId) {
        return serviceCourseStudents.getStudentFromCourse(courseId, studentId);
    }

    public CourseDto addAllStudendsToCourse(Long idCourse, List<StudentDto> studentDtoList) {
        return serviceCourseStudents.addAllStudendsToCourse(idCourse, studentDtoList);
    }

    public List<StudentDto> addAllStudentsFromFile(Long idCourse, String[] data) {
        return serviceCourseStudents.addAllStudentsFromFile(idCourse, data);
    }

    public List<StudentDto> getAllStudentsFromCourse(Long idCourse) {
        return serviceCourseStudents.getAllStudentsFromCourse(idCourse);
    }

    public void removeStudentFromCourse(Long idCourse, Long idStudent) {
        serviceCourseStudents.removeStudentFromCourse(idCourse, idStudent);
    }

    public StudentDto updateStudentFromCourse(Long idCourse, Long idStudent, StudentDto studentDto) {
        return serviceCourseStudents.updateStudentFromCourse(idCourse, idStudent, studentDto);
    }

    //==============================
    //    TPs (delegated)
    //==============================

    public TPDto addTPtoCourse(Long idCourse, int no) {
        return serviceCourseTP.addTPtoCourse(idCourse, no);
    }

    public List<TPDto> getAllTPsFromCourse(Long idCourse) {
        return serviceCourseTP.getAllTPsFromCourse(idCourse);
    }

    public Optional<TPDto> findTPFromCourseByNo(Long idCourse, int no) {
        return serviceCourseTP.findTPFromCourseByNo(idCourse, no);
    }

    public void deleteTPFromCourseByNo(Long idCourse, int no) {
        serviceCourseTP.deleteTPFromCourseByNo(idCourse, no);
    }

    public Optional<TPDto> updateTPfromCourseByNo(Long courseId, Integer tpNumber, TPDto tpDto) {
        return serviceCourseTP.updateTPfromCourseByNo(courseId, tpNumber, tpDto);
    }

    public SubmissionDto addSubmissionToTP(Long idCourse, int tpNo, InputStream zipFile) {
        return serviceCourseTP.addSubmissionToTP(idCourse, tpNo, zipFile);
    }

    public void startZipProcess(Long idCours, int idTp) throws IOException {
        serviceCourseTP.startZipProcess(idCours, idTp);
    }

    public TPDto manageSubmissionsTP(Long idCourse, int tp_no) {
        return serviceCourseTP.manageSubmissionsTP(idCourse, tp_no);
    }

    public File getTPSubmissionRestructurated(Long idCourse, int tp_no) {
        return serviceCourseTP.getTPSubmissionRestructurated(idCourse, tp_no);
    }

    public List<TPStatusDto> getAllTPStatusFromTP(Long idCourse, int tp_no) {
        return serviceCourseTP.getAllTPStatusFromTP(idCourse, tp_no);
    }

    public List<TPStatusDto> refreshTPStatusesFromTP(Long courseId, Integer tpNumber) {
        return serviceCourseTP.refreshTPStatusesFromTP(courseId, tpNumber);
    }

    //==============================
    //    EVALUATIONS (delegated)
    //==============================

    public List<EvaluationDto> getAllEvaluationsFromCourse(Long idCourse) {
        return serviceCourseEvaluation.getAllEvaluationsFromCourse(idCourse);
    }

    public EvaluationDto addEvaluationToCourse(Long idCourse, EvaluationDto evaluationDto) {
        return serviceCourseEvaluation.addEvaluationToCourse(idCourse, evaluationDto);
    }
}


