package heg.backendspring.mapping;

import org.mapstruct.*;

@MapperConfig(
        componentModel = MappingConstants.ComponentModel.SPRING,      // Spring Boot
        unmappedTargetPolicy = ReportingPolicy.WARN,                  // warn si champ non mappé
        mappingInheritanceStrategy = MappingInheritanceStrategy.AUTO_INHERIT_FROM_CONFIG,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR             // injection par constructeur
)
public interface CentralConfig {
}
