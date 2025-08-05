import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {ApiService} from '../../shared/services/api.service';
import {catchError, Observable, of, tap, throwError} from 'rxjs';
import {map} from 'rxjs/operators';
import {SemesterType} from '../models/semesterType.model';
import {CourseType} from '../models/courseType.model';
import {CoursePreview} from '../models/coursePreview.model';
import {CourseDetailsModel, StudentModel, TP_Model} from '../models/courseDetails.model';

@Injectable({ providedIn: 'root' })
export class CourseService {
  private baseUrl = '/admin/v2/course/{courseId}';

  constructor(private api: ApiService) {
  }

  // Récupère un cours par son ID et toutes ses informations
  /** Récupère un seul cours par son ID */
  getCourseById(id: number): Observable<CourseDetailsModel> {
    return this.api.getCourseById(id).pipe(
      tap(res => console.log('API', this.api.getRoute("/admin/v2/course/{courseId}"), 'raw response:', res)),
      map((res: any) => {
        // Si données sont sous res.data ou directement dans res
        const dto = Array.isArray(res)
          ? res[0]
          : res.data
            ? (Array.isArray(res.data) ? res.data[0] : res.data)
            : res;

        // On reconstruit notre modèle
        return {
          /**
           * id?: number;
           *   name: string;
           *   code: string;
           *   semester: SemesterType;
           *   year_course: number;
           *   teacher: string;
           *   courseType: CourseType;
           *   studentList: StudentModel[];
           *   tpsList:     TP_Model[];
           *   evaluations: EvaluationModel[];
           */
          id:           dto.id,
          name:         dto.name,
          code:         dto.code,
          teacher:      dto.teacher,
          year_course:  dto.year_course,
          semester:     dto.semester  as SemesterType,
          courseType:   dto.courseType as CourseType,
          studentList:  dto.studentList  || [],
          tpsList:      dto.tpsList      || [],
          evaluations:  dto.evaluations  || []
        } as CourseDetailsModel;
      }),
      catchError(err => {
        console.error('Error in getCourseById():', err);
        // selon besoin, vous pouvez renvoyer un fallback ou propager l'erreur
        return throwError(() => err);
        // ou : return of(null as any);
      })
    );
  }

  // Add one student to course
  addStudentToCourse(courseId: number, student: StudentModel): Observable<StudentModel> {
    return this.api.addStudentToCourse(courseId, student).pipe(
      tap(res => console.log('API', this.api.getRoute("/course/{courseId}/addStudent"), 'raw response:', res)),
      map((res: any) => {
        // On reconstruit notre modèle
        return {
          id: res.id,
          name: res.name,
          email: res.email,
          studyType: res.studyType
        } as StudentModel; // Remplacer 'any' par le type approprié si nécessaire
      }),
      catchError(err => {
        console.error('Error in addStudentToCourse():', err);
        return throwError(() => err);
      })
    );
  }

  // Get all students of a course
  getStudentsByCourseId(courseId: number): Observable<StudentModel[]> {
    return this.api.getStudentsByCourseId(courseId).pipe(
      tap(res => console.log('API', this.api.getRoute("/course/{courseId}/students"), 'raw response:', res)),
      map((res: any) => {
        // On reconstruit notre modèle
        return res.map((student: any) => ({
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

  // Add students from file
  addStudentsFromFile(courseId: number, file: File): Observable<any> {
    return this.api.addStudentsFromFile(courseId, file).pipe(
      tap(res => console.log('API', this.api.getRoute("/course/{courseId}/addStudentsFromFile"), 'raw response:', res)),
      catchError(err => {
        console.error('Error in addStudentsFromFile():', err);
        return throwError(() => err);
      })
    );
  }

  // Add a TP to a course (and add submission directly to it)
  addTpToCourse(courseId: number, tpNo: number, file: File): Observable<TP_Model>{
    return this.api.addTpToCourse(courseId, tpNo, file).pipe(
      tap(res => console.log('API', this.api.getRoute("/course/{courseId}/addTP"), 'raw response:', res)),
      catchError(err => {
        console.error('Error in addTpToCourse():', err);
        return throwError(() => err);
      })
    );
  }

  // Get all TPs of a course
  getTPsByCourseId(courseId: number): Observable<TP_Model[]> {
    return this.api.getTPsByCourseId(courseId).pipe(
      tap(res => console.log('API', this.api.getRoute("/course/{courseId}/tps"), 'raw response:', res)),
      map((res: any) => {
        // On reconstruit notre modèle
        return res.map((tp: any) => ({
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

  //start process submission and manage TPstatus for every student for a given TP and course
  startProcessSubmission(courseId: number, tpNo: number): Observable<any>{
    return this.api.manageTPsubmission(courseId, tpNo).pipe(
      tap(res => console.log('API', this.api.getRoute("/course/{courseId}/addTP"), 'raw response:', res)),
      catchError(err => {
        console.error('Error in addTpToCourse():', err);
        return throwError(() => err);
      })
    );
  }


  //Download the structured file of a TP
  downloadStructuredFile(courseId: number, tpNo: number): Observable<Blob> {
    return this.api.downloadTP(courseId, tpNo).pipe(
      catchError(err => {
        console.error('Error in downloadTP():', err);
        return throwError(() => err);
      })
    );
  }




}





