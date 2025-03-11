package org.acme.mapping;

import org.mapstruct.*;

@MapperConfig(
        componentModel = "cdi",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        mappingInheritanceStrategy = MappingInheritanceStrategy.AUTO_INHERIT_FROM_CONFIG,
        injectionStrategy =InjectionStrategy.CONSTRUCTOR
)
public interface CentralConfig {
}
