package models;

public class TPStatusDTO {
    private Long id;
    public Long studentId;          // ← just the ID
    public Long tpId;               // ← just the ID
    private boolean studentSubmission;

    // Constructeurs
    public TPStatusDTO() {}

    public TPStatusDTO(Long id, Long studentId, Long tpId, boolean studentSubmission) {
        this.id = id;
        this.studentId = studentId;
        this.tpId = tpId;
        this.studentSubmission = studentSubmission;
    }

    // Getters et Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public Long getTpId() {
        return tpId;
    }

    public void setTpId(Long tpId) {
        this.tpId = tpId;
    }

    public boolean isStudentSubmission() {
        return studentSubmission;
    }

    public void setStudentSubmission(boolean studentSubmission) {
        this.studentSubmission = studentSubmission;
    }
}
