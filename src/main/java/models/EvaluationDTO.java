package models;

import java.time.LocalDateTime;

public class EvaluationDTO {
    private Long id;
    private String name;
    private LocalDateTime date;
    private CourseDTO course;
    private SubmissionDTO submission;

    // Constructeurs
    public EvaluationDTO() {}

    public EvaluationDTO(Long id, String name, LocalDateTime date, CourseDTO course, SubmissionDTO submission) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.course = course;
        this.submission = submission;
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }

    public CourseDTO getCourse() { return course; }
    public void setCourse(CourseDTO course) { this.course = course; }

    public SubmissionDTO getSubmission() { return submission; }
    public void setSubmission(SubmissionDTO submission) { this.submission = submission; }
}
