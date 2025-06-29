package org.acme.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.entity.TP;

import java.util.List;

@ApplicationScoped
public class TPRepository implements PanacheRepository<TP> {

    public List<TP> findByCourseId(Long courseId) {
        return find("course.id", courseId).list();
    }
}
