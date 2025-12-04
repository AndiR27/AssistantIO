import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatDialogModule, MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatRadioModule } from '@angular/material/radio';

export interface SubmissionDialogData {
    studentName: string;
    tpNo: number;
    currentStatus: boolean | null;
}

@Component({
    selector: 'app-edit-submission-dialog',
    standalone: true,
    imports: [
        CommonModule,
        FormsModule,
        MatDialogModule,
        MatButtonModule,
        MatRadioModule
    ],
    template: `
        <h2 mat-dialog-title>Modifier le statut de rendu</h2>
        <mat-dialog-content>
            <div class="dialog-content">
                <p class="info-text">
                    <strong>Étudiant:</strong> {{ data.studentName }}<br>
                    <strong>TP:</strong> {{ data.tpNo }}
                </p>
                
                <mat-radio-group [(ngModel)]="selectedStatus" class="status-options">
                    <mat-radio-button [value]="true">Rendu</mat-radio-button>
                    <mat-radio-button [value]="false">Non rendu</mat-radio-button>
                    <mat-radio-button [value]="null">Non applicable</mat-radio-button>
                </mat-radio-group>
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
            min-width: 300px;
        }

        .info-text {
            margin-bottom: 1.5rem;
            color: #4b5563;
        }

        .status-options {
            display: flex;
            flex-direction: column;
            gap: 0.75rem;
        }

        mat-radio-button {
            margin: 0.25rem 0;
        }
    `]
})
export class EditSubmissionDialogComponent {
    selectedStatus: boolean | null;

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
