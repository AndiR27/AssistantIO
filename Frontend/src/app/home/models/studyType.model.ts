// Enum values must match backend exactly
export enum StudyType {
  TEMPS_PLEIN = "TEMPS_PLEIN",
  TEMPS_PARTIEL = "TEMPS_PARTIEL"
}

// Display labels for UI
export const StudyTypeLabels: Record<StudyType, string> = {
  [StudyType.TEMPS_PLEIN]: "Temps plein",
  [StudyType.TEMPS_PARTIEL]: "Temps partiel"
};

// Helper function to get display label
export function getStudyTypeLabel(type: StudyType): string {
  return StudyTypeLabels[type];
}

// Helper function to get all study types with labels for dropdowns
export function getStudyTypeOptions(): Array<{ value: StudyType; label: string }> {
  return Object.values(StudyType).map(type => ({
    value: type,
    label: StudyTypeLabels[type]
  }));
}
