import { Component, EventEmitter, Input, Output, inject, ViewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CdkDragDrop, DragDropModule, moveItemInArray } from '@angular/cdk/drag-drop';
import { MATERIAL_MODULES } from '../../../../shared/imports/material-imports';
import { EvaluationModel } from '../../../models/courseDetails.model';
import { CourseService } from '../../../services/course.service';

@Component({
    selector: 'app-evaluation-section',
    standalone: true,
    imports: [CommonModule, FormsModule, DragDropModule, MATERIAL_MODULES],
    templateUrl: './evaluation-section.html',
    styleUrl: './evaluation-section.css'
})
export class EvaluationSectionComponent {
    private courseService = inject(CourseService);

    @Input() courseId!: number;
    @Input() evaluations: EvaluationModel[] = [];
    @Output() evaluationAdded = new EventEmitter<void>();
    @Output() evaluationUpdated = new EventEmitter<void>();

    // État pour le formulaire d'ajout
    isAddingNew = false;
    newEvaluation: Partial<EvaluationModel> = {
        name: '',
        date: '',
        coefficient: 1,
        mode: 'CLASSIC'
    };

    // État pour l'édition inline
    editingId: number | null = null;
    editData: Partial<EvaluationModel> = {};

    // État pour les soumissions
    uploadingId: number | null = null;
    processingId: number | null = null;
    downloadingId: number | null = null;
    uploadingMappingId: number | null = null;

    // ========================================
    // AJOUT
    // ========================================

    openAddForm(): void {
        this.cancelEdit();
        this.isAddingNew = true;
        this.newEvaluation = {
            name: '',
            date: new Date().toISOString().split('T')[0],
            coefficient: 1,
            mode: 'CLASSIC'
        };
    }

    cancelAdd(): void {
        this.isAddingNew = false;
    }

    saveNewEvaluation(): void {
        if (!this.newEvaluation.name || !this.newEvaluation.date) {
            return;
        }

        const evaluation = {
            name: this.newEvaluation.name,
            date: this.newEvaluation.date,
            coefficient: this.newEvaluation.coefficient || 1,
            courseId: this.courseId,
            mode: this.newEvaluation.mode || 'CLASSIC'
        };

        this.courseService.createEvaluation(this.courseId, evaluation).subscribe({
            next: () => {
                this.isAddingNew = false;
                this.evaluationAdded.emit();
            },
            error: (err) => console.error('Error creating evaluation:', err)
        });
    }

    // ========================================
    // ÉDITION INLINE
    // ========================================

    startEdit(evaluation: EvaluationModel): void {
        this.cancelAdd();
        this.editingId = evaluation.id!;
        this.editData = {
            name: evaluation.name,
            date: evaluation.date,
            coefficient: evaluation.coefficient
        };
    }

    cancelEdit(): void {
        this.editingId = null;
        this.editData = {};
    }

    isEditing(evaluation: EvaluationModel): boolean {
        return this.editingId === evaluation.id;
    }

    saveEdit(evaluation: EvaluationModel): void {
        if (!this.editData.name || !this.editData.date) {
            return;
        }

        const updatedData = {
            id: evaluation.id,
            name: this.editData.name,
            date: this.editData.date,
            coefficient: this.editData.coefficient,
            courseId: this.courseId
        };

        this.courseService.updateEvaluation(evaluation.id!, updatedData).subscribe({
            next: (updated) => {
                const index = this.evaluations.findIndex(e => e.id === evaluation.id);
                if (index !== -1) {
                    this.evaluations[index] = updated;
                }
                this.editingId = null;
                this.editData = {};
                this.evaluationUpdated.emit();
            },
            error: (err) => console.error('Error updating evaluation:', err)
        });
    }

    // ========================================
    // DRAG & DROP
    // ========================================

    onDrop(event: CdkDragDrop<EvaluationModel[]>): void {
        if (event.previousIndex === event.currentIndex) {
            return;
        }
        moveItemInArray(this.evaluations, event.previousIndex, event.currentIndex);
    }

    // ========================================
    // SOUMISSIONS
    // ========================================

    onFileSelected(event: Event, evaluation: EvaluationModel): void {
        const input = event.target as HTMLInputElement;
        if (!input.files || input.files.length === 0) {
            return;
        }

        const file = input.files[0];
        this.uploadingId = evaluation.id!;

        this.courseService.uploadEvaluationSubmission(evaluation.id!, file).subscribe({
            next: (updated) => {
                const index = this.evaluations.findIndex(e => e.id === evaluation.id);
                if (index !== -1) {
                    this.evaluations[index] = updated;
                }
                this.uploadingId = null;
            },
            error: (err) => {
                console.error('Error uploading submission:', err);
                this.uploadingId = null;
            }
        });

        // Reset input so the same file can be re-selected
        input.value = '';
    }

    processSubmission(evaluation: EvaluationModel): void {
        this.processingId = evaluation.id!;

        this.courseService.processEvaluationSubmission(evaluation.id!).subscribe({
            next: () => {
                this.processingId = null;
                // Reload to get updated submission state
                this.evaluationUpdated.emit();
            },
            error: (err) => {
                console.error('Error processing submission:', err);
                this.processingId = null;
            }
        });
    }

    downloadRestructured(evaluation: EvaluationModel): void {
        this.downloadingId = evaluation.id!;

        this.courseService.downloadEvaluationRestructured(evaluation.id!).subscribe({
            next: (blob) => {
                // Trigger browser download
                const url = window.URL.createObjectURL(blob);
                const a = document.createElement('a');
                a.href = url;
                a.download = `${evaluation.name}_restructured.zip`;
                a.click();
                window.URL.revokeObjectURL(url);
                this.downloadingId = null;
            },
            error: (err) => {
                console.error('Error downloading restructured ZIP:', err);
                this.downloadingId = null;
            }
        });
    }

    isUploading(evaluation: EvaluationModel): boolean {
        return this.uploadingId === evaluation.id;
    }

    isProcessing(evaluation: EvaluationModel): boolean {
        return this.processingId === evaluation.id;
    }

    isDownloading(evaluation: EvaluationModel): boolean {
        return this.downloadingId === evaluation.id;
    }

    isUploadingMapping(evaluation: EvaluationModel): boolean {
        return this.uploadingMappingId === evaluation.id;
    }

    isLocalWsMode(evaluation: EvaluationModel): boolean {
        return evaluation.mode === 'LOCAL_WS';
    }

    onMappingFileSelected(event: Event, evaluation: EvaluationModel): void {
        const input = event.target as HTMLInputElement;
        if (!input.files || input.files.length === 0) {
            return;
        }

        const file = input.files[0];
        this.uploadingMappingId = evaluation.id!;

        this.courseService.uploadEvaluationMapping(evaluation.id!, file).subscribe({
            next: (updated) => {
                const index = this.evaluations.findIndex(e => e.id === evaluation.id);
                if (index !== -1) {
                    this.evaluations[index] = updated;
                }
                this.uploadingMappingId = null;
                this.evaluationUpdated.emit();
            },
            error: (err) => {
                console.error('Error uploading mapping file:', err);
                this.uploadingMappingId = null;
            }
        });

        input.value = '';
    }
}
