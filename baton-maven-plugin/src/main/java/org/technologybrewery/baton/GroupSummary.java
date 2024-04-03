package org.technologybrewery.baton;

import java.util.ArrayList;
import java.util.List;

/**
 * High level summarization information for a specific Group. 
 */
public class GroupSummary {

    private String groupName;

    private List<MigrationSummary> migrationSummaries;

    public GroupSummary(String groupName) {
        this.groupName = groupName;
        migrationSummaries = new ArrayList<>();
    }

    /**
     * Adds a migration summary to the group summary
     * @param migrationSummary
     */
    public void addMigrationSummary(MigrationSummary migrationSummary) {
        migrationSummaries.add(migrationSummary);
    }

    public List<MigrationSummary> getMigrationSummaries() {
        return this.migrationSummaries;
    }

    public String getGroupName() {
        return this.groupName;
    }
    
}
