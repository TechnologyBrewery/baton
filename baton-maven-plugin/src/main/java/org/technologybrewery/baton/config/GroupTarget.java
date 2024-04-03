package org.technologybrewery.baton.config;

import java.util.ArrayList;
import java.util.List;

import org.technologybrewery.commons.json.AbstractValidatedElement;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Holds target information to support group execution. 
 */
public class GroupTarget extends AbstractValidatedElement {

    @JsonProperty(required = true)
    private String group;

    @JsonProperty(required = true)
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<MigrationTarget> migrations = new ArrayList<>();

    public GroupTarget(){}

    public void setGroup(String group) {
        this.group = group;
    }

    public String getGroup() {
        return this.group;
    }

    @JsonIgnore
    public void addMigration(MigrationTarget migration) {
        this.migrations.add(migration);
    }

    public List<MigrationTarget> getMigrations() {
        return this.migrations;
    }

    @Override
    public String getSchemaFileName() {
        return "migration-target-schema.json";
    }
    
}
