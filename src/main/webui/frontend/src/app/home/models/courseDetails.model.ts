import {SemesterType} from './semesterType.model';
import {CourseType} from './courseType.model';
import {StudyType} from './studyType.model';



export interface CourseDetailsModel{
  id?: number;
  name: string;
  code: string;
  semester: SemesterType;
  year_course: number;
  teacher: string;
  courseType: CourseType;
  studentList: StudentModel[];
  tpsList:     TP_Model[];
  evaluations: EvaluationModel[];
}

export interface StudentModel{
  id?: number;
  name: string;
  email: string;
  studyType: StudyType;
}

interface EvaluationModel{
  // private Long id;
  // private String name;
  // private LocalDateTime date;
  id?: number;
  name: string;
  date: Date;
  submission: SubmissionModel;
}



interface TP_Model{

  id?: number;
  no: number;
  courseId?: number; // Assuming CourseDTO is represented by its ID
  submission: SubmissionModel;
  statusStudents: TPStatusModel[];
}

interface SubmissionModel {
  id?: number;
  fileName: string;
  pathStorage: string;
  pathFileStructured: string;
}

interface TPStatusModel {
  id?: number;
  studentId: number; // Assuming StudentDTO is represented by its ID
  tpId: number; // Assuming TP_Model is represented by its ID
  status: boolean; // Could be an enum or string depending on your design
}
