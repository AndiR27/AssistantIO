package heg.backendspring.exception;

/**
 * Codes d'erreurs métier liés à la gestion des cours.
 */
public enum CourseErrorCode {
    COURSE_NOT_FOUND,
    TP_ALREADY_EXISTS,
    STUDENT_ALREADY_IN_COURSE,
    INVALID_COURSE_CODE,
    OPERATION_NOT_ALLOWED,
    GENERIC_COURSE_ERROR
}
