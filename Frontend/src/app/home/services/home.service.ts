import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { catchError, Observable, of, tap, throwError } from 'rxjs';
import { map } from 'rxjs/operators';
import { CoursePreview } from '../models/coursePreview.model';
import { ApiService } from '../../shared/services/api.service';
import { SemesterType } from '../models/semesterType.model';
import { CourseType } from '../models/courseType.model';

/**
 * Service : pour gérer les opérations liées aux cours sur la page d'accueil.
 * Il utilise HttpClient pour faire des requêtes HTTP vers l'API backend.
 * * @Injectable : Décorateur qui indique que cette classe peut être injectée
 * dans d'autres classes via le système d'injection de dépendances d'Angular.
 * * @HttpClient : Service Angular pour faire des requêtes HTTP.
 * * @Observable : Type de données qui permet de gérer des flux de données asynchrones.
 * * @map : Opérateur RxJS pour transformer les données émises par un Observable.
 */

@Injectable({
  //providedIn : permet d'indiquer la portée du service
  providedIn: 'root' // Le service est disponible dans toute l'application
})

export class HomeService {
  coursePreviews: CoursePreview[] = []; // Propriété pour stocker la liste des cours
  private baseURL = '/admin/courses';
  constructor(private api: ApiService) {
    // Le constructeur initialise le service avec HttpClient pour faire des requêtes HTTP
  }

  //Get data from ApiService, and map it to CoursePreview[]
  //methode : getCourseList
  getCoursePreviews(): Observable<CoursePreview[]> {
    console.log('getCoursePreviews called');
    return this.api.getAllCourses().pipe(
      tap(res => console.log('API ', this.api.getRoute(this.baseURL + "/all"), ' raw response:', res)),
      map((res: any) => {
        const arr: any[] = Array.isArray(res)
          ? res
          : Array.isArray(res?.data)
            ? res.data
            : [];
        return arr.map(dto => ({
          id: dto.id,
          name: dto.name,
          code: dto.code,
          teacher: dto.teacher,
          year_course: dto.year_course,
          semester: dto.semester as SemesterType,
          courseType: dto.courseType as CourseType
        }));
      }),
      catchError(err => {
        console.error('Error in getCoursePreviews():', err);
        return of([] as CoursePreview[]);
      })
    );
  }



  // Add a course
  addCourse(course: CoursePreview): Observable<CoursePreview> {
    console.log('addCourse called');
    return this.api.addCourse(course).pipe(
      map(dto => ({
        id: 0, // Assuming the backend will return the new ID after creation
        name: dto.name,
        code: dto.code,
        teacher: dto.teacher,
        year_course: dto.year_course,
        semester: dto.semester as SemesterType, // Cast to SemesterType
        courseType: dto.courseType as CourseType // Cast to CourseType
      })),
      catchError(error => {
        console.error('Error adding course:', error);
        return throwError(() => error); // Rethrow the error
      })
    );
  }

  // Update a course
  updateCourse(course: CoursePreview): Observable<CoursePreview> {
    console.log('updateCourse called', course);
    return this.api.updateCourse(course).pipe(
      map(dto => ({
        id: dto.id,
        name: dto.name,
        code: dto.code,
        teacher: dto.teacher,
        year_course: dto.year_course,
        semester: dto.semester as SemesterType,
        courseType: dto.courseType as CourseType
      })),
      catchError(error => {
        console.error('Error updating course:', error);
        return throwError(() => error);
      })
    );
  }

  // Delete a course
  deleteCourse(courseId: number): Observable<void> {
    console.log('deleteCourse called', courseId);
    return this.api.deleteCourse(courseId).pipe(
      catchError(error => {
        console.error('Error deleting course:', error);
        return throwError(() => error);
      })
    );
  }

}
