import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpErrorResponse, HttpHeaders } from '@angular/common/http';
import {Observable, throwError, of, switchMap, tap} from 'rxjs';
import { retry, catchError } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { CoursePreview } from '../../home/models/coursePreview.model';
import {CourseDetailsModel, StudentModel, TP_Model} from '../../home/models/courseDetails.model';

const API_URL = environment.apiUrl;

@Injectable({ providedIn: 'root' })
export class ApiService {
  private http = inject(HttpClient);
  private httpOptions = {
    headers: new HttpHeaders({ 'Content-Type': 'application/json' })
  };

  /** GET route for debugging */
  getRoute(route: string): string {
    return `${API_URL}${route}`;
  }
  /** GET all courses; on error, alert + return [] */
  getAllCourses(): Observable<CoursePreview[]> {
    return this.http
      .get<CoursePreview[]>(`${API_URL}/admin/v2/courses/all`, this.httpOptions)
      .pipe(
        retry(1),
        catchError((err: HttpErrorResponse) => {
          this.handleError(err);
          return of([] as CoursePreview[]);
        })
      );
  }

  /** POST add a course; on error, alert + re-throw */
  addCourse(course: CoursePreview): Observable<CoursePreview> {
    return this.http
      .post<CoursePreview>(
        `${API_URL}/admin/v2/courses/addCourse`,
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

  /** GET course details by ID; on error, alert + re-throw */
  getCourseById(id: number): Observable<CourseDetailsModel> {
    return this.http
      .get<CourseDetailsModel>(
        `${API_URL}/admin/v2/courses/${id}`,
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

  /** POST add one student to course; on error, alert + re-throw */
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

  /** GET all students of a course; on error, alert + return [] */
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

  /** POST add students from a TXT file; on error, alert + re-throw */
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
  /** POST add a TP to a course + add submission to it; on error, alert + re-throw */
  addTpToCourse(courseId: number, tpNo: number, file: File): Observable<TP_Model> {
    // Création du TP
    return this.http.post<TP_Model>(`${API_URL}/course/${courseId}/addTP/${tpNo}`, {}).pipe(
      tap({
        next: resp => console.log('Réponse du backend à addTP:', resp),
        error: err => console.error('Erreur sur addTP:', err)
      }),
      switchMap((tp) => {
        // Upload du rendu ZIP associé au TP
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

  /** GET all TPs of a course; on error, alert + return [] */
  getTPsByCourseId(courseId: number): Observable<TP_Model[]> {
    return this.http
      .get<TP_Model[]>(
        `${API_URL}/course/${courseId}/tps`,
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

  /** POST start the submission process for a TP + manage TPstatus of it; on error, alert + re-throw */
  manageTPsubmissionv2(courseId: number, tpNo: number) {
    // Démarrage du process de submission pour le TP
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
          // Gestion des TPstatus pour chaque étudiant
          return this.http.post<TP_Model>(`${API_URL}/course/${courseId}/manageTP/${tpNo}`, {});
        }),
        catchError((err: HttpErrorResponse) => {
          this.handleError(err);
          return throwError(() => err);
        })
      );
  }
  /** POST start the submission process for a TP + manage TPstatus of it; on error, alert + re-throw */
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
  /** GET download a TP structured file; on error, alert + re-throw */
  downloadTP(courseId: number, tpNo: number) {
    const downloadUrl = `${API_URL}/course/${courseId}/downloadRestructuredZip/${tpNo}`;
    return this.http.get(downloadUrl, {responseType: 'blob'});
  }

  /** Affiche une alert() et ne retourne rien */
  private handleError(error: HttpErrorResponse): void {
    const msg =
      error.error && typeof error.error === 'string'
        ? `API a retourné une erreur : ${error.error}`
        : `Erreur serveur (code = ${error.status})\nMessage : ${error.message}`;
    window.alert(msg);
  }



}
