package org.technologybrewery.baton;

import org.apache.maven.shared.model.fileset.FileSet;

import java.io.File;

/**
 * Base migration for testing flow - does nothing, but can be instantiated.
 */
public class NoOpMigration extends AbstractMigration {

    public MigrationSummary execute(FileSet[] fileSets) {
        return new MigrationSummary(0, 0);
    }

    @Override
    protected boolean shouldExecuteOnFile(File file) {
        return true;
    }

    @Override
    protected boolean performMigration(File file) {
        return true;
    }

}
