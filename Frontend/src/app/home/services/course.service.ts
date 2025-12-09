import { Injectable } from '@angular/core';
import { ApiService } from '../../shared/services/api.service';
import { catchError, Observable, switchMap, tap, throwError } from 'rxjs';
import { map } from 'rxjs/operators';
import { SemesterType } from '../models/semesterType.model';
import { CourseType } from '../models/courseType.model';
import { CourseDetailsModel, StudentModel, TP_Model, TPStatusModel } from '../models/courseDetails.model';

/**
 * Service pour gérer les opérations détaillées des cours.
 * Gère les étudiants, les TPs et les données spécifiques aux cours.
 * Utilise ApiService pour les requêtes HTTP.
 */
@Injectable({ providedIn: 'root' })
export class CourseService {

  constructor(private api: ApiService) { }

  // ========================================
  // OPÉRATIONS SUR LES COURS
  // ========================================

  /**
   * Récupère les détails d'un cours par son identifiant.
   * GET /admin/courses/{courseId}
   * @param id - L'identifiant du cours
   * @returns Observable<CourseDetailsModel>
   */
  getCourseById(id: number): Observable<CourseDetailsModel> {
    return this.api.getCourseById(id).pipe(
      tap(res => console.log('API getCourseById response:', res)),
      map((res: any) => {
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
  // OPÉRATIONS SUR LES ÉTUDIANTS
  // ========================================

  /**
   * Récupère tous les étudiants d'un cours.
   * GET /course/{courseId}/students
   * @param courseId - L'identifiant du cours
   * @returns Observable<StudentModel[]>
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
   * Ajoute un étudiant à un cours.
   * POST /course/{courseId}/addStudent
   * @param courseId - L'identifiant du cours
   * @param student - Les données de l'étudiant à ajouter
   * @returns Observable<StudentModel>
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
   * Ajoute des étudiants depuis un fichier .txt.
   * POST /course/{courseId}/addStudentsFromFile
   * @param courseId - L'identifiant du cours
   * @param file - Le fichier contenant la liste d'étudiants
   * @returns Observable<any>
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

  /**
   * Supprime un étudiant d'un cours.
   * DELETE /course/{courseId}/students/{studentId}
   * @param courseId - L'identifiant du cours
   * @param studentId - L'identifiant de l'étudiant à supprimer
   * @returns Observable<void>
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
   * Met à jour les informations d'un étudiant.
   * PUT /course/{courseId}/students/{studentId}
   * @param courseId - L'identifiant du cours
   * @param studentId - L'identifiant de l'étudiant
   * @param student - Les nouvelles données de l'étudiant
   * @returns Observable<StudentModel>
   */
  updateStudent(courseId: number, studentId: number, student: StudentModel): Observable<StudentModel> {
    return this.api.updateStudent(courseId, studentId, student).pipe(
      tap(res => console.log(`Student ${studentId} updated in course ${courseId}:`, res)),
      map((res: any) => ({
        id: res.id,
        name: res.name,
        email: res.email,
        studyType: res.studyType
      } as StudentModel)),
      catchError(err => {
        console.error('Error in updateStudent():', err);
        return throwError(() => err);
      })
    );
  }

  // ========================================
  // OPÉRATIONS SUR LES TPS (TRAVAUX PRATIQUES)
  // ========================================

  /**
   * Récupère tous les TPs d'un cours.
   * GET /course/{courseId}/TPs
   * @param courseId - L'identifiant du cours
   * @returns Observable<TP_Model[]>
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
   * Ajoute un TP à un cours avec un fichier de rendu.
   * POST /course/{courseId}/TPs/{tpNumber}
   * POST /course/{courseId}/addRendu/{tpNo}
   * @param courseId - L'identifiant du cours
   * @param tpNo - Le numéro du TP
   * @param file - Le fichier de rendu (archive ZIP)
   * @returns Observable<TP_Model>
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
   * Démarre le traitement de restructuration pour un TP.
   * POST /course/{courseId}/startProcessSubmission/{tpNo}
   * @param courseId - L'identifiant du cours
   * @param tpNo - Le numéro du TP
   * @returns Observable<any>
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
   * Gère les statuts du TP pour tous les étudiants.
   * POST /course/{courseId}/manageTP/{tpNo}
   * @param courseId - L'identifiant du cours
   * @param tpNo - Le numéro du TP
   * @returns Observable<any>
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
   * Télécharge le fichier ZIP restructuré d'un TP.
   * GET /course/{courseId}/downloadRestructuredZip/{tpNo}
   * @param courseId - L'identifiant du cours
   * @param tpNo - Le numéro du TP
   * @returns Observable<Blob>
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
   * Supprime un TP d'un cours.
   * DELETE /course/{courseId}/TPs/{tpNo}
   * @param courseId - L'identifiant du cours
   * @param tpNo - Le numéro du TP à supprimer
   * @returns Observable<void>
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
   * Met à jour un TP.
   * PUT /course/{courseId}/TPs/{tpNo}
   * @param courseId - L'identifiant du cours
   * @param tpNo - Le numéro du TP
   * @param tpData - Les nouvelles données du TP
   * @returns Observable<TP_Model>
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

  /**
   * Rafraîchit la liste des statuts d'un TP.
   * POST /course/{courseId}/TPs/{tpNo}/TPStatusRefresh
   * @param courseId - L'identifiant du cours
   * @param tpNo - Le numéro du TP
   * @returns Observable<TPStatusModel[]>
   */
  refreshTPStatusList(courseId: number, tpNo: number): Observable<TPStatusModel[]> {
    return this.api.refreshTPStatus(courseId, tpNo).pipe(
      tap(res => console.log(`✅ Refreshed TPStatus list (Course: ${courseId}, TP: ${tpNo}):`, res)),
      catchError(err => {
        console.error('Error in refreshTPStatusList():', err);
        return throwError(() => err);
      })
    );
  }
}
