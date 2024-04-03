package org.technologybrewery.baton;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides summary information across multiple baton migration executions.
 */
public class BatonExecutionSummary {

    private static final Logger logger = LoggerFactory.getLogger(BatonExecutionSummary.class);

    private List<GroupSummary> groupSummaries = new ArrayList<>();

    /**
     * Adds a specific migration summary run to the cross-migration summary..
     *
     * @param summary summary to add
     */
    public void addGroupSummary(GroupSummary summary) {
        if (summary == null) {
            logger.warn("Mission migration summary details - file counts may be inaccurate!");
        } else {
            groupSummaries.add(summary);
        }
    }

    public int getNumberOfTargetsExecuted() {
        int numberOfTargetsExecuted = 0;
        for(GroupSummary groupSummary : groupSummaries) {
            numberOfTargetsExecuted += groupSummary.getMigrationSummaries().size();
        }
        return numberOfTargetsExecuted;
    }

    public List<GroupSummary> getGroupSummaries() {
        return this.groupSummaries;
    }

    /**
     * Human-readable summary information.
     *
     * @return the summary as a readable string
     */
    public String getSummary() {
        int filesSuccess = 0;
        int filesUnsuccess = 0;
        for(GroupSummary groupSummary : groupSummaries) {
            for(MigrationSummary migrationSummary : groupSummary.getMigrationSummaries()) {
                filesSuccess += migrationSummary.getFilesSuccessfullyMigrated();
                filesUnsuccess += migrationSummary.getFilesUnsuccessfullyMigrated();
            }
        }
        return String.format("Migrations Processed: %d, Successfully Migrated Files: %d, Unsuccessfully Migrated Files: %d",
                getNumberOfTargetsExecuted(), filesSuccess, filesUnsuccess);
    }

}
