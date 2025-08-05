import {Component, inject, OnInit, signal} from '@angular/core';
import {ActivatedRoute, RouterLink} from '@angular/router';
import {CourseDetailsModel, StudentModel, TP_Model} from '../../models/courseDetails.model';
import {FooterComponent} from '../../../shared/components/footer/footer.component';
import {HeaderComponent} from '../../../shared/components/header/header.component';
import {FormsModule} from '@angular/forms';
import {AddStudent} from './Forms/add-student/add-student';
import {AddTP} from './Forms/rendu/addTP';
import {CourseService} from '../../services/course.service';
import {CommonModule} from '@angular/common';
import {UploadFileTxt} from './Forms/upload-file-txt/upload-file-txt';
import {MATERIAL_MODULES} from '../../../shared/imports/material-imports';

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
    AddTP
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
  isDownloading = false;

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

  //Retourne true si le TP et le rendu sont créés et correctement formattés
  shouldShowDownload(tp: TP_Model): boolean {
    // Vérifie si le TP a une soumission et un chemin de fichier structuré
    if (!tp.submission) {
      console.warn(`Le TP ${tp.no} n'a pas de soumission associée.`);
      return false;
    } else {
      // Vérifie si le TP a un rendu et un chemin de fichier structuré
      return !(tp.submission.pathFileStructured === null || tp.submission.pathFileStructured === '');
    }
  }

  //Permet de télécharger le rendu restructuré d'un TP
  onDownloadClick(tp: TP_Model) {

    if (!this.courseId || !tp.submission?.pathFileStructured) {
      console.warn('Aucun chemin de téléchargement disponible.');
      return;
    }

    this.isDownloading = true;
    this.courseService.downloadStructuredFile(this.courseId() as number, tp.no).subscribe({
      next: blob => {
        // Génère un nom de fichier propre
        const name = `TP${tp.no}_RenduRestructuration.zip`;

        // Crée une URL temporaire pour le blob
        const downloadUrl = window.URL.createObjectURL(blob);

        // Construit et déclenche un <a> pour le télécharger
        const a = document.createElement('a');
        a.href = downloadUrl;
        a.download = name;
        document.body.appendChild(a);
        a.click();

        // Nettoi
        document.body.removeChild(a);
        window.URL.revokeObjectURL(downloadUrl);
      },
      error: err => {
        console.error(`Erreur lors du téléchargement du TP ${tp.no}:`, err);
      },
      complete: () => {
        this.isDownloading = false;
      }
    });
  }



  //Permet de démarrer le process de submission pour un TP
  startProcessSubmission(tp: TP_Model) {
    if (!this.courseId()) return;

    console.log(`Démarrage du process de submission pour le TP ${tp.no} du cours ${this.courseId()}`);
    this.courseService.startProcessSubmission(this.courseId() as number, tp.no).subscribe({
      next: (updatedTP) => {
        console.log(`Process de submission démarré pour le TP ${tp.no}`, updatedTP);
        // Mettre à jour la liste des TPs
        this.loadTPInfos();
      },
      error: (err) => {
        console.error(`Erreur lors du démarrage du process de submission pour le TP ${tp.no}`, err);
      }
    });
  }
  // Permet de récupérer le statut d'un rendu pour un étudiant et un TP donnés
  getStatutForStudent(tp: TP_Model, studentId: number | undefined): boolean | null {
    if (!tp.statusStudents || !studentId) {
      return null;
    }

    const status = tp.statusStudents.find(
      s => s.studentId === studentId
    );

    return status?.studentSubmission ?? null;

  }
  /**
  // Permet de récupérer la classe CSS associée à un statut afin de gérer l'affichage
  getStatutClass(statut: boolean | null): string {

    switch (statut) {
      case 'RECU': return 'tp-recu';
      case 'NON_RECU': return 'tp-non-recu';
      case 'INCOMPLET': return 'tp-incomplet';
      case 'EXCUSE': return 'tp-excuse';
      default: return 'tp-inconnu';
    }

  }*/

  formatStatut(statut: boolean | null): boolean {
    if(statut === null) {
      return false; // Statut inconnu
    }
    return statut; // Retourne le statut tel quel (true ou false)
  }


}
