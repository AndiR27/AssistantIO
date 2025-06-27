package org.acme.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.entity.TPStatus;

import java.util.List;

@ApplicationScoped
public class TPStatusRepository implements PanacheRepository<TPStatus> {

    //Retourne la liste des TP Status pour un TP donné
    public List<TPStatus> findByTP(Long tpId) {
        return find("tp.id", tpId).list();
    }
}
