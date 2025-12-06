import { Injectable } from '@angular/core';
import { ApiService } from '../../shared/services/api.service';
import { Observable, throwError } from 'rxjs';
import { tap, catchError } from 'rxjs/operators';
import { TPStatusModel, StudentSubmissionType } from '../models/tpStatus.model';

/**
 * Service for managing TPStatus operations.
 * Handles CRUD operations for student submission statuses in TPs.
 * Uses ApiService for HTTP requests.
 */
@Injectable({ providedIn: 'root' })
export class TPStatusService {

    constructor(private api: ApiService) { }

    // ========================================
    // TP STATUS OPERATIONS
    // ========================================

    /**
     * Get all TP statuses for a specific TP
     * GET /course/{courseId}/TPs/{tpNumber}/TPStatus
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
     * Get details of a specific TP status
     * GET /TPStatus/{statusId}
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
     * Update a TP status (change submission type)
     * PUT /TPStatus/{statusId}
     * @param statusId - The ID of the TPStatus to update
     * @param newType - The new StudentSubmissionType
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
     * Delete a TP status
     * DELETE /TPStatus/{statusId}
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
    // UTILITY METHODS
    // ========================================

    /**
     * Batch update multiple TP statuses
     * Useful for bulk operations like marking multiple students as exempt
     */
    batchUpdateStatuses(updates: Array<{ statusId: number; newType: StudentSubmissionType }>): Observable<TPStatusModel[]> {
        const updateObservables = updates.map(update =>
            this.updateTPStatus(update.statusId, update.newType)
        );

        // Note: This returns an array of observables. In real usage, consider using forkJoin
        // to execute them in parallel and wait for all to complete
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
