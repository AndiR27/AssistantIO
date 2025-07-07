import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpErrorResponse, HttpHeaders } from '@angular/common/http';
import { Observable, throwError, of } from 'rxjs';
import { retry, catchError } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { CoursePreview } from '../../home/models/coursePreview.model';
import { CourseDetailsModel, StudentModel } from '../../home/models/courseDetails.model';

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

  /** Affiche une alert() et ne retourne rien */
  private handleError(error: HttpErrorResponse): void {
    const msg =
      error.error && typeof error.error === 'string'
        ? `API a retourné une erreur : ${error.error}`
        : `Erreur serveur (code = ${error.status})\nMessage : ${error.message}`;
    window.alert(msg);
  }
}
