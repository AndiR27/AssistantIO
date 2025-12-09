import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpErrorResponse, HttpHeaders } from '@angular/common/http';
import { Observable, throwError, of, switchMap, tap } from 'rxjs';
import { retry, catchError } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { CoursePreview } from '../../home/models/coursePreview.model';
import { CourseDetailsModel, StudentModel, TP_Model } from '../../home/models/courseDetails.model';
import { TPStatusModel, StudentSubmissionType } from '../../home/models/tpStatus.model';

const API_URL = environment.apiUrl;

/**
 * Service API centralisé pour toutes les requêtes HTTP vers le backend.
 * Gère les cours, étudiants, TPs et statuts de TP.
 * Utilise HttpClient pour les communications HTTP.
 */
@Injectable({ providedIn: 'root' })
export class ApiService {
  private http = inject(HttpClient);
  private httpOptions = {
    headers: new HttpHeaders({ 'Content-Type': 'application/json' })
  };

  /**
   * Construit l'URL complète d'une route pour le débogage.
   * @param route - Le chemin de la route
   * @returns L'URL complète
   */
  getRoute(route: string): string {
    return `${API_URL}${route}`;
  }

  // ========================================
  // ADMIN-COURS : Gestion administrative des cours (CRUD)
  // ========================================

  /**
   * Récupère tous les cours (admin).
   * GET /admin/courses
   * @returns Observable<CoursePreview[]>
   */
  getAllCourses(): Observable<CoursePreview[]> {
    return this.http
      .get<CoursePreview[]>(`${API_URL}/admin/courses`, this.httpOptions)
      .pipe(
        retry(1),
        catchError((err: HttpErrorResponse) => {
          this.handleError(err);
          return of([] as CoursePreview[]);
        })
      );
  }

  /**
   * Récupère les détails d'un cours par son ID (admin).
   * GET /admin/courses/{id}
   * @param id - L'identifiant du cours
   * @returns Observable<CourseDetailsModel>
   */
  getCourseById(id: number): Observable<CourseDetailsModel> {
    return this.http
      .get<CourseDetailsModel>(
        `${API_URL}/admin/courses/${id}`,
        this.httpOptions
      )
      .pipe(
        retry(1),
        catchError((err: HttpErrorResponse) => {
          this.handleError(err);
          return throwError(() => err);
        })
      );
  }

  /**
   * Crée un nouveau cours (admin).
   * POST /admin/courses
   * @param course - Les données du cours à créer
   * @returns Observable<CoursePreview>
   */
  addCourse(course: CoursePreview): Observable<CoursePreview> {
    return this.http
      .post<CoursePreview>(
        `${API_URL}/admin/courses`,
        JSON.stringify(course),
        this.httpOptions
      )
      .pipe(
        retry(1),
        catchError((err: HttpErrorResponse) => {
          this.handleError(err);
          return throwError(() => err);
        })
      );
  }

  /**
   * Met à jour un cours (admin).
   * PUT /admin/courses
   * @param course - Les données du cours mises à jour
   * @returns Observable<CoursePreview>
   */
  updateCourse(course: CoursePreview): Observable<CoursePreview> {
    return this.http
      .put<CoursePreview>(
        `${API_URL}/admin/courses`,
        JSON.stringify(course),
        this.httpOptions
      )
      .pipe(
        retry(1),
        catchError((err: HttpErrorResponse) => {
          this.handleError(err);
          return throwError(() => err);
        })
      );
  }

  /**
   * Supprime un cours (admin).
   * DELETE /admin/courses/{courseId}
   * @param courseId - L'identifiant du cours à supprimer
   * @returns Observable<void>
   */
  deleteCourse(courseId: number): Observable<void> {
    return this.http
      .delete<void>(
        `${API_URL}/admin/courses/${courseId}`,
        this.httpOptions
      )
      .pipe(
        retry(1),
        catchError((err: HttpErrorResponse) => {
          this.handleError(err);
          return throwError(() => err);
        })
      );
  }

  // ========================================
  // COURS : Opérations standards sur les cours
  // ========================================

  /**
   * Récupère la liste des cours.
   * GET /course
   * @returns Observable<CoursePreview[]>
   */
  getCourses(): Observable<CoursePreview[]> {
    return this.http
      .get<CoursePreview[]>(`${API_URL}/course`, this.httpOptions)
      .pipe(
        retry(1),
        catchError((err: HttpErrorResponse) => {
          this.handleError(err);
          return of([] as CoursePreview[]);
        })
      );
  }

  /**
   * Récupère les détails d'un cours.
   * GET /course/{courseId}
   * @param courseId - L'identifiant du cours
   * @returns Observable<CourseDetailsModel>
   */
  getCourseDetails(courseId: number): Observable<CourseDetailsModel> {
    return this.http
      .get<CourseDetailsModel>(
        `${API_URL}/course/${courseId}`,
        this.httpOptions
      )
      .pipe(
        retry(1),
        catchError((err: HttpErrorResponse) => {
          this.handleError(err);
          return throwError(() => err);
        })
      );
  }

  // ========================================
  // COURS - Opérations sur les étudiants
  // ========================================

  /**
   * Récupère tous les étudiants d'un cours.
   * GET /course/{courseId}/students
   * @param courseId - L'identifiant du cours
   * @returns Observable<StudentModel[]>
   */
  getStudentsByCourseId(courseId: number): Observable<StudentModel[]> {
    return this.http
      .get<StudentModel[]>(
        `${API_URL}/course/${courseId}/students`,
        this.httpOptions
      )
      .pipe(
        retry(1),
        catchError((err: HttpErrorResponse) => {
          this.handleError(err);
          return of([] as StudentModel[]);
        })
      );
  }

  /**
   * Récupère les détails d'un étudiant.
   * GET /course/{courseId}/students/{studentId}
   * @param courseId - L'identifiant du cours
   * @param studentId - L'identifiant de l'étudiant
   * @returns Observable<StudentModel>
   */
  getStudentById(courseId: number, studentId: number): Observable<StudentModel> {
    return this.http
      .get<StudentModel>(
        `${API_URL}/course/${courseId}/students/${studentId}`,
        this.httpOptions
      )
      .pipe(
        retry(1),
        catchError((err: HttpErrorResponse) => {
          this.handleError(err);
          return throwError(() => err);
        })
      );
  }

  /**
   * Ajoute un étudiant à un cours.
   * POST /course/{courseId}/addStudent
   * @param courseId - L'identifiant du cours
   * @param student - Les données de l'étudiant
   * @returns Observable<StudentModel>
   */
  addStudentToCourse(
    courseId: number,
    student: StudentModel
  ): Observable<StudentModel> {
    return this.http
      .post<StudentModel>(
        `${API_URL}/course/${courseId}/addStudent`,
        JSON.stringify(student),
        this.httpOptions
      )
      .pipe(
        retry(1),
        catchError((err: HttpErrorResponse) => {
          this.handleError(err);
          return throwError(() => err);
        })
      );
  }

  /**
   * Ajoute des étudiants depuis un fichier.
   * POST /course/{courseId}/addStudentsFromFile
   * @param courseId - L'identifiant du cours
   * @param file - Le fichier contenant la liste d'étudiants
   * @returns Observable<any>
   */
  addStudentsFromFile(courseId: number, file: File): Observable<any> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http
      .post(`${API_URL}/course/${courseId}/addStudentsFromFile`, formData)
      .pipe(
        retry(1),
        catchError((err: HttpErrorResponse) => {
          this.handleError(err);
          return throwError(() => err);
        })
      );
  }

  /**
   * Supprime un étudiant d'un cours.
   * DELETE /course/{courseId}/students/{studentId}
   * @param courseId - L'identifiant du cours
   * @param studentId - L'identifiant de l'étudiant
   * @returns Observable<void>
   */
  deleteStudent(courseId: number, studentId: number): Observable<void> {
    return this.http
      .delete<void>(
        `${API_URL}/course/${courseId}/students/${studentId}`,
        this.httpOptions
      )
      .pipe(
        retry(1),
        catchError((err: HttpErrorResponse) => {
          this.handleError(err);
          return throwError(() => err);
        })
      );
  }

  /**
   * Met à jour un étudiant.
   * PUT /course/{courseId}/students/{studentId}
   * @param courseId - L'identifiant du cours
   * @param studentId - L'identifiant de l'étudiant
   * @param student - Les nouvelles données de l'étudiant
   * @returns Observable<StudentModel>
   */
  updateStudent(courseId: number, studentId: number, student: StudentModel): Observable<StudentModel> {
    return this.http
      .put<StudentModel>(
        `${API_URL}/course/${courseId}/students/${studentId}`,
        JSON.stringify(student),
        this.httpOptions
      )
      .pipe(
        retry(1),
        catchError((err: HttpErrorResponse) => {
          this.handleError(err);
          return throwError(() => err);
        })
      );
  }

  // ========================================
  // COURS - Opérations sur les TPs
  // ========================================

  /**
   * Récupère tous les TPs d'un cours.
   * GET /course/{courseId}/TPs
   * @param courseId - L'identifiant du cours
   * @returns Observable<TP_Model[]>
   */
  getTPsByCourseId(courseId: number): Observable<TP_Model[]> {
    return this.http
      .get<TP_Model[]>(
        `${API_URL}/course/${courseId}/TPs`,
        this.httpOptions
      )
      .pipe(
        retry(1),
        catchError((err: HttpErrorResponse) => {
          this.handleError(err);
          return of([] as TP_Model[]);
        })
      );
  }

  /**
   * Récupère les détails d'un TP spécifique.
   * GET /course/{courseId}/TPs/{tpNumber}
   * @param courseId - L'identifiant du cours
   * @param tpNumber - Le numéro du TP
   * @returns Observable<TP_Model>
   */
  getTPDetails(courseId: number, tpNumber: number): Observable<TP_Model> {
    return this.http
      .get<TP_Model>(
        `${API_URL}/course/${courseId}/TPs/${tpNumber}`,
        this.httpOptions
      )
      .pipe(
        retry(1),
        catchError((err: HttpErrorResponse) => {
          this.handleError(err);
          return throwError(() => err);
        })
      );
  }

  /**
   * Crée un TP par numéro.
   * POST /course/{courseId}/TPs/{tpNumber}
   * @param courseId - L'identifiant du cours
   * @param tpNumber - Le numéro du TP
   * @param tpData - Données optionnelles du TP
   * @returns Observable<TP_Model>
   */
  createTP(courseId: number, tpNumber: number, tpData?: any): Observable<TP_Model> {
    return this.http
      .post<TP_Model>(
        `${API_URL}/course/${courseId}/TPs/${tpNumber}`,
        JSON.stringify(tpData || {}),
        this.httpOptions
      )
      .pipe(
        retry(1),
        catchError((err: HttpErrorResponse) => {
          this.handleError(err);
          return throwError(() => err);
        })
      );
  }

  /**
   * Met à jour un TP.
   * PUT /course/{courseId}/TPs/{tpNumber}
   * @param courseId - L'identifiant du cours
   * @param tpNumber - Le numéro du TP
   * @param tpData - Les nouvelles données du TP
   * @returns Observable<TP_Model>
   */
  updateTP(courseId: number, tpNumber: number, tpData: any): Observable<TP_Model> {
    return this.http
      .put<TP_Model>(
        `${API_URL}/course/${courseId}/TPs/${tpNumber}`,
        JSON.stringify(tpData),
        this.httpOptions
      )
      .pipe(
        retry(1),
        catchError((err: HttpErrorResponse) => {
          this.handleError(err);
          return throwError(() => err);
        })
      );
  }

  /**
   * Supprime un TP.
   * DELETE /course/{courseId}/TPs/{tpNumber}
   * @param courseId - L'identifiant du cours
   * @param tpNumber - Le numéro du TP
   * @returns Observable<void>
   */
  deleteTP(courseId: number, tpNumber: number): Observable<void> {
    return this.http
      .delete<void>(
        `${API_URL}/course/${courseId}/TPs/${tpNumber}`,
        this.httpOptions
      )
      .pipe(
        retry(1),
        catchError((err: HttpErrorResponse) => {
          this.handleError(err);
          return throwError(() => err);
        })
      );
  }

  /**
   * Ajoute un TP à un cours avec son fichier de rendu.
   * Crée le TP puis upload le fichier ZIP associé.
   * POST /course/{courseId}/TPs/{tpNo}
   * POST /course/{courseId}/addRendu/{tpNo}
   * @param courseId - L'identifiant du cours
   * @param tpNo - Le numéro du TP
   * @param file - Le fichier de rendu (archive ZIP)
   * @returns Observable<TP_Model>
   */
  addTpToCourse(courseId: number, tpNo: number, file: File): Observable<TP_Model> {
    return this.http.post<TP_Model>(`${API_URL}/course/${courseId}/TPs/${tpNo}`, {}).pipe(
      tap({
        next: resp => console.log('Réponse du backend à addTP:', resp),
        error: err => console.error('Erreur sur addTP:', err)
      }),
      switchMap((tp) => {
        const formData = new FormData();
        formData.append('file', file);
        return this.http.post<TP_Model>(`${API_URL}/course/${courseId}/addRendu/${tpNo}`, formData);
      }),
      catchError((error) => {
        alert('Erreur lors de la création du TP ou de l\'upload du rendu.');
        return throwError(() => error);
      })
    );
  }

  /**
   * Ajoute un fichier de rendu à un TP existant.
   * POST /course/{courseId}/addRendu/{tpNo}
   * @param courseId - L'identifiant du cours
   * @param tpNo - Le numéro du TP
   * @param file - Le fichier de rendu (archive ZIP)
   * @returns Observable<any>
   */
  addRendu(courseId: number, tpNo: number, file: File): Observable<any> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http
      .post(`${API_URL}/course/${courseId}/addRendu/${tpNo}`, formData)
      .pipe(
        retry(1),
        catchError((err: HttpErrorResponse) => {
          this.handleError(err);
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
    return this.http
      .post(
        `${API_URL}/course/${courseId}/startProcessSubmission/${tpNo}`,
        {},
        this.httpOptions
      )
      .pipe(
        retry(1),
        catchError((err: HttpErrorResponse) => {
          this.handleError(err);
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
    return this.http
      .post(
        `${API_URL}/course/${courseId}/manageTP/${tpNo}`,
        {},
        this.httpOptions
      )
      .pipe(
        retry(1),
        catchError((err: HttpErrorResponse) => {
          this.handleError(err);
          return throwError(() => err);
        })
      );
  }

  /**
   * Démarre le traitement de submission et gère les statuts (version 2).
   * Enchaîne startProcessSubmission puis manageTP.
   * @param courseId - L'identifiant du cours
   * @param tpNo - Le numéro du TP
   * @returns Observable<TP_Model>
   */
  manageTPsubmissionv2(courseId: number, tpNo: number) {
    return this.http
      .post<TP_Model>(
        `${API_URL}/course/${courseId}/startProcessSubmission/${tpNo}`,
        {},
        this.httpOptions
      )
      .pipe(
        tap({
          next: resp => console.log('Réponse du backend à startProcessZip:', resp),
          error: err => console.error('Erreur sur startProcessZip:', err)
        }),
        switchMap((tp) => {
          return this.http.post<TP_Model>(`${API_URL}/course/${courseId}/manageTP/${tpNo}`, {});
        }),
        catchError((err: HttpErrorResponse) => {
          this.handleError(err);
          return throwError(() => err);
        })
      );
  }

  /**
   * Démarre le traitement de submission et gère les statuts.
   * Enchaîne startProcessSubmission puis manageTP.
   * @param courseId - L'identifiant du cours
   * @param tpNo - Le numéro du TP
   * @returns Observable<TP_Model>
   */
  manageTPsubmission(courseId: number, tpNo: number): Observable<TP_Model> {
    const startUrl = `${API_URL}/course/${courseId}/startProcessSubmission/${tpNo}`;
    const manageUrl = `${API_URL}/course/${courseId}/manageTP/${tpNo}`;

    return this.http.post(startUrl, {}, { responseType: 'text' as const }).pipe(
      switchMap(() => this.http.post<TP_Model>(manageUrl, {})),
      catchError(err => {
        this.handleError(err);
        return throwError(() => err);
      })
    );
  }

  /**
   * Télécharge le fichier ZIP restructuré d'un TP.
   * GET /course/{courseId}/downloadStructuredSubmission/{tpNo}
   * @param courseId - L'identifiant du cours
   * @param tpNo - Le numéro du TP
   * @returns Observable<Blob>
   */
  downloadTP(courseId: number, tpNo: number) {
    const downloadUrl = `${API_URL}/course/${courseId}/downloadStructuredSubmission/${tpNo}`;
    return this.http.get(downloadUrl, { responseType: 'blob' });
  }

  // ========================================
  // STATUTS DE TP : Opérations CRUD
  // ========================================

  /**
   * Récupère tous les statuts de TP pour un TP spécifique.
   * GET /course/{courseId}/TPs/{tpNumber}/TPStatus
   * @param courseId - L'identifiant du cours
   * @param tpNumber - Le numéro du TP
   * @returns Observable<TPStatusModel[]>
   */
  getAllTPStatusForTP(courseId: number, tpNumber: number): Observable<TPStatusModel[]> {
    return this.http
      .get<TPStatusModel[]>(
        `${API_URL}/course/${courseId}/TPs/${tpNumber}/TPStatus`,
        this.httpOptions
      )
      .pipe(
        retry(1),
        catchError((err: HttpErrorResponse) => {
          this.handleError(err);
          return of([] as TPStatusModel[]);
        })
      );
  }

  /**
   * Rafraîchit les statuts de TP (met à jour ou crée les manquants).
   * POST /course/{courseId}/TPs/{tpNumber}/TPStatusRefresh
   * @param courseId - L'identifiant du cours
   * @param tpNumber - Le numéro du TP
   * @returns Observable<TPStatusModel[]>
   */
  refreshTPStatus(courseId: number, tpNumber: number): Observable<TPStatusModel[]> {
    return this.http
      .post<TPStatusModel[]>(
        `${API_URL}/course/${courseId}/TPs/${tpNumber}/TPStatusRefresh`,
        {},
        this.httpOptions
      )
      .pipe(
        retry(1),
        catchError((err: HttpErrorResponse) => {
          this.handleError(err);
          return throwError(() => err);
        })
      );
  }

  /**
   * Récupère les détails d'un statut de TP.
   * GET /TPStatus/{statusId}
   * @param statusId - L'identifiant du statut
   * @returns Observable<TPStatusModel>
   */
  getTPStatusById(statusId: number): Observable<TPStatusModel> {
    return this.http
      .get<TPStatusModel>(
        `${API_URL}/TPStatus/${statusId}`,
        this.httpOptions
      )
      .pipe(
        retry(1),
        catchError((err: HttpErrorResponse) => {
          this.handleError(err);
          return throwError(() => err);
        })
      );
  }

  /**
   * Met à jour un statut de TP (change le type de rendu).
   * PUT /TPStatus/{statusId}?submissionType={newType}
   * @param statusId - L'identifiant du statut
   * @param newType - Le nouveau type de rendu
   * @returns Observable<TPStatusModel>
   */
  updateTPStatus(statusId: number, newType: StudentSubmissionType): Observable<TPStatusModel> {
    return this.http
      .put<TPStatusModel>(
        `${API_URL}/TPStatus/${statusId}?submissionType=${newType}`,
        {},
        this.httpOptions
      )
      .pipe(
        retry(1),
        catchError((err: HttpErrorResponse) => {
          this.handleError(err);
          return throwError(() => err);
        })
      );
  }

  /**
   * Supprime un statut de TP.
   * DELETE /TPStatus/{statusId}
   * @param statusId - L'identifiant du statut
   * @returns Observable<void>
   */
  deleteTPStatus(statusId: number): Observable<void> {
    return this.http
      .delete<void>(
        `${API_URL}/TPStatus/${statusId}`,
        this.httpOptions
      )
      .pipe(
        retry(1),
        catchError((err: HttpErrorResponse) => {
          this.handleError(err);
          return throwError(() => err);
        })
      );
  }

  // ========================================
  // GESTION DES ERREURS
  // ========================================

  /**
   * Gère les erreurs HTTP en affichant une alerte.
   * @param error - L'erreur HTTP reçue
   */
  private handleError(error: HttpErrorResponse): void {
    const msg =
      error.error && typeof error.error === 'string'
        ? `API a retourné une erreur : ${error.error}`
        : `Erreur serveur (code = ${error.status})\nMessage : ${error.message}`;
    window.alert(msg);
  }
}
