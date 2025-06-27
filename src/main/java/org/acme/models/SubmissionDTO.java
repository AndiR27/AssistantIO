package org.acme.models;

public class SubmissionDTO {
    private Long id;
    private String fileName;
    private String pathStorage;
    private String pathFileStructured;

    // Constructeurs
    public SubmissionDTO() {}

    public SubmissionDTO(Long id, String fileName, String pathStorage, String pathFileStructured) {
        this.id = id;
        this.fileName = fileName;
        this.pathStorage = pathStorage;
        this.pathFileStructured = pathFileStructured;
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getPathStorage() { return pathStorage; }
    public void setPathStorage(String pathStorage) { this.pathStorage = pathStorage; }

    public String getPathFileStructured() { return pathFileStructured; }
    public void setPathFileStructured(String pathFileStructured) { this.pathFileStructured = pathFileStructured; }
}
