package org.technologybrewery.baton.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.technologybrewery.commons.json.AbstractValidatedElement;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds target information to support migration execution. A target
 * represents the implementation details for each migration.
 */
public class MigrationTarget extends AbstractValidatedElement {

    @JsonIgnore
    private static final Logger logger = LoggerFactory.getLogger(MigrationTarget.class);

    @JsonProperty(required = true)
    private String name;

    @JsonProperty
    private String description;

    @JsonProperty(required = true)
    private String implementation;

    @JsonProperty
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<FileSet> fileSets = new ArrayList<>();

    public MigrationTarget() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImplementation() {
        return implementation;
    }

    public void setImplementation(String implementation) {
        this.implementation = implementation;
    }

    public List<FileSet> getFileSets() {
        return fileSets;
    }

    @JsonIgnore
    public void addFileSets(FileSet fileSet) {
        this.fileSets.add(fileSet);
    }

    @Override
    public String getSchemaFileName() {
        return "migration-target-schema.json";
    }

}
