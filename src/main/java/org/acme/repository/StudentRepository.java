package org.acme.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;

import jakarta.enterprise.context.ApplicationScoped;
import org.acme.entity.*;

@ApplicationScoped
public class StudentRepository implements PanacheRepository<Student>{

    public Student findByEmail(String email) {
        return find("email", email).firstResult();
    }

}
