/**
 * Enum for Student Submission Types
 * Must match backend enum exactly
 */
export enum StudentSubmissionType {
    DONE = 'DONE',
    DONE_LATE = 'DONE_LATE',
    DONE_GOOD = 'DONE_GOOD',
    DONE_BUT_NOTHING = 'DONE_BUT_NOTHING',
    DONE_BUT_MEDIOCRE = 'DONE_BUT_MEDIOCRE',
    NOT_DONE_MISSING = 'NOT_DONE_MISSING',
    EXEMPT = 'EXEMPT'
}

/**
 * Display labels for UI (French)
 */
export const StudentSubmissionTypeLabels: Record<StudentSubmissionType, string> = {
    [StudentSubmissionType.DONE]: 'Rendu',
    [StudentSubmissionType.DONE_LATE]: 'Rendu en retard',
    [StudentSubmissionType.DONE_GOOD]: 'Bon rendu',
    [StudentSubmissionType.DONE_BUT_NOTHING]: 'Rendu vide',
    [StudentSubmissionType.DONE_BUT_MEDIOCRE]: 'Rendu médiocre',
    [StudentSubmissionType.NOT_DONE_MISSING]: 'Non rendu',
    [StudentSubmissionType.EXEMPT]: 'Exempté'
};

/**
 * Color codes for visual distinction in UI
 */
export const StudentSubmissionTypeColors: Record<StudentSubmissionType, string> = {
    [StudentSubmissionType.DONE]: '#4CAF50',              // Green
    [StudentSubmissionType.DONE_LATE]: '#FF9800',          // Orange
    [StudentSubmissionType.DONE_GOOD]: '#2196F3',          // Blue
    [StudentSubmissionType.DONE_BUT_NOTHING]: '#9E9E9E',   // Grey
    [StudentSubmissionType.DONE_BUT_MEDIOCRE]: '#FFC107',  // Amber
    [StudentSubmissionType.NOT_DONE_MISSING]: '#F44336',   // Red
    [StudentSubmissionType.EXEMPT]: '#9C27B0'              // Purple
};

/**
 * TPStatus Model - Represents the status of a student for a specific TP
 */
export interface TPStatusModel {
    id?: number;
    studentId: number;
    tpId: number;
    studentSubmission: StudentSubmissionType;
}

/**
 * Helper function to get display label
 */
export function getSubmissionTypeLabel(type: StudentSubmissionType): string {
    return StudentSubmissionTypeLabels[type];
}

/**
 * Helper function to get submission type color
 */
export function getSubmissionTypeColor(type: StudentSubmissionType): string {
    return StudentSubmissionTypeColors[type];
}

/**
 * Helper function to get all submission types for dropdowns
 */
export function getSubmissionTypeOptions(): Array<{ value: StudentSubmissionType; label: string; color: string }> {
    return Object.values(StudentSubmissionType).map(type => ({
        value: type,
        label: StudentSubmissionTypeLabels[type],
        color: StudentSubmissionTypeColors[type]
    }));
}
