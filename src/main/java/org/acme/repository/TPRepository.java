package org.acme.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.entity.TravailPratique;

@ApplicationScoped
public class TPRepository implements PanacheRepository<TravailPratique> {
}
