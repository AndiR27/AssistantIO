import { Component, EventEmitter, Input, Output, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { StudentModel, TP_Model } from '../../../models/courseDetails.model';
import { CourseService } from '../../../services/course.service';
import { EditStudentDialogComponent } from './dialogs/edit-student-dialog.component';
import { EditSubmissionDialogComponent } from './dialogs/edit-submission-dialog.component';

@Component({
    selector: 'app-table-submissions',
    standalone: true,
    imports: [
        CommonModule,
        MatIconModule,
        MatButtonModule,
        MatTooltipModule,
        MatProgressSpinnerModule,
        MatDialogModule,
        MatSnackBarModule
    ],
    templateUrl: './table-submissions.component.html',
    styleUrls: ['./table-submissions.component.css']
})
export class TableSubmissionsComponent implements OnInit {
    @Input() courseId?: number; // Optional to match parent signal type
    @Input() students: StudentModel[] = [];
    @Input() tps: TP_Model[] = [];
    @Output() refresh = new EventEmitter<void>();

    private courseService = inject(CourseService);
    private dialog = inject(MatDialog);
    private snackBar = inject(MatSnackBar);
    isDownloading = false;
    processingTPs = new Set<number>(); // Track which TPs are being processed
    processingMapping = new Set<number>(); // Track which TPs are regenerating mapping


    ngOnInit() {
        console.log('TableSubmissionsComponent initialized');
        console.log('Students:', this.students);
        console.log('TPs:', this.tps);
    }

    // Retourne true si le TP et le rendu sont créés et correctement formattés
    shouldShowDownload(tp: TP_Model): boolean {
        if (!tp.submission) {
            return false;
        } else {
            return !(tp.submission.pathFileStructured === null || tp.submission.pathFileStructured === '');
        }
    }

    // Permet de télécharger le rendu restructuré d'un TP
    onDownloadClick(tp: TP_Model) {
        if (!this.courseId || !tp.submission?.pathFileStructured) {
            console.warn('Aucun chemin de téléchargement disponible.');
            return;
        }

        this.isDownloading = true;
        this.courseService.downloadStructuredFile(this.courseId!, tp.no).subscribe({
            next: blob => {
                const name = `TP${tp.no}_RenduRestructuration.zip`;
                const downloadUrl = window.URL.createObjectURL(blob);
                const a = document.createElement('a');
                a.href = downloadUrl;
                a.download = name;
                document.body.appendChild(a);
                a.click();
                document.body.removeChild(a);
                window.URL.revokeObjectURL(downloadUrl);
            },
            error: err => {
                console.error(`Erreur lors du téléchargement du TP ${tp.no}:`, err);
            },
            complete: () => {
                this.isDownloading = false;
            }
        });
    }

    // Permet de démarrer le process de submission pour un TP
    // This makes 2 REST calls: startProcessSubmission + manageTP
    startProcessSubmission(tp: TP_Model) {
        if (!this.courseId) {
            console.error('Course ID is required');
            return;
        }

        if (this.processingTPs.has(tp.no)) {
            console.warn(`TP ${tp.no} is already being processed`);
            return;
        }

        this.processingTPs.add(tp.no);
        console.log(`Démarrage du process de submission pour le TP ${tp.no} du cours ${this.courseId}`);

        // Call endpoints sequentially with a delay to prevent backend race condition
        // First: startProcessSubmission (launches the restructuration)
        this.courseService.startProcessSubmission(this.courseId!, tp.no).subscribe({
            next: () => {
                console.log(`✅ Restructuration du TP ${tp.no} terminée avec succès`);
                // Wait 1 second for backend to finalize before calling manageTP
                setTimeout(() => {
                    // Second: manageTP (updates status for all students)
                    // Note: This may fail due to backend issues, but restructuration is already done
                    this.courseService.manageTP(this.courseId!, tp.no).subscribe({
                        next: () => {
                            console.log(`✅ Gestion du TP ${tp.no} terminée`);
                            this.processingTPs.delete(tp.no);
                            this.refresh.emit();
                        },
                        error: (err) => {
                            console.warn(`⚠️ Restructuration réussie mais erreur lors de la mise à jour des statuts pour le TP ${tp.no}:`, err);
                            // Still refresh UI and remove processing state since restructuration succeeded
                            this.processingTPs.delete(tp.no);
                            this.refresh.emit();
                            this.snackBar.open(
                                `TP ${tp.no}: Restructuration réussie mais mise à jour des statuts échouée. Rechargez la page.`,
                                'OK',
                                { duration: 5000 }
                            );
                        }
                    });
                }, 1000); // 1 second delay to let backend finalize
            },
            error: (err) => {
                console.error(`❌ Erreur lors de la restructuration du TP ${tp.no}:`, err);
                this.processingTPs.delete(tp.no);
                this.snackBar.open(
                    `Erreur lors de la restructuration du TP ${tp.no}`,
                    'Fermer',
                    { duration: 5000 }
                );
            }
        });
    }

    // Check if a TP is currently being processed
    isProcessing(tpNo: number): boolean {
        return this.processingTPs.has(tpNo);
    }

    // Regenerate mapping to check submission status
    regenerateMapping(tp: TP_Model) {
        if (!this.courseId) {
            console.error('Course ID is required');
            return;
        }

        if (this.processingMapping.has(tp.no)) {
            console.warn(`TP ${tp.no} mapping is already being regenerated`);
            return;
        }

        this.processingMapping.add(tp.no);
        console.log(`Régénération du mapping pour le TP ${tp.no}`);

        this.courseService.manageTP(this.courseId!, tp.no).subscribe({
            next: () => {
                console.log(`✅ Mapping régénéré pour le TP ${tp.no}`);
                this.processingMapping.delete(tp.no);
                this.refresh.emit();
                this.snackBar.open(
                    `TP ${tp.no}: Statuts des rendus mis à jour`,
                    'OK',
                    { duration: 3000 }
                );
            },
            error: (err) => {
                console.error(`❌ Erreur lors de la régénération du mapping pour le TP ${tp.no}:`, err);
                this.processingMapping.delete(tp.no);
                this.snackBar.open(
                    `Erreur lors de la mise à jour des statuts pour le TP ${tp.no}`,
                    'Fermer',
                    { duration: 5000 }
                );
            }
        });
    }

    // Check if a TP mapping is being regenerated
    isRegeneratingMapping(tpNo: number): boolean {
        return this.processingMapping.has(tpNo);
    }

    deleteStudent(student: StudentModel) {
        if (!this.courseId || !student.id || !confirm(`Êtes-vous sûr de vouloir supprimer l'étudiant ${student.name} ?`)) {
            return;
        }

        this.courseService.deleteStudent(this.courseId!, student.id).subscribe({
            next: () => {
                this.refresh.emit();
            },
            error: (err) => {
                console.error('Erreur lors de la suppression de l\'étudiant:', err);
            }
        });
    }

    deleteTP(tp: TP_Model) {
        if (!this.courseId || !confirm(`Êtes-vous sûr de vouloir supprimer le TP ${tp.no} ?`)) {
            return;
        }

        this.courseService.deleteTP(this.courseId!, tp.no).subscribe({
            next: () => {
                this.refresh.emit();
            },
            error: (err) => {
                console.error('Erreur lors de la suppression du TP:', err);
            }
        });
    }

    // Edit student details
    editStudent(student: StudentModel) {
        const dialogRef = this.dialog.open(EditStudentDialogComponent, {
            width: '500px',
            data: { ...student }
        });

        dialogRef.afterClosed().subscribe(result => {
            if (result) {
                // Update student via API (to be implemented)
                console.log('Updated student:', result);
                this.snackBar.open('Étudiant mis à jour avec succès', 'Fermer', { duration: 3000 });
                this.refresh.emit();
            }
        });
    }

    // Edit submission status for a student and TP
    editSubmission(student: StudentModel, tp: TP_Model) {
        const currentStatus = this.getStatutForStudent(tp, student.id);
        const dialogRef = this.dialog.open(EditSubmissionDialogComponent, {
            width: '400px',
            data: {
                studentName: student.name,
                tpNo: tp.no,
                currentStatus: currentStatus
            }
        });

        dialogRef.afterClosed().subscribe(result => {
            if (result !== undefined && result !== null) {
                // Update submission status via API (to be implemented)
                console.log('Updated submission status:', result);
                this.snackBar.open('Statut de rendu mis à jour', 'Fermer', { duration: 3000 });
                this.refresh.emit();
            }
        });
    }

    // Permet de récupérer le statut d'un rendu pour un étudiant et un TP donnés
    getStatutForStudent(tp: TP_Model, studentId: number | undefined): boolean | null {
        if (!tp.statusStudents || !studentId) {
            return null;
        }
        const status = tp.statusStudents.find(s => s.studentId === studentId);
        return status?.studentSubmission ?? null;
    }

    formatStatut(statut: boolean | null): boolean {
        if (statut === null) {
            return false;
        }
        return statut;
    }

    // Get TP status: 'ready' | 'processing' | 'needs-update'
    getTPStatus(tp: TP_Model): 'ready' | 'processing' | 'needs-update' {
        if (this.isProcessing(tp.no)) {
            return 'processing';
        }
        if (this.shouldShowDownload(tp)) {
            return 'ready';
        }
        return 'needs-update';
    }

    // Get CSS class for TP status badge
    getTPStatusClass(tp: TP_Model): string {
        const status = this.getTPStatus(tp);
        return `status-${status}`;
    }

    // Get icon for TP status
    getTPStatusIcon(tp: TP_Model): string {
        const status = this.getTPStatus(tp);
        switch (status) {
            case 'ready':
                return 'check';
            case 'processing':
                return 'sync';
            case 'needs-update':
                return 'warning';
            default:
                return 'help';
        }
    }

    // Get label for TP status
    getTPStatusLabel(tp: TP_Model): string {
        const status = this.getTPStatus(tp);
        switch (status) {
            case 'ready':
                return 'Prêt';
            case 'processing':
                return 'En cours...';
            case 'needs-update':
                return 'À restructurer';
            default:
                return 'Inconnu';
        }
    }

    // Get CSS class for submission status
    getSubmissionStatusClass(status: boolean | null): string {
        if (status === true) return 'submitted';
        if (status === false) return 'not-submitted';
        return 'unknown';
    }
}
