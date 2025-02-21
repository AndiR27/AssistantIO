package org.acme.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;

import jakarta.enterprise.context.ApplicationScoped;
import org.acme.entity.*;

import java.util.List;

@ApplicationScoped
public class EtudiantRepository implements PanacheRepository<Etudiant>{

}
