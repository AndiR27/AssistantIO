package org.acme.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.entity.TP_Status;

import java.util.List;

@ApplicationScoped
public class TP_StatusRepository implements PanacheRepository<TP_Status> {

    //Retourne la liste des TP Status pour un TP donné
    public List<TP_Status> findByTP(Long tpId) {
        return find("travailPratique.id", tpId).list();
    }
}
