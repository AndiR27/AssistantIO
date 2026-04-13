package heg.backendspring.enums;

/**
 * Mode de traitement d'une soumission de rendu.
 * <ul>
 *   <li>{@link #CLASSIC} : dossiers nommés d'après le nom de l'étudiant (Cyberlearn classique).</li>
 *   <li>{@link #LOCAL_WS} : dossiers nommés "HEG-WS-XXXX" accompagnés d'un fichier mapping.txt
 *       qui associe "StudentName : HEG-WS-XXXX".</li>
 * </ul>
 */
public enum SubmissionMode {
    CLASSIC,
    LOCAL_WS
}
