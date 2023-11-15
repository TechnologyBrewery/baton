package org.technologybrewery.baton;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.apache.commons.lang3.RandomStringUtils;
import org.technologybrewery.baton.config.MigrationTarget;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MigrationTargetSteps {

    private List<MigrationTarget> targets = new ArrayList<>();
    private BatonExecutionSummary summary;

    private Exception mojoExecutionException;

    @Given("a migration target with a name and and implementation")
    public void a_migration_target_with_a_name_and_and_implementation() {
        MigrationTarget target = new MigrationTarget();
        target.setName(RandomStringUtils.randomAlphabetic(10));
        target.setImplementation("org.technologybrewery.baton.NoOpMigration");
        targets.add(target);
    }

    @When("Baton executes")
    public void baton_executes() {
        try {
            BatonMojo batonMojo = new BatonMojo();
            batonMojo.sourceDirectory = new File("./src/main/java");
            batonMojo.testDirectory = new File("./src/test/java");
            batonMojo.baseDirectory = new File("./");
            summary = batonMojo.performMigration(targets);
        } catch (BatonException e) {
            mojoExecutionException = e;
        }
    }

    @Then("a single migration is performed")
    public void a_single_migration_is_performed() throws Exception {
        if (mojoExecutionException != null) {
            throw mojoExecutionException;
        }
        int numberOfMigrationsExecuted = summary.getNumberOfTargetsExecuted();
        assertEquals(1, numberOfMigrationsExecuted, "Unexpected number of migrations performed!");
    }

    @Then("an exception is thrown")
    public void an_exception_is_thrown() {
        assertNotNull(mojoExecutionException, "Baton should have failed, but passed!");
    }

}
