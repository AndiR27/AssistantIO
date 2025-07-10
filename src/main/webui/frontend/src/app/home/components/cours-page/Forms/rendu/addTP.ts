import {Component, ElementRef, EventEmitter, inject, Output, ViewChild} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {CourseService} from '../../../../services/course.service';
import {FormBuilder, FormsModule, ReactiveFormsModule, Validators} from '@angular/forms';
import {MATERIAL_MODULES} from '../../../../../shared/imports/material-imports';

@Component({
  selector: 'app-rendu-tp',
  standalone: true,
  imports: [ReactiveFormsModule, MATERIAL_MODULES],
  templateUrl: './addTP.html',
  styleUrl: './addTP.css'
})
export class AddTP {
  @Output() uploaded = new EventEmitter<void>();
  @ViewChild('fileInputRef', { static: false })
  private fileInput!: ElementRef<HTMLInputElement>;


  private route = inject(ActivatedRoute);
  private courseService = inject(CourseService);
  private fb = inject(FormBuilder);

  file?: File;
  isUploading = false;
  errorMsg: string | null = null;
  successMsg: string | null = null;

  form = this.fb.group({
    noTp: ['', Validators.required],
    file: [null as File | null, Validators.required]
  });

  onFileChange(event: Event) {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      this.file = input.files[0];
      this.form.patchValue({ file: this.file });
    }
  }

  onSubmit() {
    this.errorMsg = null;
    if (this.form.invalid || !this.file) {
      this.errorMsg = 'Veuillez renseigner le numéro du TP et choisir un fichier ZIP.';
      return;
    }
    this.isUploading = true;

    const courseId = Number(this.route.snapshot.paramMap.get('id'));
    const tpNo = Number(this.form.value.noTp);

    this.courseService.addTpToCourse(courseId, tpNo, this.file).subscribe({
      next: () => {
        this.isUploading = false;
        this.form.reset();
        this.file = undefined;
        if (this.fileInput) this.fileInput.nativeElement.value = '';
        this.uploaded.emit();
        this.successMsg = "AddTP pour le TP " + tpNo + " ajouté avec succès.";
      },
      error: (err) => {
        this.isUploading = false;
        this.errorMsg = "Erreur lors de l'envoi. Merci de réessayer.";

      }
    });


  }
}

