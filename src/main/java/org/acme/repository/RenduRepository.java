package org.acme.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.entity.Rendu;

@ApplicationScoped
public class RenduRepository implements PanacheRepository<Rendu> {
}
