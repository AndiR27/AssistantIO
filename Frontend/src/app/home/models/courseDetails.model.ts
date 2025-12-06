import { SemesterType } from './semesterType.model';
import { CourseType } from './courseType.model';
import { StudyType } from './studyType.model';
import { TPStatusModel } from './tpStatus.model';



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

export interface StudentModel {
  id?: number;
  name: string;
  email: string;
  studyType: StudyType;
}

interface EvaluationModel {
  // private Long id;
  // private String name;
  // private LocalDateTime date;
  id?: number;
  name: string;
  date: Date;
  submission: SubmissionModel;
}



export interface TP_Model {

  id?: number;
  no: number;
  course: CourseDetailsModel
  submission: SubmissionModel;
  statusStudents: TPStatusModel[];
}

interface SubmissionModel {
  id?: number;
  fileName: string;
  pathStorage: string;
  pathFileStructured: string;
}

// Re-export TPStatusModel for convenience
export type { TPStatusModel };
