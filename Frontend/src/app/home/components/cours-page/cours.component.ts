import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { CourseDetailsModel, StudentModel, TP_Model, EvaluationModel } from '../../models/courseDetails.model';
import { FooterComponent } from '../../../shared/components/footer/footer.component';
import { HeaderComponent } from '../../../shared/components/header/header.component';
import { FormsModule } from '@angular/forms';
import { AddStudent } from './Forms/add-student/add-student';
import { AddTP } from './Forms/rendu/addTP';
import { CourseService } from '../../services/course.service';
import { HomeService } from '../../services/home.service';
import { CommonModule } from '@angular/common';
import { MatSnackBar } from '@angular/material/snack-bar';
import { UploadFileTxt } from './Forms/upload-file-txt/upload-file-txt';
import { MATERIAL_MODULES } from '../../../shared/imports/material-imports';
import { TableSubmissionsComponent } from './TableSubmissions/table-submissions.component';
import { EvaluationSectionComponent } from './EvaluationSection/evaluation-section';

@Component({
  selector: 'app-cours',
  standalone: true,
  templateUrl: './cours.component.html',
  styleUrls: ['./cours.component.css'],
  imports: [
    FooterComponent,
    HeaderComponent,
    CommonModule,
    FormsModule,
    AddStudent,
    MATERIAL_MODULES,
    RouterLink,
    UploadFileTxt,
    AddTP,
    TableSubmissionsComponent,
    EvaluationSectionComponent
  ]
})
export class CoursComponent implements OnInit {
  //Composant pour la page d'un Cours : contiendra une section d'ajout de TP, CC, et Exam
  // En plus de ça, contiendra une grille avec la liste des étudiants X les TPs (pour voir
  // qui a rendu ou pas)
  private route = inject(ActivatedRoute)
  private courseService = inject(CourseService);
  private homeService = inject(HomeService);
  private snackBar = inject(MatSnackBar);

  constructor() {
  }

  courseId = signal<number | undefined>(undefined);
  course!: CourseDetailsModel;
  students: StudentModel[] = [];
  tps: TP_Model[] = [];
  evaluations: EvaluationModel[] = [];

  isEditing: boolean = false;
  isEditingLink: boolean = false;
  editLinkValue: string = '';

  ngOnInit(): void {
    const params = this.route.snapshot.params;
    this.courseId.set(params['id'] ? parseInt(params['id']) : undefined);
    this.course = this.route.snapshot.data['course'];

    // Charge les étudiants dès l'init
    this.loadStudents();
    // Charge les TPs dès l'init
    this.loadTPInfos();
    // Charge les évaluations dès l'init
    this.loadEvaluations();
  }

  loadStudents(): void {
    // Vérification si courseId est défini
    if (this.courseId() === undefined) {
      console.error('Course ID is not defined');
      return;
    }
    this.courseService.getStudentsByCourseId(this.courseId() as number).subscribe(
      students => {
        this.students = students;
      }
    );
  }

  loadTPInfos(): void {
    // Vérification si courseId est défini
    if (this.courseId() === undefined) {
      console.error('Course ID is not defined');
      return;
    }
    this.courseService.getTPsByCourseId(this.courseId() as number).subscribe(
      tps => {
        this.tps = tps;
      }
    );
  }

  loadEvaluations(): void {
    if (this.courseId() === undefined) {
      console.error('Course ID is not defined');
      return;
    }
    this.courseService.getEvaluationsByCourseId(this.courseId() as number).subscribe(
      evaluations => {
        this.evaluations = evaluations;
      }
    );
  }

  //met à jour un étudiant et le rafraichit dans la liste
  updateStudent(student: StudentModel): void {
    const index = this.students.findIndex(s => s.id === student.id);
    if (index !== -1) {
      this.students[index] = student;
    }
  }

  startEditLink(): void {
    this.editLinkValue = this.course.cyberlearn_link || '';
    this.isEditingLink = true;
  }

  cancelEditLink(): void {
    this.isEditingLink = false;
  }

  saveLink(): void {
    let link = this.editLinkValue.trim();
    if (link && !/^https?:\/\//i.test(link)) {
      link = 'https://' + link;
    }

    const payload = {
      id: this.course.id,
      name: this.course.name,
      code: this.course.code,
      semester: this.course.semester,
      year_course: this.course.year_course,
      teacher: this.course.teacher,
      courseType: this.course.courseType,
      cyberlearn_link: link
    };

    this.homeService.updateCourse(payload).subscribe({
      next: () => {
        this.course.cyberlearn_link = link;
        this.isEditingLink = false;
        this.snackBar.open('Lien Cyberlearn mis à jour', 'OK', { duration: 3000 });
      },
      error: (err) => {
        console.error('Error updating cyberlearn link:', err);
        this.snackBar.open('Erreur lors de la mise à jour', 'Fermer', { duration: 5000 });
      }
    });
  }
}
