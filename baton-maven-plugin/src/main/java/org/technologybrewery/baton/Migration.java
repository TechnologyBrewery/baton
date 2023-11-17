package org.technologybrewery.baton;

import org.apache.maven.shared.model.fileset.FileSet;

import java.util.Set;

/**
 * Interface to authoring a migration.
 */
public interface Migration {

    /**
     * Executes the migration.
     *
     * @param filesets the files on which to perform the migration
     * @return a summary of the migrations performed
     */
    MigrationSummary execute(FileSet[] filesets);

    String getName();

    void setName(String name);

    String getDescription();

    void setDescription(String description);

    boolean isActive();

    void setActive(boolean active);

    Set<String> getFileNamePatterns();

    void setFileNamePatterns(Set<String> fileNamePatterns);

}
