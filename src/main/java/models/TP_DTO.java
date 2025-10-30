package models;
import java.util.Set;

/**
 * Classe POJO représentant un Travail Pratique sans exposer l'entité JPA.
 */
public class TP_DTO {

    private Long id;
    private int no;
    private CourseDTO course;
    private SubmissionDTO submission;
    private Set<TPStatusDTO> statusStudents;

    // Constructeurs
    public TP_DTO() {}

    public TP_DTO(Long id, int no, CourseDTO course, SubmissionDTO submission, Set<TPStatusDTO> statusStudents) {
        this.id = id;
        this.no = no;
        this.course = course;
        this.submission = submission;
        this.statusStudents = statusStudents;
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public int getNo() { return no; }
    public void setNo(int no) { this.no = no; }

    public CourseDTO getCourse() { return course; }
    public void setCourse(CourseDTO course) { this.course = course; }

    public SubmissionDTO getSubmission() { return submission; }
    public void setSubmission(SubmissionDTO submission) { this.submission = submission; }

    public Set<TPStatusDTO> getStatusStudents() { return statusStudents; }
    public void setStatusStudents(Set<TPStatusDTO> statusStudents) { this.statusStudents = statusStudents; }
}
