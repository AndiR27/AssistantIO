import { Component, inject, Input, Signal } from '@angular/core';
import { CoursePreview } from '../../models/coursePreview.model';
import { HomeService } from '../../services/home.service';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { Observable, take } from 'rxjs';
import { CoursFormComponent } from '../cours-form/cours-form.component';
import { HeaderComponent } from '../../../shared/components/header/header.component';
import { FooterComponent } from '../../../shared/components/footer/footer.component';
import { toSignal } from '@angular/core/rxjs-interop';
import { MatDialog } from '@angular/material/dialog';
import { DeleteConfirmationDialogComponent } from '../delete-confirmation-dialog/delete-confirmation-dialog.component';
import { ShareCourseDialogComponent } from '../share-course-dialog/share-course-dialog.component';
import { MATERIAL_MODULES } from '../../../shared/imports/material-imports';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../../shared/services/api.service';
import { AuthService } from '../../../shared/services/auth.service';
import { CoursePermission } from '../../models/courseAccess.model';


@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, CoursFormComponent, RouterLink, HeaderComponent, FooterComponent, ...MATERIAL_MODULES, FormsModule],
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css'],
})
//Pour les attributs
/**
 * On les définit par des propriétés de la classe.
 * @Input : Permet de recevoir des données depuis un composant parent (par exemple,
 * dans home.component.html, on peut utiliser <app-root [title]="TitreExemple"></app-root>).
 * @Output : Permet d'émettre des événements vers un composant parent.
 */
export class HomeComponent {
  // Propriété pour stocker le titre de la page d'accueil
  homeService = inject(HomeService);
  dialog = inject(MatDialog);
  private api = inject(ApiService);
  private auth = inject(AuthService);
  title = 'Page d\'accueil';
  // Propriété pour stocker une liste de course
  courseList: CoursePreview[] = [];

  // Set of courseIds where the current user is OWNER
  private ownedCourseIds = new Set<number>();

  // UI state
  isFormExpanded = false;
  editingCourseId: number | null = null;
  editingCourse: CoursePreview | null = null;

  @Input() course!: CoursePreview;
  // Constructeur pour initialiser le composant
  constructor() {
    this.getCourses();
    this.loadMyAccesses();
  }

  //Récupérer la liste des cours avec ngOnInit : cela permet de charger les données dès que le composant est initialisé.
  // ngOnInit() {
  //   this.homeService.getCoursePreviews().subscribe((courseList) =>{this.courseList = courseList;});
  // }

  // Méthode pour récupérer la liste des cours
  getCourses() {
    this.homeService.getCoursePreviews().subscribe(courses => {
      this.courseList = courses;
    });
  }

  refreshCourses() {
    this.getCourses();
  }

  private loadMyAccesses(): void {
    this.api.getMyAccesses().subscribe(accesses => {
      this.ownedCourseIds = new Set(
        accesses
          .filter(a => a.permission === 'OWNER')
          .map(a => a.courseId)
      );
    });
  }

  get userName(): string {
    return this.auth.getUserName();
  }

  toggleForm(): void {
    this.isFormExpanded = !this.isFormExpanded;
  }

  canShare(courseId: number | undefined): boolean {
    if (!courseId) return false;
    return this.auth.isGlobalAdmin() || this.ownedCourseIds.has(courseId);
  }


  // Méthode pour afficher un message de bienvenue
  getWelcomeMessage(): string {
    return 'test';
  }

  // Edit mode methods
  onEditCourse(course: CoursePreview, event: Event) {
    event.preventDefault();
    event.stopPropagation();
    this.editingCourseId = course.id!;
    this.editingCourse = { ...course }; // Create a copy for editing
  }

  onSaveEdit(event: Event) {
    event.preventDefault();
    event.stopPropagation();
    if (this.editingCourse) {
      this.homeService.updateCourse(this.editingCourse).subscribe({
        next: () => {
          this.editingCourseId = null;
          this.editingCourse = null;
          this.refreshCourses();
        },
        error: (err) => {
          console.error('Error updating course:', err);
          alert('Erreur lors de la mise à jour du cours.');
        }
      });
    }
  }

  onCancelEdit(event: Event) {
    event.preventDefault();
    event.stopPropagation();
    this.editingCourseId = null;
    this.editingCourse = null;
  }

  onShareCourse(course: CoursePreview, event: Event): void {
    event.preventDefault();
    event.stopPropagation();

    if (event.target instanceof HTMLElement) {
      event.target.blur();
    }

    this.dialog.open(ShareCourseDialogComponent, {
      width: '620px',
      data: { courseId: course.id, courseName: course.name },
      restoreFocus: true,
      autoFocus: 'first-tabbable'
    });
  }

  onDeleteCourse(course: CoursePreview, event: Event) {
    event.preventDefault();
    event.stopPropagation();

    // Blur the button to remove focus before opening dialog (fixes aria-hidden accessibility warning)
    if (event.target instanceof HTMLElement) {
      event.target.blur();
    }

    const dialogRef = this.dialog.open(DeleteConfirmationDialogComponent, {
      width: '400px',
      data: { courseName: course.name },
      restoreFocus: true, // Properly restore focus after dialog closes
      autoFocus: 'first-tabbable' // Auto-focus first tabbable element in dialog
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result === true && course.id) {
        this.homeService.deleteCourse(course.id).subscribe({
          next: () => {
            this.refreshCourses();
          },
          error: (err) => {
            console.error('Error deleting course:', err);
            alert('Erreur lors de la suppression du cours.');
          }
        });
      }
    });
  }
}
