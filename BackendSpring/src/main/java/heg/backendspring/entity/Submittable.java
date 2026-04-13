package heg.backendspring.entity;

/**
 * Interface commune pour les entités pouvant avoir une Submission (TP, Evaluation).
 * Permet de factoriser la logique de gestion des soumissions.
 */
public interface Submittable {

    Long getId();

    /**
     * Retourne le nom utilisé pour nommer les fichiers (ex: "TP1", "CC1").
     */
    String getSubmissionName();

    /**
     * Retourne le nom du dossier de stockage (ex: "TP1", "Examen_Final").
     */
    String getFolderName();

    Course getCourse();

    Submission getSubmission();

    void setSubmission(Submission submission);
}
