import { Component, EventEmitter, Input, Output, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { StudentModel, TP_Model, TPStatusModel, CourseDetailsModel } from '../../../models/courseDetails.model';
import { CourseService } from '../../../services/course.service';
import { TPStatusService } from '../../../services/tp-status.service';
import { StudentSubmissionType } from '../../../models/tpStatus.model';
import { EditStudentDialogComponent } from './dialogs/edit-student-dialog.component';
import { EditSubmissionDialogComponent } from './dialogs/edit-submission-dialog.component';
import { ExportPdfComponent } from './export-pdf.component';

@Component({
    selector: 'app-table-submissions',
    standalone: true,
    imports: [
        CommonModule,
        FormsModule,
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
    @Input() course?: CourseDetailsModel; // Course details for PDF export
    @Input() students: StudentModel[] = [];
    @Input() tps: TP_Model[] = [];
    @Output() refresh = new EventEmitter<void>();

    private courseService = inject(CourseService);
    private tpStatusService = inject(TPStatusService);
    private dialog = inject(MatDialog);
    private snackBar = inject(MatSnackBar);
    private exportPdfService = inject(ExportPdfComponent);
    isDownloading = false;
    processingTPs = new Set<number>(); // Track which TPs are being processed
    processingMapping = new Set<number>(); // Track which TPs are regenerating mapping
    searchTerm = ''; // Search term for filtering students


    ngOnInit() {
        console.log('TableSubmissionsComponent initialized');
        console.log('Students:', this.students);
        console.log('TPs:', this.tps);
    }

    // Getter that returns students sorted alphabetically by name and filtered by search term
    get sortedStudents(): StudentModel[] {
        let filtered = [...this.students];

        // Filter by search term if present
        if (this.searchTerm.trim()) {
            const searchLower = this.searchTerm.toLowerCase().trim();
            filtered = filtered.filter(s =>
                s.name?.toLowerCase().includes(searchLower)
            );
        }

        // Sort alphabetically
        return filtered.sort((a, b) => {
            const nameA = a.name?.toLowerCase() || '';
            const nameB = b.name?.toLowerCase() || '';
            return nameA.localeCompare(nameB);
        });
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
        console.log(`Rafraîchissement des statuts pour le TP ${tp.no}`);

        // Use the new refresh endpoint that checks submissions without recreating TPStatus
        this.courseService.refreshTPStatusList(this.courseId!, tp.no).subscribe({
            next: (updatedStatuses) => {
                console.log(`✅ Statuts rafraîchis pour le TP ${tp.no}:`, updatedStatuses);
                this.processingMapping.delete(tp.no);
                this.refresh.emit();
                this.snackBar.open(
                    `TP ${tp.no}: Statuts des rendus rafraîchis`,
                    'OK',
                    { duration: 3000 }
                );
            },
            error: (err) => {
                console.error(`❌ Erreur lors du rafraîchissement des statuts pour le TP ${tp.no}:`, err);
                this.processingMapping.delete(tp.no);
                this.snackBar.open(
                    `Erreur lors du rafraîchissement des statuts pour le TP ${tp.no}`,
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
            if (result && this.courseId && student.id) {
                // Update student via API
                this.courseService.updateStudent(this.courseId!, student.id, result).subscribe({
                    next: (updatedStudent) => {
                        console.log('Student updated:', updatedStudent);
                        this.snackBar.open('Étudiant mis à jour avec succès', 'Fermer', { duration: 3000 });
                        this.refresh.emit();
                    },
                    error: (err) => {
                        console.error('Error updating student:', err);
                        this.snackBar.open('Erreur lors de la mise à jour de l\'étudiant', 'Fermer', { duration: 5000 });
                    }
                });
            }
        });
    }

    // Edit submission status for a student and TP
    editSubmission(student: StudentModel, tp: TP_Model) {
        const currentStatus = this.getTPStatusForStudent(tp, student.id);
        const dialogRef = this.dialog.open(EditSubmissionDialogComponent, {
            width: '400px',
            data: {
                studentName: student.name,
                tpNo: tp.no,
                currentStatus: this.convertToValidSubmissionType(currentStatus?.studentSubmission)
            }
        });

        dialogRef.afterClosed().subscribe(result => {
            if (result !== undefined && result !== null && currentStatus?.id) {
                // Validate and convert result to proper enum type
                const validatedType = this.convertToValidSubmissionType(result);

                if (!validatedType) {
                    this.snackBar.open('Type de statut invalide', 'Fermer', { duration: 3000 });
                    return;
                }

                // Update submission status via API with validated enum value
                this.tpStatusService.updateTPStatus(currentStatus.id, validatedType).subscribe({
                    next: (updatedStatus) => {
                        console.log('Submission status updated:', updatedStatus);
                        this.snackBar.open('Statut de rendu mis à jour', 'Fermer', { duration: 3000 });
                        this.refresh.emit();
                    },
                    error: (err) => {
                        console.error('Error updating submission status:', err);
                        this.snackBar.open('Erreur lors de la mise à jour du statut', 'Fermer', { duration: 5000 });
                    }
                });
            }
        });
    }

    // Helper: Convert any value to a valid StudentSubmissionType or null
    // This handles old boolean data from backend
    private convertToValidSubmissionType(value: any): StudentSubmissionType | null {
        if (!value) return null;

        // If it's already a valid enum value, return it
        if (Object.values(StudentSubmissionType).includes(value as StudentSubmissionType)) {
            return value as StudentSubmissionType;
        }

        // Handle boolean to enum conversion for old data
        if (typeof value === 'boolean') {
            return value ? StudentSubmissionType.DONE : StudentSubmissionType.NOT_DONE_MISSING;
        }

        // Handle string boolean values
        if (value === 'true') return StudentSubmissionType.DONE;
        if (value === 'false') return StudentSubmissionType.NOT_DONE_MISSING;

        console.warn('Invalid submission type value:', value);
        return null;
    }

    // Permet de récupérer le statut d'un rendu pour un étudiant et un TP donnés
    getStatutForStudent(tp: TP_Model, studentId: number | undefined): StudentSubmissionType | null {
        if (!tp.statusStudents || !studentId) {
            return null;
        }
        const status = tp.statusStudents.find(s => s.studentId === studentId);
        // Sanitize the data to ensure it's a valid enum value
        return this.convertToValidSubmissionType(status?.studentSubmission);
    }

    // Get TPStatus object for a student (for accessing id and other fields)
    getTPStatusForStudent(tp: TP_Model, studentId: number | undefined): TPStatusModel | null {
        if (!tp.statusStudents || !studentId) {
            return null;
        }
        return tp.statusStudents.find(s => s.studentId === studentId) ?? null;
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
                return 'En attente';
            default:
                return 'Inconnu';
        }
    }

    // Get CSS class for submission status based on StudentSubmissionType
    getSubmissionStatusClass(status: StudentSubmissionType | null): string {
        if (!status) return 'unknown';

        switch (status) {
            case StudentSubmissionType.DONE:
            case StudentSubmissionType.DONE_LATE:
            case StudentSubmissionType.DONE_GOOD:
                return 'submitted';
            case StudentSubmissionType.DONE_BUT_NOTHING:
            case StudentSubmissionType.DONE_BUT_MEDIOCRE:
                return 'submitted-partial';
            case StudentSubmissionType.NOT_DONE_MISSING:
                return 'not-submitted';
            case StudentSubmissionType.EXEMPT:
                return 'exempt';
            default:
                return 'unknown';
        }
    }

    // Get display label for submission status
    getSubmissionStatusLabel(status: StudentSubmissionType | null): string {
        if (!status) return 'N/A';

        switch (status) {
            case StudentSubmissionType.DONE:
                return 'Rendu';
            case StudentSubmissionType.DONE_LATE:
                return 'Rendu en retard';
            case StudentSubmissionType.DONE_GOOD:
                return 'Bon rendu';
            case StudentSubmissionType.DONE_BUT_NOTHING:
                return 'Rendu vide';
            case StudentSubmissionType.DONE_BUT_MEDIOCRE:
                return 'Rendu médiocre';
            case StudentSubmissionType.NOT_DONE_MISSING:
                return 'Non rendu';
            case StudentSubmissionType.EXEMPT:
                return 'Exempté';
            default:
                return 'Inconnu';
        }
    }

    // Get icon for submission status
    getSubmissionStatusIcon(status: StudentSubmissionType | null): string {
        if (!status) return 'remove';

        switch (status) {
            case StudentSubmissionType.DONE:
                return 'check';
            case StudentSubmissionType.DONE_LATE:
                return 'schedule';
            case StudentSubmissionType.DONE_GOOD:
                return 'star';
            case StudentSubmissionType.DONE_BUT_NOTHING:
                return 'warning';
            case StudentSubmissionType.DONE_BUT_MEDIOCRE:
                return 'info';
            case StudentSubmissionType.NOT_DONE_MISSING:
                return 'close';
            case StudentSubmissionType.EXEMPT:
                return 'block';
            default:
                return 'help';
        }
    }

    // Export table data to PDF
    exportToPDF() {
        // Prompt user for threshold percentage
        const thresholdInput = prompt('Entrez le seuil de réussite (%) pour le calcul:', '75');

        if (thresholdInput === null) {
            // User cancelled
            return;
        }

        const threshold = parseFloat(thresholdInput);

        if (isNaN(threshold) || threshold < 0 || threshold > 100) {
            this.snackBar.open('Veuillez entrer un pourcentage valide (0-100)', 'Fermer', { duration: 3000 });
            return;
        }

        try {
            this.exportPdfService.generatePDF(this.students, this.tps, threshold, this.course);
            this.snackBar.open('PDF généré avec succès', 'Fermer', { duration: 3000 });
        } catch (error) {
            console.error('Error generating PDF:', error);
            this.snackBar.open('Erreur lors de la génération du PDF', 'Fermer', { duration: 5000 });
        }
    }
}
