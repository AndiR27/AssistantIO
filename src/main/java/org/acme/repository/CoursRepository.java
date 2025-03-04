package org.acme.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;

import jakarta.enterprise.context.ApplicationScoped;
import org.acme.entity.*;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

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

    /**
     * Methode permettant de retrouver un TP selon son no
     */
    public TravailPratique findTpByNo(Long idCours, int noTp) {
        return findByIdOptional(idCours)
                .stream()
                .flatMap(cours -> cours.travauxPratiques.stream()) // Transforme en Stream<TravailPratique>
                .filter(tp -> Objects.equals(tp.no, noTp)) // Comparaison sécurisée avec Objects.equals()
                .findFirst()
                .get(); // Récupère le premier trouvé sous forme d'Optional
    }
}
