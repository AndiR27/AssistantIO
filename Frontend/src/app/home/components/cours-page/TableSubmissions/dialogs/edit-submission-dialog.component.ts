import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatDialogModule, MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';
import { MatFormFieldModule } from '@angular/material/form-field';
import { StudentSubmissionType, getSubmissionTypeOptions } from '../../../../models/tpStatus.model';

export interface SubmissionDialogData {
    studentName: string;
    tpNo: number;
    currentStatus: StudentSubmissionType | null;
}

@Component({
    selector: 'app-edit-submission-dialog',
    standalone: true,
    imports: [
        CommonModule,
        FormsModule,
        MatDialogModule,
        MatButtonModule,
        MatSelectModule,
        MatFormFieldModule
    ],
    template: `
        <h2 mat-dialog-title>Modifier le statut de rendu</h2>
        <mat-dialog-content>
            <div class="dialog-content">
                <p class="info-text">
                    <strong>Étudiant:</strong> {{ data.studentName }}<br>
                    <strong>TP:</strong> {{ data.tpNo }}
                </p>
                
                <mat-form-field appearance="outline" class="full-width">
                    <mat-label>Statut du rendu</mat-label>
                    <mat-select [(ngModel)]="selectedStatus">
                        @for (option of statusOptions; track option.value) {
                            <mat-option [value]="option.value">
                                <span [style.color]="option.color">{{ option.label }}</span>
                            </mat-option>
                        }
                    </mat-select>
                </mat-form-field>
            </div>
        </mat-dialog-content>
        <mat-dialog-actions align="end">
            <button mat-button (click)="onCancel()">Annuler</button>
            <button mat-raised-button color="primary" (click)="onSave()">
                Enregistrer
            </button>
        </mat-dialog-actions>
    `,
    styles: [`
        .dialog-content {
            padding: 1rem 0;
            min-width: 350px;
        }

        .info-text {
            margin-bottom: 1.5rem;
            color: #4b5563;
            font-size: 0.9375rem;
        }

        .full-width {
            width: 100%;
        }

        ::ng-deep .mat-mdc-option {
            font-weight: 500;
        }
    `]
})
export class EditSubmissionDialogComponent {
    selectedStatus: StudentSubmissionType | null;
    statusOptions = getSubmissionTypeOptions();

    constructor(
        public dialogRef: MatDialogRef<EditSubmissionDialogComponent>,
        @Inject(MAT_DIALOG_DATA) public data: SubmissionDialogData
    ) {
        this.selectedStatus = data.currentStatus;
    }

    onCancel(): void {
        this.dialogRef.close();
    }

    onSave(): void {
        this.dialogRef.close(this.selectedStatus);
    }
}
