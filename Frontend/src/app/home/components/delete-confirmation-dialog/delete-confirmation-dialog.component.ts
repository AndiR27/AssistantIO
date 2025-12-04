import { Component, Inject } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { FormsModule } from '@angular/forms';
import { MATERIAL_MODULES } from '../../../shared/imports/material-imports';
import { CommonModule } from '@angular/common';

@Component({
    selector: 'app-delete-confirmation-dialog',
    standalone: true,
    imports: [CommonModule, FormsModule, ...MATERIAL_MODULES],
    template: `
    <h2 mat-dialog-title>Confirmer la suppression</h2>
    <mat-dialog-content>
      <p>Êtes-vous sûr de vouloir supprimer le cours <strong>{{ data.courseName }}</strong> ?</p>
      <p class="warning-text">Cette action est irréversible.</p>
      <mat-form-field appearance="outline" class="full-width">
        <mat-label>Tapez "delete" pour confirmer</mat-label>
        <input matInput [(ngModel)]="confirmText" (keyup.enter)="onConfirm()" />
      </mat-form-field>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button (click)="onCancel()">Annuler</button>
      <button mat-raised-button color="warn" [disabled]="confirmText !== 'delete'" (click)="onConfirm()">
        Supprimer
      </button>
    </mat-dialog-actions>
  `,
    styles: [`
    .warning-text {
      color: #d32f2f;
      font-weight: 500;
      margin-top: 0.5rem;
    }
    .full-width {
      width: 100%;
      margin-top: 1rem;
    }
    mat-dialog-content {
      min-width: 300px;
    }
  `]
})
export class DeleteConfirmationDialogComponent {
    confirmText = '';

    constructor(
        public dialogRef: MatDialogRef<DeleteConfirmationDialogComponent>,
        @Inject(MAT_DIALOG_DATA) public data: { courseName: string }
    ) { }

    onCancel(): void {
        this.dialogRef.close(false);
    }

    onConfirm(): void {
        if (this.confirmText === 'delete') {
            this.dialogRef.close(true);
        }
    }
}
