package org.technologybrewery.baton;

/**
 * High level summarization information for a specific Migration run.
 */
public class MigrationSummary {

    private int filesSuccessfullyMigrated;

    private int filesUnsuccessfullyMigrated;

    public MigrationSummary(int filesSuccessfullyMigrated, int fileUnsuccessfullyMigrated) {
        this.filesSuccessfullyMigrated = filesSuccessfullyMigrated;
        this.filesUnsuccessfullyMigrated = fileUnsuccessfullyMigrated;
    }

    public int getFilesSuccessfullyMigrated() {
        return filesSuccessfullyMigrated;
    }

    public int getFilesUnsuccessfullyMigrated() {
        return filesUnsuccessfullyMigrated;
    }
}
