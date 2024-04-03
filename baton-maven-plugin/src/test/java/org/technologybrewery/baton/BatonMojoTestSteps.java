package org.technologybrewery.baton;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.technologybrewery.baton.config.GroupTarget;
import org.technologybrewery.baton.config.MigrationTarget;
import org.technologybrewery.commons.json.AbstractValidatedElement;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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

    @Given("a migration file")
    public void a_migration_file(String migrationText) throws IOException {
        File migrationFile = new File("migration-file.json");
        migrationFile.createNewFile();
        BufferedWriter writer = new BufferedWriter(new FileWriter(migrationFile));
        writer.write(migrationText);
        writer.close();
        ObjectMapper objectMapper = batonMojo.initializeObjectMapper();
        groupTargets = AbstractValidatedElement.readAndValidateJsonList(migrationFile,
                objectMapper, GroupTarget.class);
    }

    @Given("groups {word} with migrations")
    public void groups_with_migrations(String groups) {
        for(String group : groups.split(",")) {
            GroupTarget groupTarget = new GroupTarget();
            groupTarget.setGroup(group);
            MigrationTarget migrationTarget = new MigrationTarget();
            migrationTarget.setName(group);
            migrationTarget.setImplementation("org.technologybrewery.baton.NoOpMigration");
            groupTarget.addMigration(migrationTarget);
            groupTargets.add(groupTarget);
        }
    }

    @Given("a group")
    public void a_group() {
        GroupTarget groupTarget = new GroupTarget();
        groupTarget.setGroup("Group_1");
        groupTargets.add(groupTarget);
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

    @When("Baton executes")
    public void baton_executes() {
        try {
            batonMojo.sourceDirectory = new File("./src/main/java");
            batonMojo.testDirectory = new File("./src/test/java");
            batonMojo.baseDirectory = new File("./");
            summary = batonMojo.performMigration(groupTargets);
        } catch(BatonException e) {
            mojoException = e;
        }
    }

    @Then("groups are executed in the order they appear") 
    public void groups_are_executed_in_the_order_they_appear() throws Exception {
        if(mojoException != null) {
            throw mojoException;
        }
        List<GroupSummary> groupSummaries = summary.getGroupSummaries();
        for(int i=0;i<groupSummaries.size();i++) {
            GroupTarget groupTarget = groupTargets.get(i);
            GroupSummary summary = groupSummaries.get(i);
            assertTrue(groupTarget.getGroup().equals(summary.getGroupName()));
        }
    }

    @Then("a single migration is performed")
    public void a_single_migration_is_performed() throws Exception {
        validateNumberOfMigrationsPerformed(1);
    }

    private void validateNumberOfMigrationsPerformed(int numberOfExpectedMigrations) throws Exception {
        if(mojoException != null) {
            throw mojoException;
        }
        int numberOfMigrationsExecuted = summary.getNumberOfTargetsExecuted();
        assertEquals(numberOfExpectedMigrations, numberOfMigrationsExecuted, "Unexpected number of migrations performed!");
    }

    @Then("no migration is performed")
    public void no_migration_is_performed() throws Exception {
        validateNumberOfMigrationsPerformed(0);
    }

}
