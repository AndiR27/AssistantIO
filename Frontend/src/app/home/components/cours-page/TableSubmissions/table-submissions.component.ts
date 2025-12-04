import { Component, EventEmitter, Input, Output, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { StudentModel, TP_Model } from '../../../models/courseDetails.model';
import { CourseService } from '../../../services/course.service';

@Component({
    selector: 'app-table-submissions',
    standalone: true,
    imports: [
        CommonModule,
        MatIconModule,
        MatButtonModule,
        MatTooltipModule,
        MatProgressSpinnerModule
    ],
    templateUrl: './table-submissions.component.html',
    styleUrls: ['./table-submissions.component.css']
})
export class TableSubmissionsComponent implements OnInit {
    @Input() courseId: number | undefined;
    @Input() students: StudentModel[] = [];
    @Input() tps: TP_Model[] = [];
    @Output() refresh = new EventEmitter<void>();

    private courseService = inject(CourseService);
    isDownloading = false;

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
        const courseId = this.courseId;
        if (!courseId || !tp.submission?.pathFileStructured) {
            console.warn('Aucun chemin de téléchargement disponible.');
            return;
        }

        this.isDownloading = true;
        this.courseService.downloadStructuredFile(courseId, tp.no).subscribe({
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
    startProcessSubmission(tp: TP_Model) {
        const courseId = this.courseId;
        if (!courseId) return;

        console.log(`Démarrage du process de submission pour le TP ${tp.no} du cours ${courseId}`);
        this.courseService.startProcessSubmission(courseId, tp.no).subscribe({
            next: (updatedTP) => {
                console.log(`Process de submission démarré pour le TP ${tp.no}`, updatedTP);
                this.refresh.emit();
            },
            error: (err) => {
                console.error(`Erreur lors du démarrage du process de submission pour le TP ${tp.no}`, err);
            }
        });
    }

    deleteStudent(student: StudentModel) {
        const courseId = this.courseId;
        const studentId = student.id;
        if (!courseId || !studentId || !confirm(`Êtes-vous sûr de vouloir supprimer l'étudiant ${student.name} ?`)) return;

        this.courseService.deleteStudent(courseId, studentId).subscribe({
            next: () => {
                this.refresh.emit();
            },
            error: (err) => {
                console.error('Erreur lors de la suppression de l\'étudiant:', err);
            }
        });
    }

    deleteTP(tp: TP_Model) {
        const courseId = this.courseId;
        if (!courseId || !confirm(`Êtes-vous sûr de vouloir supprimer le TP ${tp.no} ?`)) return;

        this.courseService.deleteTP(courseId, tp.no).subscribe({
            next: () => {
                this.refresh.emit();
            },
            error: (err) => {
                console.error('Erreur lors de la suppression du TP:', err);
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
}
