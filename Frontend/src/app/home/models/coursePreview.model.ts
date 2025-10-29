import { CourseType } from './courseType.model';
import {SemesterType} from './semesterType.model';

export interface CoursePreview {

  id?: number;
  name: string;
  code: string;
  semester: SemesterType;
  year_course: number;
  teacher: string;
  courseType: CourseType;
}

// For creation, drop `id`
