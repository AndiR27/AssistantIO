import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatDialogModule, MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { StudentModel } from '../../../../models/courseDetails.model';
import { StudyType } from '../../../../models/studyType.model';

@Component({
    selector: 'app-edit-student-dialog',
    standalone: true,
    imports: [
        CommonModule,
        FormsModule,
        MatDialogModule,
        MatButtonModule,
        MatFormFieldModule,
        MatInputModule,
        MatSelectModule
    ],
    template: `
        <h2 mat-dialog-title>Modifier l'étudiant</h2>
        <mat-dialog-content>
            <div class="dialog-form">
                <mat-form-field appearance="outline">
                    <mat-label>Nom</mat-label>
                    <input matInput [(ngModel)]="data.name" required>
                </mat-form-field>

                <mat-form-field appearance="outline">
                    <mat-label>Email</mat-label>
                    <input matInput type="email" [(ngModel)]="data.email" required>
                </mat-form-field>

                <mat-form-field appearance="outline">
                    <mat-label>Type d'étude</mat-label>
                    <mat-select [(ngModel)]="data.studyType">
                        <mat-option value="CLASSIC">Classique</mat-option>
                        <mat-option value="APPRENTICE">Apprenti</mat-option>
                    </mat-select>
                </mat-form-field>
            </div>
        </mat-dialog-content>
        <mat-dialog-actions align="end">
            <button mat-button (click)="onCancel()">Annuler</button>
            <button mat-raised-button color="primary" (click)="onSave()" 
                [disabled]="!data.name || !data.email">
                Enregistrer
            </button>
        </mat-dialog-actions>
    `,
    styles: [`
        .dialog-form {
            display: flex;
            flex-direction: column;
            gap: 1rem;
            min-width: 400px;
            padding: 1rem 0;
        }

        mat-form-field {
            width: 100%;
        }
    `]
})
export class EditStudentDialogComponent {
    constructor(
        public dialogRef: MatDialogRef<EditStudentDialogComponent>,
        @Inject(MAT_DIALOG_DATA) public data: StudentModel
    ) { }

    onCancel(): void {
        this.dialogRef.close();
    }

    onSave(): void {
        this.dialogRef.close(this.data);
    }
}
