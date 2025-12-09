// Type de régime d'études (les valeurs doivent correspondre au backend)
export enum StudyType {
  TEMPS_PLEIN = "TEMPS_PLEIN",
  TEMPS_PARTIEL = "TEMPS_PARTIEL"
}

// Libellés d'affichage pour l'interface utilisateur
export const StudyTypeLabels: Record<StudyType, string> = {
  [StudyType.TEMPS_PLEIN]: "Temps plein",
  [StudyType.TEMPS_PARTIEL]: "Temps partiel"
};

// Récupère le libellé d'affichage d'un type d'études
export function getStudyTypeLabel(type: StudyType): string {
  return StudyTypeLabels[type];
}

// Récupère toutes les options de type d'études pour les menus déroulants
export function getStudyTypeOptions(): Array<{ value: StudyType; label: string }> {
  return Object.values(StudyType).map(type => ({
    value: type,
    label: StudyTypeLabels[type]
  }));
}
