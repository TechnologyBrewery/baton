package org.technologybrewery.baton;

import org.apache.maven.shared.model.fileset.FileSet;

/**
 * Base migration for testing flow - does nothing, but can be instantiated.
 */
public class NoOpMigration implements Migration {

    @Override
    public MigrationSummary execute(FileSet[] filesets) {
        return new MigrationSummary(0, 0);
    }

}
