package org.acme.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;

import jakarta.enterprise.context.ApplicationScoped;
import org.acme.entity.*;

import java.util.List;

@ApplicationScoped
public class CoursRepository implements PanacheRepository<Cours>{

    //methode permettant de récupérer TOUS les étudiants inscrits à un cours
    public List<Etudiant> findEtudiantsInscrits(Long idCours){
        return find("id", idCours).firstResult().etudiantsInscrits;
    }

    /**
     * Methode permettant de retrouver un cours selon son code
     */
    public Cours findCoursByCode(String code) {
        return find("code", code).firstResult();
    }
}
