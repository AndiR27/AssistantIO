package org.acme.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolationException;
import org.acme.entity.Etudiant;
import org.acme.mapping.EtudiantMapper;
import org.acme.models.EtudiantDTO;
import org.acme.repository.EtudiantRepository;

@ApplicationScoped
public class ServiceEtudiant {

    @Inject
    EtudiantRepository etudiantRepository;

    @Inject
    EtudiantMapper etudiantMapper;

    /**
     * Creation d'un etudiant
     * @param etudiantDTO : dto de l'etudiant à créer
     * @return EtudiantDTO : dto de l'etudiant créé
     */
    public EtudiantDTO addEtudiant(EtudiantDTO etudiantDTO){

        try {
            Etudiant etudiantEntity = etudiantMapper.toEntity(etudiantDTO);
            etudiantRepository.persist(etudiantEntity);
            return etudiantMapper.toDto(etudiantEntity);
        } catch (ConstraintViolationException e) {
            System.out.println("Violation de contrainte : un étudiant avec cette valeur unique existe déjà." + e.getMessage());
        }
        return null;
    }
}
