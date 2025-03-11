package org.acme.mapping;


import org.acme.entity.*;
import org.acme.models.*;
import org.mapstruct.*;

/**
 * @Mapper(componentModel = "cdi") :
 * indique à MapStruct et à Quarkus que cette interface doit être gérée par le conteneur CDI.
 */
@Mapper(config = CentralConfig.class, componentModel = "cdi",
        uses = {EtudiantMapper.class, TravailPratiqueMapper.class})
public interface CoursMapper {
    /**
     * Convertit un Cours (entité) en CoursDto (DTO).
     */
    CoursDTO toDto(Cours cours);

    /**
     * Convertit un CoursDto (DTO) en Cours (entité).
     */
    Cours toEntity(CoursDTO dto);

    /**
     * Met à jour un Cours (entité) à partir d'un CoursDto (DTO).
     */
    @Mapping(target = "id", ignore = true)
    void updateEntity(CoursDTO dto, @MappingTarget Cours entity);

    //--- Méthodes par défaut pour les enums ---

    /**
     * Conversion TypeSemestre -> TypeSemestreDTO
     */
    default TypeSemestreDTO toDto(TypeSemestre semestre) {
        if (semestre == null) {
            return null;
        }
        // Supposons que TypeSemestreDTO est un enum "miroir" de TypeSemestre
        return TypeSemestreDTO.valueOf(semestre.name());
    }

    /**
     * Conversion TypeSemestreDTO -> TypeSemestre
     */
    default TypeSemestre toEntity(TypeSemestreDTO semestreDTO) {
        if (semestreDTO == null) {
            return null;
        }
        return TypeSemestre.valueOf(semestreDTO.name());
    }

    /**
     * Conversion TypeCours -> TypeCoursDTO
     */
    default TypeCoursDTO toDto(TypeCours typeCours) {
        if (typeCours == null) {
            return null;
        }
        return TypeCoursDTO.valueOf(typeCours.name());
    }

    /**
     * Conversion TypeCoursDTO -> TypeCours
     */
    default TypeCours toEntity(TypeCoursDTO typeCoursDTO) {
        if (typeCoursDTO == null) {
            return null;
        }
        return TypeCours.valueOf(typeCoursDTO.name());
    }
}
