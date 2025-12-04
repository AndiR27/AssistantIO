package heg.backendspring.exception;

public class CourseException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final CourseErrorCode errorCode;
    private final Long courseId;

    /**
     * Exception générique sans information supplémentaire.
     */
    public CourseException(String message) {
        this(message, CourseErrorCode.GENERIC_COURSE_ERROR, null, null);
    }

    /**
     * Exception générique avec cause.
     */
    public CourseException(String message, Throwable cause) {
        this(message, CourseErrorCode.GENERIC_COURSE_ERROR, null, cause);
    }

    /**
     * Exception avec code d'erreur métier.
     */
    public CourseException(String message, CourseErrorCode errorCode) {
        this(message, errorCode, null, null);
    }

    /**
     * Exception avec code d'erreur métier et identifiant de cours.
     */
    public CourseException(String message, CourseErrorCode errorCode, Long courseId) {
        this(message, errorCode, courseId, null);
    }

    /**
     * Constructeur principal.
     */
    public CourseException(String message,
                           CourseErrorCode errorCode,
                           Long courseId,
                           Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode != null ? errorCode : CourseErrorCode.GENERIC_COURSE_ERROR;
        this.courseId = courseId;
    }

    public CourseErrorCode getErrorCode() {
        return errorCode;
    }

    public Long getCourseId() {
        return courseId;
    }

}
