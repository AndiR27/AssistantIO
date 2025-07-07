import {Component, ElementRef, EventEmitter, inject, Output, signal, ViewChild} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {CourseService} from '../../../../services/course.service';
import {FormsModule} from '@angular/forms';
import {MATERIAL_MODULES} from '../../../../../shared/imports/material-imports';

@Component({
  selector: 'app-upload-file-txt',
  imports: [
    FormsModule,
    MATERIAL_MODULES
  ],
  templateUrl: './upload-file-txt.html',
  styleUrl: './upload-file-txt.css'
})
export class UploadFileTxt {
  @Output() uploaded = new EventEmitter<void>();
  @ViewChild('fileInputRef', { static: false })
  private fileInput!: ElementRef<HTMLInputElement>;

  private route     = inject(ActivatedRoute);
  private courseSvc = inject(CourseService);

  file?: File;
  isUploading = false;
  addedCount: number | null = null;
  /** Nombre de lignes non vides comptées dans le TXT */
  lineCount = signal<number|null>(null);

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (!input.files?.length) {
      this.file = undefined;
      this.lineCount.set(null);
      return;
    }
    this.file = input.files[0];
    // Lire le fichier et compter les lignes non‐vides
    const reader = new FileReader();
    reader.onload = () => {
      const text = reader.result as string;
      const lines = text
        .split(/\r?\n/)
        .filter(line => line.trim().length > 0);
      this.lineCount.set(lines.length);
    };
    reader.readAsText(this.file);
  }

  clearFileInput(): void {
    this.file = undefined;
    this.lineCount.set(null);
    this.fileInput.nativeElement.value = '';
  }

  onSubmit(): void {
    if (!this.file) { return; }
    this.isUploading  = true;
    const courseId = Number(this.route.snapshot.paramMap.get('id'));

    this.courseSvc.addStudentsFromFile(courseId, this.file)
      .subscribe({
        next: () => {
          console.log(`Importé ${this.lineCount()} ligne(s).`);
          this.clearFileInput();
          this.uploaded.emit();
          // masquer le message après 5s
          setTimeout(() => this.lineCount.set(null), 5000);
        },
        error: err => console.error('Erreur import TXT :', err),
        complete: () => this.isUploading = false
      });
  }
}
