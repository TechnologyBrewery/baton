package org.technologybrewery.baton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides summary information across multiple baton migration executions.
 */
public class BatonExecutionSummary {

    private static final Logger logger = LoggerFactory.getLogger(BatonExecutionSummary.class);

    private int filesSuccessfullyMigrated;

    private int filesUnsuccessfullyMigrated;

    private int numberOfTargetsExecuted;

    /**
     * Adds a specific migration summary run to the cross-migration summary..
     *
     * @param summary summary to add
     */
    public void addMigrationSummary(MigrationSummary summary) {
        if (summary == null) {
            logger.warn("Mission migration summary details - file counts may be inaccurate!");
        } else {
            filesSuccessfullyMigrated += summary.getFilesSuccessfullyMigrated();
            filesUnsuccessfullyMigrated += summary.getFilesUnsuccessfullyMigrated();
        }

        numberOfTargetsExecuted++;

    }

    public int getFilesSuccessfullyMigrated() {
        return filesSuccessfullyMigrated;
    }

    public int getFilesUnsuccessfullyMigrated() {
        return filesUnsuccessfullyMigrated;
    }

    public int getNumberOfTargetsExecuted() {
        return numberOfTargetsExecuted;
    }

    /**
     * Human-readable summary information.
     *
     * @return the summary as a readable string
     */
    public String getSummary() {
        return String.format("Migrations Processed: %d, Successfully Migrated Files: %d, Unsuccessfully Migrated Files: %d",
                numberOfTargetsExecuted, filesSuccessfullyMigrated, filesUnsuccessfullyMigrated);
    }

}
