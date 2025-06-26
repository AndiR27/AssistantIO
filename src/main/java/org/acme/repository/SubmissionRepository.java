package org.acme.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.entity.Submission;

@ApplicationScoped
public class SubmissionRepository implements PanacheRepository<Submission> {
}
