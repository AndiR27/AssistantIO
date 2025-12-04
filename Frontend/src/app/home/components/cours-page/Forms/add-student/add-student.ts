import { Component, computed, EventEmitter, inject, Input, Output, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { CourseService } from '../../../../services/course.service';
import { StudyType } from '../../../../models/studyType.model';
import { StudentModel } from '../../../../models/courseDetails.model';
import { ActivatedRoute } from '@angular/router';
import { CommonModule } from '@angular/common';
import { MatCard } from '@angular/material/card';
import { MatFormField } from '@angular/material/input';
import { MATERIAL_MODULES } from '../../../../../shared/imports/material-imports';


@Component({
  selector: 'app-add-student',
  standalone: true,
  imports: [
    ReactiveFormsModule, CommonModule, MATERIAL_MODULES
  ],
  templateUrl: './add-student.html',
  styleUrl: './add-student.css'
})
export class AddStudent {
  //Form pour ajouter un étudiant à un cours
  @Input() courseId!: number;
  @Output() added = new EventEmitter<void>();
  private fb = inject(FormBuilder);
  private courseService = inject(CourseService);
  private route = inject(ActivatedRoute);

  studyOptions = Object.values(StudyType)
  readonly studyTypes = signal(Object.values(StudyType));
  readonly isSubmitting = signal(false);
  readonly addedStudent = signal<StudentModel | null>(null);

  readonly form = this.fb.group({
    name: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
    studyType: [this.studyOptions[0], Validators.required],
  });

  readonly canSubmit = computed(() =>
    this.form.valid && !this.isSubmitting()
  );


  onSubmit(): void {
    // log
    console.log('Debut de l\'ajout d\'un étudiant');
    // Vérification de la validité du formulaire avant de soumettre
    if (!this.form.valid) {
      this.form.markAllAsTouched();
      return;
    }



    this.isSubmitting.set(true);
    const courseId = Number(this.route.snapshot.paramMap.get('id'));
    const payload = this.form.value as StudentModel;

    this.courseService
      .addStudentToCourse(courseId, payload)
      .subscribe({
        next: student => {
          //log

          this.addedStudent.set(student);
          this.added.emit();
          console.log('Emit Etudiant avec succès:', student);
          this.form.reset({ studyType: StudyType.TEMPS_PLEIN })
        },
        error: err => {
          console.error('Error adding student:', err);
          this.addedStudent.set(null);
        },
        complete: () => {
          this.isSubmitting.set(false);
        }
      },
      )

  }


}
