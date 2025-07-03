import {Injectable} from '@angular/core';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import {Observable, throwError} from 'rxjs';
import {retry, catchError} from 'rxjs/operators';
import {environment} from '../../../environments/environment';
import {CoursePreview} from '../../home/models/coursePreview.model';

const API_URL = environment.apiUrl;

const headers = new Headers();
headers.append('Content-Type', 'application/json');
// Add CORS headers to allow cross-origin requests
headers.append('Access-Control-Allow-Origin', '*');

@Injectable({
  providedIn: 'root'
})
export class ApiService{
  private apiUrl = 'http://localhost:8088';
  constructor(private http: HttpClient) {
    // Le constructeur initialise le service avec HttpClient pour faire des requêtes HTTP

  }
  httpOptions = {
    headers: new HttpHeaders({
      'Content-Type': 'application/json'
    })
  };

  // Getter pour l'URL de l'API
  getRoute(): string {
    return this.apiUrl;
  }

  // Méthode GET pour récupérer des données depuis l'API
  getAllCourses(): Observable<CoursePreview[]>{
    return this.http.get<CoursePreview[]>(`${API_URL}/admin/v2/courses/all`, this.httpOptions)
      .pipe(retry(1), catchError(this.handleError));
  }

  // Méthode POST pour ajouter un nouveau cours
  addCourse(course: CoursePreview): Observable<CoursePreview> {
    return this.http.post<CoursePreview>(`${API_URL}/admin/v2/courses/addCourse`, JSON.stringify(course), this.httpOptions)
      .pipe(retry(1), catchError(this.handleError));
  }

  // Error handling
  handleError(error:any) {
    let errorMessage = '';
    if (error.error instanceof ErrorEvent) {
      // Get client-side error
      errorMessage = error.error.message;
    } else {
      // Get server-side error
      errorMessage = `Error Code: ${error.status}\nMessage: ${error.message}`;
    }
    window.alert(errorMessage);
    return throwError(() => new Error(errorMessage));
  }



}
