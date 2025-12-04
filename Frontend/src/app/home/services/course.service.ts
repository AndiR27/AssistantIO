import { Injectable } from '@angular/core';
import { ApiService } from '../../shared/services/api.service';
import { catchError, Observable, tap, throwError } from 'rxjs';
import { map } from 'rxjs/operators';
import { SemesterType } from '../models/semesterType.model';
import { CourseType } from '../models/courseType.model';
import { CourseDetailsModel, StudentModel, TP_Model } from '../models/courseDetails.model';

/**
 * Service for managing course details operations.
 * Handles students, TPs, and course-specific data.
 * Uses ApiService for HTTP requests.
 */
@Injectable({ providedIn: 'root' })
export class CourseService {

  constructor(private api: ApiService) { }

  // ========================================
  // COURSE OPERATIONS
  // ========================================

  /**
   * Get course details by ID (admin endpoint)
   * GET /admin/courses/{courseId}
   */
  getCourseById(id: number): Observable<CourseDetailsModel> {
    return this.api.getCourseById(id).pipe(
      tap(res => console.log('API getCourseById response:', res)),
      map((res: any) => {
        // Handle different response structures
        const dto = Array.isArray(res)
          ? res[0]
          : res.data
            ? (Array.isArray(res.data) ? res.data[0] : res.data)
            : res;

        return {
          id: dto.id,
          name: dto.name,
          code: dto.code,
          teacher: dto.teacher,
          year_course: dto.year_course,
          semester: dto.semester as SemesterType,
          courseType: dto.courseType as CourseType,
          studentList: dto.studentList || [],
          tpsList: dto.tpsList || [],
          evaluations: dto.evaluations || []
        } as CourseDetailsModel;
      }),
      catchError(err => {
        console.error('Error in getCourseById():', err);
        return throwError(() => err);
      })
    );
  }

  // ========================================
  // STUDENT OPERATIONS
  // ========================================

  /**
   * Get all students of a course
   * GET /course/{courseId}/students
   */
  getStudentsByCourseId(courseId: number): Observable<StudentModel[]> {
    return this.api.getStudentsByCourseId(courseId).pipe(
      tap(res => console.log('API getStudentsByCourseId response:', res)),
      map((res: any) => {
        const students = Array.isArray(res) ? res : [];
        return students.map((student: any) => ({
          id: student.id,
          name: student.name,
          email: student.email,
          studyType: student.studyType
        } as StudentModel));
      }),
      catchError(err => {
        console.error('Error in getStudentsByCourseId():', err);
        return throwError(() => err);
      })
    );
  }

  /**
   * Add one student to a course
   * POST /course/{courseId}/addStudent
   */
  addStudentToCourse(courseId: number, student: StudentModel): Observable<StudentModel> {
    return this.api.addStudentToCourse(courseId, student).pipe(
      tap(res => console.log('API addStudentToCourse response:', res)),
      map((res: any) => ({
        id: res.id,
        name: res.name,
        email: res.email,
        studyType: res.studyType
      } as StudentModel)),
      catchError(err => {
        console.error('Error in addStudentToCourse():', err);
        return throwError(() => err);
      })
    );
  }

  /**
   * Add students from a .txt file
   * POST /course/{courseId}/addStudentsFromFile
   */
  addStudentsFromFile(courseId: number, file: File): Observable<any> {
    return this.api.addStudentsFromFile(courseId, file).pipe(
      tap(res => console.log('API addStudentsFromFile response:', res)),
      catchError(err => {
        console.error('Error in addStudentsFromFile():', err);
        return throwError(() => err);
      })
    );
  }

  // ========================================
  // TP (TRAVAUX PRATIQUES) OPERATIONS
  // ========================================

  /**
   * Get all TPs of a course
   * GET /course/{courseId}/TPs
   */
  getTPsByCourseId(courseId: number): Observable<TP_Model[]> {
    return this.api.getTPsByCourseId(courseId).pipe(
      tap(res => console.log('API getTPsByCourseId response:', res)),
      map((res: any) => {
        const tps = Array.isArray(res) ? res : [];
        return tps.map((tp: any) => ({
          id: tp.id,
          no: tp.no,
          course: tp.course,
          submission: tp.submission,
          statusStudents: tp.statusStudents || []
        } as TP_Model));
      }),
      catchError(err => {
        console.error('Error in getTPsByCourseId():', err);
        return throwError(() => err);
      })
    );
  }

  /**
   * Add a TP to a course with submission file
   * POST /course/{courseId}/TPs/{tpNumber}
   * POST /course/{courseId}/addRendu/{tpNo}
   */
  addTpToCourse(courseId: number, tpNo: number, file: File): Observable<TP_Model> {
    return this.api.addTpToCourse(courseId, tpNo, file).pipe(
      tap(res => console.log('API addTpToCourse response:', res)),
      catchError(err => {
        console.error('Error in addTpToCourse():', err);
        return throwError(() => err);
      })
    );
  }

  /**
   * Start submission processing for a TP
   * POST /course/{courseId}/startProcessSubmission/{tpNo}
   */
  startProcessSubmission(courseId: number, tpNo: number): Observable<any> {
    return this.api.startProcessSubmission(courseId, tpNo).pipe(
      tap(res => console.log('API startProcessSubmission response:', res)),
      catchError(err => {
        console.error('Error in startProcessSubmission():', err);
        return throwError(() => err);
      })
    );
  }

  /**
   * Manage TP status for all students
   * POST /course/{courseId}/manageTP/{tpNo}
   */
  manageTP(courseId: number, tpNo: number): Observable<any> {
    return this.api.manageTP(courseId, tpNo).pipe(
      tap(res => console.log('API manageTP response:', res)),
      catchError(err => {
        console.error('Error in manageTP():', err);
        return throwError(() => err);
      })
    );
  }

  /**
   * Download the structured ZIP file of a TP
   * GET /course/{courseId}/downloadRestructuredZip/{tpNo}
   */
  downloadStructuredFile(courseId: number, tpNo: number): Observable<Blob> {
    return this.api.downloadTP(courseId, tpNo).pipe(
      catchError(err => {
        console.error('Error in downloadStructuredFile():', err);
        return throwError(() => err);
      })
    );
  }

  /**
   * Delete a student from a course
   * DELETE /course/{courseId}/students/{studentId}
   */
  deleteStudent(courseId: number, studentId: number): Observable<void> {
    return this.api.deleteStudent(courseId, studentId).pipe(
      tap(() => console.log(`Student ${studentId} deleted from course ${courseId}`)),
      catchError(err => {
        console.error('Error in deleteStudent():', err);
        return throwError(() => err);
      })
    );
  }

  /**
   * Delete a TP from a course
   * DELETE /course/{courseId}/TPs/{tpNo}
   */
  deleteTP(courseId: number, tpNo: number): Observable<void> {
    return this.api.deleteTP(courseId, tpNo).pipe(
      tap(() => console.log(`TP ${tpNo} deleted from course ${courseId}`)),
      catchError(err => {
        console.error('Error in deleteTP():', err);
        return throwError(() => err);
      })
    );
  }


  /**
   * Update a TP
   * PUT /course/{courseId}/TPs/{tpNo}
   */
  updateTP(courseId: number, tpNo: number, tpData: any): Observable<TP_Model> {
    return this.api.updateTP(courseId, tpNo, tpData).pipe(
      tap(res => console.log('API updateTP response:', res)),
      catchError(err => {
        console.error('Error in updateTP():', err);
        return throwError(() => err);
      })
    );
  }
}
