import { SemesterType } from './semesterType.model';
import { CourseType } from './courseType.model';
import { StudyType } from './studyType.model';
import { TPStatusModel } from './tpStatus.model';

// Modèle détaillé d'un cours avec ses listes d'étudiants, TPs et évaluations
export interface CourseDetailsModel {
  id?: number;
  name: string;
  code: string;
  semester: SemesterType;
  year_course: number;
  teacher: string;
  courseType: CourseType;
  studentList: StudentModel[];
  tpsList: TP_Model[];
  evaluations: EvaluationModel[];
}

// Modèle d'un étudiant
export interface StudentModel {
  id?: number;
  name: string;
  email: string;
  studyType: StudyType;
}

// Modèle d'une évaluation
interface EvaluationModel {
  id?: number;
  name: string;
  date: Date;
  submission: SubmissionModel;
}

// Modèle d'un TP (Travail Pratique)
export interface TP_Model {
  id?: number;
  no: number;
  course: CourseDetailsModel
  submission: SubmissionModel;
  statusStudents: TPStatusModel[];
}

// Modèle d'une soumission de fichier
interface SubmissionModel {
  id?: number;
  fileName: string;
  pathStorage: string;
  pathFileStructured: string;
}

// Ré-export de TPStatusModel pour faciliter les imports
export type { TPStatusModel };
