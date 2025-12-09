// Type de rendu étudiant (les valeurs doivent correspondre au backend)
export enum StudentSubmissionType {
    DONE = 'DONE',
    DONE_LATE = 'DONE_LATE',
    DONE_GOOD = 'DONE_GOOD',
    DONE_BUT_NOTHING = 'DONE_BUT_NOTHING',
    DONE_BUT_MEDIOCRE = 'DONE_BUT_MEDIOCRE',
    NOT_DONE_MISSING = 'NOT_DONE_MISSING',
    EXEMPT = 'EXEMPT'
}

// Libellés d'affichage pour l'interface utilisateur
export const StudentSubmissionTypeLabels: Record<StudentSubmissionType, string> = {
    [StudentSubmissionType.DONE]: 'Rendu',
    [StudentSubmissionType.DONE_LATE]: 'Rendu en retard',
    [StudentSubmissionType.DONE_GOOD]: 'Bon rendu',
    [StudentSubmissionType.DONE_BUT_NOTHING]: 'Rendu vide',
    [StudentSubmissionType.DONE_BUT_MEDIOCRE]: 'Rendu médiocre',
    [StudentSubmissionType.NOT_DONE_MISSING]: 'Non rendu',
    [StudentSubmissionType.EXEMPT]: 'Exempté'
};

// Codes couleurs pour la distinction visuelle dans l'interface
export const StudentSubmissionTypeColors: Record<StudentSubmissionType, string> = {
    [StudentSubmissionType.DONE]: '#4CAF50',              // Vert
    [StudentSubmissionType.DONE_LATE]: '#FF9800',          // Orange
    [StudentSubmissionType.DONE_GOOD]: '#2196F3',          // Bleu
    [StudentSubmissionType.DONE_BUT_NOTHING]: '#9E9E9E',   // Gris
    [StudentSubmissionType.DONE_BUT_MEDIOCRE]: '#FFC107',  // Ambre
    [StudentSubmissionType.NOT_DONE_MISSING]: '#F44336',   // Rouge
    [StudentSubmissionType.EXEMPT]: '#9C27B0'              // Violet
};

// Modèle de statut TP - Représente le statut d'un étudiant pour un TP spécifique
export interface TPStatusModel {
    id?: number;
    studentId: number;
    tpId: number;
    studentSubmission: StudentSubmissionType;
}

// Récupère le libellé d'affichage d'un type de rendu
export function getSubmissionTypeLabel(type: StudentSubmissionType): string {
    return StudentSubmissionTypeLabels[type];
}

// Récupère la couleur associée à un type de rendu
export function getSubmissionTypeColor(type: StudentSubmissionType): string {
    return StudentSubmissionTypeColors[type];
}

// Récupère toutes les options de type de rendu pour les menus déroulants
export function getSubmissionTypeOptions(): Array<{ value: StudentSubmissionType; label: string; color: string }> {
    return Object.values(StudentSubmissionType).map(type => ({
        value: type,
        label: StudentSubmissionTypeLabels[type],
        color: StudentSubmissionTypeColors[type]
    }));
}
