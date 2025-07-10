import {Component, inject, OnInit, signal} from '@angular/core';
import {ActivatedRoute, RouterLink} from '@angular/router';
import {CourseDetailsModel, StudentModel} from '../../models/courseDetails.model';
import {FooterComponent} from '../../../shared/components/footer/footer.component';
import {HeaderComponent} from '../../../shared/components/header/header.component';
import {FormsModule} from '@angular/forms';
import {AddStudent} from './Forms/add-student/add-student';
import {AddTP} from './Forms/rendu/addTP';
import {MatCard, MatCardContent, MatCardHeader, MatCardTitle} from '@angular/material/card';
import {CourseService} from '../../services/course.service';
import {CommonModule} from '@angular/common';
import {MatButton} from '@angular/material/button';
import {UploadFileTxt} from './Forms/upload-file-txt/upload-file-txt';

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
    MatCardHeader,
    MatCardContent,
    MatCardTitle,
    MatCard,
    MatButton,
    RouterLink,
    UploadFileTxt,
    AddTP
  ]
})

export class CoursComponent implements OnInit{
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

  ngOnInit(): void{
    const params = this.route.snapshot.params;
    this.courseId.set(params['id'] ? parseInt(params['id']) : undefined);
    this.course = this.route.snapshot.data['course'];
    // Charge les étudiants dès l'init
    this.loadStudents();
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
      students =>{
          this.students = students;
      }
    );
  }
}
