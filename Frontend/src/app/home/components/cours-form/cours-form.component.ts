import { Component, EventEmitter, inject, Output } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';
import { HomeService } from '../../services/home.service';
import { CourseType } from '../../models/courseType.model';
import { SemesterType } from '../../models/semesterType.model';
import { CoursePreview } from '../../models/coursePreview.model';
import { MatButton } from '@angular/material/button';
import { MatFormField, MatLabel, MatError } from '@angular/material/form-field';
import { MatInput } from '@angular/material/input';
import { MatSelect } from '@angular/material/select';
import { MatOption } from '@angular/material/core';
import { MatIcon } from '@angular/material/icon';

@Component({
  selector: 'app-cours-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatButton,
    MatFormField,
    MatLabel,
    MatInput,
    MatSelect,
    MatOption,
    MatIcon,
    MatError
  ],
  templateUrl: './cours-form.component.html',
  styleUrls: ['./cours-form.component.css']
})

export class CoursFormComponent {
  // FormGroup pour gérer le formulaire réactif
  private fb = inject(FormBuilder);
  private homeService = inject(HomeService);
  @Output() courseAdded = new EventEmitter<void>();
  // utilisation de FormBuilder pour créer un formulaire réactif
  // Options extraites directement des enums
  semesterOptions = Object.values(SemesterType);
  courseTypeOptions = Object.values(CourseType);
  form = this.fb.group({
    name: ['', Validators.required],
    code: ['', Validators.required],
    semester: [this.semesterOptions[0], Validators.required],
    year_course: [new Date().getFullYear(), [Validators.required, Validators.min(2000)]],
    teacher: ['', Validators.required],
    courseType: [this.courseTypeOptions[0], Validators.required]
  });

  constructor() { }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    // form.value a la forme exacte de CoursePreview sans id
    const payload = this.form.value as Omit<CoursePreview, 'id'>;
    this.homeService.addCourse(payload).subscribe(() => {
      // Reset aux valeurs par défaut
      this.form.reset({
        name: '',
        code: '',
        semester: this.semesterOptions[0],
        year_course: new Date().getFullYear(),
        teacher: '',
        courseType: this.courseTypeOptions[0]
      });
      console.log('Course added successfully:', payload);
      // Émettre l'événement pour notifier le composant parent
      this.courseAdded.emit();
    });

  }

  refreshPage() {
    // Rafraîchit la page actuelle
    window.location.reload();
  }




}
