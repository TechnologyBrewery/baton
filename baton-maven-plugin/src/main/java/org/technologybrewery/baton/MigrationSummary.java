package org.technologybrewery.baton;

/**
 * High level summarization information for a specific Migration run.
 */
public class MigrationSummary {

    private final String name;

    private final int filesSuccessfullyMigrated;

    private final int filesUnsuccessfullyMigrated;

    public MigrationSummary(String name, int filesSuccessfullyMigrated, int fileUnsuccessfullyMigrated) {
        this.name = name;
        this.filesSuccessfullyMigrated = filesSuccessfullyMigrated;
        this.filesUnsuccessfullyMigrated = fileUnsuccessfullyMigrated;
    }

    public int getFilesSuccessfullyMigrated() {
        return filesSuccessfullyMigrated;
    }

    public int getFilesUnsuccessfullyMigrated() {
        return filesUnsuccessfullyMigrated;
    }

    public String getName() {
        return this.name;
    }
}
