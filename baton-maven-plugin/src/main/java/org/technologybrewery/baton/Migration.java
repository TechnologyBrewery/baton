package org.technologybrewery.baton;

import org.apache.maven.shared.model.fileset.FileSet;

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

}
