package org.technologybrewery.baton;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Used as an ObjectMapper mixin to ignore the version field for MigrationTargets
 */
public abstract class MigrationTargetMixin {
    
    @JsonIgnore
    private String version;
}
