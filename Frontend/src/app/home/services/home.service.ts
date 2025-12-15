import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { catchError, Observable, of, tap, throwError } from 'rxjs';
import { map } from 'rxjs/operators';
import { CoursePreview } from '../models/coursePreview.model';
import { ApiService } from '../../shared/services/api.service';
import { SemesterType } from '../models/semesterType.model';
import { CourseType } from '../models/courseType.model';

/**
 * Service pour gérer les opérations liées aux cours sur la page d'accueil.
 * Utilise ApiService pour effectuer les requêtes HTTP vers le backend.
 */
@Injectable({ providedIn: 'root' })
export class HomeService {

  coursePreviews: CoursePreview[] = [];
  private baseURL = '/admin/courses';

  constructor(private api: ApiService) { }

  // ========================================
  // COURSE OPERATIONS
  // ========================================

  /**
   * Récupère la liste de tous les cours.
   * GET /admin/courses/all
   * @returns Observable<CoursePreview[]>
   */
  getCoursePreviews(): Observable<CoursePreview[]> {
    console.log('getCoursePreviews called');
    return this.api.getAllCourses().pipe(
      tap(res => console.log('API ', this.api.getRoute(this.baseURL), ' raw response:', res)),
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

  /**
   * Ajoute un nouveau cours.
   * POST /admin/courses
   * @param course - Les données du cours à ajouter
   * @returns Observable<CoursePreview>
   */
  addCourse(course: CoursePreview): Observable<CoursePreview> {
    console.log('addCourse called');
    return this.api.addCourse(course).pipe(
      map(dto => ({
        id: 0,
        name: dto.name,
        code: dto.code,
        teacher: dto.teacher,
        year_course: dto.year_course,
        semester: dto.semester as SemesterType,
        courseType: dto.courseType as CourseType
      })),
      catchError(error => {
        console.error('Error adding course:', error);
        return throwError(() => error);
      })
    );
  }

  /**
   * Met à jour un cours existant.
   * PUT /admin/courses/{courseId}
   * @param course - Les données du cours mises à jour
   * @returns Observable<CoursePreview>
   */
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

  /**
   * Supprime un cours.
   * DELETE /admin/courses/{courseId}
   * @param courseId - L'identifiant du cours à supprimer
   * @returns Observable<void>
   */
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
