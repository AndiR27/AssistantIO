package heg.backendspring.repository;

import heg.backendspring.entity.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RepositorySubmission extends JpaRepository<Submission, Long> {
}
