package org.acme.mapping;

import org.mapstruct.*;

@MapperConfig(
        componentModel = MappingConstants.ComponentModel.JAKARTA_CDI,
        unmappedTargetPolicy = ReportingPolicy.WARN,
        mappingInheritanceStrategy = MappingInheritanceStrategy.AUTO_INHERIT_FROM_CONFIG,
        injectionStrategy =InjectionStrategy.CONSTRUCTOR
)
public interface CentralConfig {
}
