package org.technologybrewery.baton;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.technologybrewery.baton.config.GroupTarget;
import org.technologybrewery.baton.config.MigrationTarget;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BatonMojoTestSteps {

    private TestBatonMojo batonMojo = new TestBatonMojo();
    private List<GroupTarget> groupTargets;
    private BatonExecutionSummary summary;
    private Exception mojoException;

    @Before("@baton")
    public void startUp() {
        groupTargets = new ArrayList<>();
    }

    @Given("groups {string} with migrations")
    public void groups_with_migrations(String groups) {
        for(String group : groups.split(",")) {
            GroupTarget groupTarget = new GroupTarget();
            groupTarget.setGroup(group);
            groupTarget.setType(BatonMojo.ORDERED);
            MigrationTarget migrationTarget = new MigrationTarget();
            migrationTarget.setName(group);
            migrationTarget.setImplementation("org.technologybrewery.baton.NoOpMigration");
            groupTarget.addMigration(migrationTarget);
            groupTargets.add(groupTarget);
        }
    }

    @Given("a group with type {string}")
    public void a_group_with_type(String type) {
        GroupTarget groupTarget = new GroupTarget();
        groupTarget.setGroup("Group_1");
        groupTarget.setType(type);
        groupTargets.add(groupTarget);
    }

    @Given("migrations {string} with implementations")
    public void migrations_with_implementations(String migrations) {
        assertEquals(1, groupTargets.size(), "Unexpected number of groups added to test!");
        GroupTarget groupTarget = groupTargets.get(0);
        for(String migration : migrations.split(",")) {
            MigrationTarget migrationTarget = new MigrationTarget();
            migrationTarget.setName(migration);
            migrationTarget.setImplementation("org.technologybrewery.baton.NoOpMigration");
            groupTarget.addMigration(migrationTarget);
        }
    }

    @Given("migrations {string} with {string} and implementations")
    public void migrations_with_versions_and_implementations(String migrations, String versions) {
        String[] splitMigrations = migrations.split(",");
        String[] splitVersions = versions.split(",");
        assertEquals(splitMigrations.length, splitVersions.length, "Migrations length does not equal versions length!");
        assertEquals(1, groupTargets.size());
        GroupTarget groupTarget = groupTargets.get(0);
        for(int i=0;i<splitMigrations.length;i++) {
            String migration = splitMigrations[i];
            String version = splitVersions[i];
            MigrationTarget migrationTarget = new MigrationTarget();
            migrationTarget.setName(migration);
            migrationTarget.setImplementation("org.technologybrewery.baton.NoOpMigration");
            migrationTarget.setVersion(version);
            groupTarget.addMigration(migrationTarget);
        }
    }

    @Given("a migration target with a name {string} and a implementation")
    public void a_migration_target_with_a_and_a_implementation(String name) {
        MigrationTarget target = new MigrationTarget();
        target.setName(name);
        target.setImplementation("org.technologybrewery.baton.NoOpMigration");
        assertEquals(1, groupTargets.size());
        groupTargets.get(0).addMigration(target);;
    }

    @Given("Baton is configured to ignore {string}")
    public void baton_is_configured_to_ignore(String migrationToDeactivate) {
        Set<String> migrationsToDeactivate = new HashSet<>();
        migrationsToDeactivate.add(migrationToDeactivate);
        batonMojo.setDeactivateMigrations(migrationsToDeactivate);
    }

    @Given("minimum version {string}")
    public void minimum_version(String minimumVersion) {
        batonMojo.minimumVersion = minimumVersion;
    }

    @When("Baton executes")
    public void baton_executes() {
        try {
            batonMojo.sourceDirectory = new File("./src/main/java");
            batonMojo.testDirectory = new File("./src/test/java");
            batonMojo.baseDirectory = new File("./");
            batonMojo.sortMigrationsInGroups(groupTargets);
            summary = batonMojo.performMigration(groupTargets);
        } catch(BatonException e) {
            mojoException = e;
        }
    }

    @Then("groups are executed in the order {string}") 
    public void groups_are_executed_in_the_order(String groups) throws Exception {
        checkException();
        List<GroupSummary> groupSummaries = summary.getGroupSummaries();
        String[] splitGroups = groups.split(",");
        for(int i=0;i<splitGroups.length;i++) {
            String group = splitGroups[i];
            GroupSummary summary = groupSummaries.get(i);
            assertEquals(group, summary.getGroupName());
        }
    }

    @Then("migrations are executed in the order {string}") 
    public void migrations_are_executed_in_the_order(String migrations) throws Exception {
        checkException();
        List<GroupSummary> groupSummaries = summary.getGroupSummaries();
        assertEquals(1, groupSummaries.size());
        GroupSummary groupSummary = groupSummaries.get(0);
        String[] splitMigrations = migrations.split(",");
        List<MigrationSummary> migrationSummaries = groupSummary.getMigrationSummaries();
        for(int i=0;i<splitMigrations.length;i++) {
            String migration = splitMigrations[i];
            MigrationSummary migrationSummary = migrationSummaries.get(i);
            assertEquals(migration, migrationSummary.getName()); 
        }
    }

    @Then("a single migration is performed")
    public void a_single_migration_is_performed() throws Exception {
        validateNumberOfMigrationsPerformed(1);
    }

    @Then("no migration is performed")
    public void no_migration_is_performed() throws Exception {
        validateNumberOfMigrationsPerformed(0);
    }

    private void validateNumberOfMigrationsPerformed(int numberOfExpectedMigrations) throws Exception {
        checkException();
        int numberOfMigrationsExecuted = summary.getNumberOfTargetsExecuted();
        assertEquals(numberOfExpectedMigrations, numberOfMigrationsExecuted, "Unexpected number of migrations performed!");
    }

    private void checkException() throws Exception {
        if(mojoException != null) {
            throw mojoException;
        }
    }

}
