import { Injectable } from '@angular/core';
import { ApiService } from '../../shared/services/api.service';
import { Observable, throwError } from 'rxjs';
import { tap, catchError } from 'rxjs/operators';
import { TPStatusModel, StudentSubmissionType } from '../models/tpStatus.model';

/**
 * Service pour gérer les opérations sur les statuts des TPs.
 * Gère les opérations CRUD pour les statuts de rendu des étudiants.
 * Utilise ApiService pour les requêtes HTTP.
 */
@Injectable({ providedIn: 'root' })
export class TPStatusService {

    constructor(private api: ApiService) { }

    // ========================================
    // OPÉRATIONS SUR LES STATUTS DE TP
    // ========================================

    /**
     * Récupère tous les statuts de rendu pour un TP spécifique.
     * GET /course/{courseId}/TPs/{tpNumber}/TPStatus
     * @param courseId - L'identifiant du cours
     * @param tpNumber - Le numéro du TP
     * @returns Observable<TPStatusModel[]>
     */
    getAllTPStatusForTP(courseId: number, tpNumber: number): Observable<TPStatusModel[]> {
        return this.api.getAllTPStatusForTP(courseId, tpNumber).pipe(
            tap(res => console.log(`API getAllTPStatusForTP (Course: ${courseId}, TP: ${tpNumber}) response:`, res)),
            catchError(err => {
                console.error('Error in getAllTPStatusForTP():', err);
                return throwError(() => err);
            })
        );
    }

    /**
     * Récupère les détails d'un statut de TP par son identifiant.
     * GET /TPStatus/{statusId}
     * @param statusId - L'identifiant du statut
     * @returns Observable<TPStatusModel>
     */
    getTPStatusById(statusId: number): Observable<TPStatusModel> {
        return this.api.getTPStatusById(statusId).pipe(
            tap(res => console.log(`API getTPStatusById (${statusId}) response:`, res)),
            catchError(err => {
                console.error('Error in getTPStatusById():', err);
                return throwError(() => err);
            })
        );
    }

    /**
     * Met à jour un statut de TP (change le type de rendu).
     * PUT /TPStatus/{statusId}
     * @param statusId - L'identifiant du statut à modifier
     * @param newType - Le nouveau type de rendu (StudentSubmissionType)
     * @returns Observable<TPStatusModel>
     */
    updateTPStatus(statusId: number, newType: StudentSubmissionType): Observable<TPStatusModel> {
        return this.api.updateTPStatus(statusId, newType).pipe(
            tap(res => console.log(`API updateTPStatus (${statusId}) to ${newType} response:`, res)),
            catchError(err => {
                console.error('Error in updateTPStatus():', err);
                return throwError(() => err);
            })
        );
    }

    /**
     * Supprime un statut de TP.
     * DELETE /TPStatus/{statusId}
     * @param statusId - L'identifiant du statut à supprimer
     * @returns Observable<void>
     */
    deleteTPStatus(statusId: number): Observable<void> {
        return this.api.deleteTPStatus(statusId).pipe(
            tap(() => console.log(`TPStatus ${statusId} deleted successfully`)),
            catchError(err => {
                console.error('Error in deleteTPStatus():', err);
                return throwError(() => err);
            })
        );
    }

    // ========================================
    // MÉTHODES UTILITAIRES
    // ========================================

    /**
     * Met à jour plusieurs statuts de TP en lot.
     * Utile pour les opérations groupées comme marquer plusieurs étudiants comme exemptés.
     * @param updates - Liste des mises à jour à effectuer
     * @returns Observable<TPStatusModel[]>
     */
    batchUpdateStatuses(updates: Array<{ statusId: number; newType: StudentSubmissionType }>): Observable<TPStatusModel[]> {
        const updateObservables = updates.map(update =>
            this.updateTPStatus(update.statusId, update.newType)
        );

        return new Observable(observer => {
            const results: TPStatusModel[] = [];
            let completed = 0;

            updateObservables.forEach((obs, index) => {
                obs.subscribe({
                    next: (result) => {
                        results[index] = result;
                        completed++;
                        if (completed === updateObservables.length) {
                            observer.next(results);
                            observer.complete();
                        }
                    },
                    error: (err) => observer.error(err)
                });
            });
        });
    }
}
