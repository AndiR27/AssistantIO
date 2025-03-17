package org.acme.mapping;


import org.acme.entity.Etudiant;
import org.acme.entity.TypeEtude;
import org.acme.models.EtudiantDTO;
import org.acme.models.TypeEtudeDTO;
import org.mapstruct.*;

@Mapper(config = CentralConfig.class)
public interface EtudiantMapper {
    // on ignore la liste "coursInscrits" pour éviter que l'étudiant
    // ne contienne lui-même les cours, qui contiennent les étudiants, etc.
    @Mapping(target = "coursEtudiant", ignore = true)
    EtudiantDTO toDto(Etudiant etudiant);

    // ou inversement, on ignore dans l’autre sens
    Etudiant toEntity(EtudiantDTO dto);

    // Gestion de typeEtude
    default TypeEtudeDTO toDto(TypeEtude typeEtude) {
        if (typeEtude == null) {
            return null;
        }
        // Supposons que TypeSemestreDTO est un enum "miroir" de TypeSemestre
        return TypeEtudeDTO.valueOf(typeEtude.name());
    }

    default TypeEtude toEntity(TypeEtudeDTO typeEtudeDTO) {
        if (typeEtudeDTO == null) {
            return null;
        }
        return TypeEtude.valueOf(typeEtudeDTO.name());
    }
}
