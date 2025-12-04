import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { CourseDetailsModel, StudentModel, TP_Model } from '../../models/courseDetails.model';
import { FooterComponent } from '../../../shared/components/footer/footer.component';
import { HeaderComponent } from '../../../shared/components/header/header.component';
import { FormsModule } from '@angular/forms';
import { AddStudent } from './Forms/add-student/add-student';
import { AddTP } from './Forms/rendu/addTP';
import { CourseService } from '../../services/course.service';
import { CommonModule } from '@angular/common';
import { UploadFileTxt } from './Forms/upload-file-txt/upload-file-txt';
import { MATERIAL_MODULES } from '../../../shared/imports/material-imports';
import { TableSubmissionsComponent } from './TableSubmissions/table-submissions.component';

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
    TableSubmissionsComponent
  ]
})
export class CoursComponent implements OnInit {
  //Composant pour la page d'un Cours : contiendra une section d'ajout de TP, CC, et Exam
  // En plus de ça, contiendra une grille avec la liste des étudiants X les TPs (pour voir
  // qui a rendu ou pas)
  private route = inject(ActivatedRoute)
  private courseService = inject(CourseService);

  constructor() {
  }

  courseId = signal<number | undefined>(undefined);
  course!: CourseDetailsModel;
  students: StudentModel[] = [];
  tps: TP_Model[] = [];

  isEditing: boolean = false

  ngOnInit(): void {
    const params = this.route.snapshot.params;
    this.courseId.set(params['id'] ? parseInt(params['id']) : undefined);
    this.course = this.route.snapshot.data['course'];

    // Charge les étudiants dès l'init
    this.loadStudents();
    // Charge les TPs dès l'init
    this.loadTPInfos();
  }

  loadStudents(): void {
    // Vérification si courseId est défini
    if (this.courseId() === undefined) {
      console.error('Course ID is not defined');
      return;
    }
    // Logique pour charger les étudiants du cours
    console.log('Chargement des étudiants pour le cours ID:', this.courseId());
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
    // lancer le process de submissions et des TPStatus
    console.log('Chargement des infos TP pour le cours ID:', this.courseId());
    this.courseService.getTPsByCourseId(this.courseId() as number).subscribe(
      tps => {
        console.log('TPs payload:', JSON.parse(JSON.stringify(tps)));
        this.tps = tps;
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
}
