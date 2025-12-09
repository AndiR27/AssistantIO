import { CourseType } from './courseType.model';
import { SemesterType } from './semesterType.model';

// Modèle pour l'aperçu d'un cours (utilisé dans la liste des cours)
export interface CoursePreview {
  id?: number;
  name: string;
  code: string;
  semester: SemesterType;
  year_course: number;
  teacher: string;
  courseType: CourseType;
}
