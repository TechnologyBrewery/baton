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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BatonMojoTestSteps {

    private TestBatonMojo batonMojo = new TestBatonMojo();

    private List<MigrationTarget> targets = new ArrayList<>();
    private BatonExecutionSummary summary;

    private Exception mojoExecutionException;

    @Given("a migration target with a name and a implementation")
    public void a_migration_target_with_a_name_and_a_implementation() {
        a_migration_target_with_a_and_a_implementation(RandomStringUtils.randomAlphabetic(10));
    }

    @Given("a migration target with a {string} and a implementation")
    public void a_migration_target_with_a_and_a_implementation(String name) {
        MigrationTarget target = new MigrationTarget();
        target.setName(name);
        target.setImplementation("org.technologybrewery.baton.NoOpMigration");
        targets.add(target);
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
            summary = batonMojo.performMigration(targets);
        } catch (BatonException e) {
            mojoExecutionException = e;
        }
    }

    @Then("a single migration is performed")
    public void a_single_migration_is_performed() throws Exception {
        validateNumberOfMigrationsPerformed(1);
    }

    private void validateNumberOfMigrationsPerformed(int numberOfExpectedMigrations) throws Exception {
        if (mojoExecutionException != null) {
            throw mojoExecutionException;
        }
        int numberOfMigrationsExecuted = summary.getNumberOfTargetsExecuted();
        assertEquals(numberOfExpectedMigrations, numberOfMigrationsExecuted, "Unexpected number of migrations performed!");
    }

    @Then("an exception is thrown")
    public void an_exception_is_thrown() {
        assertNotNull(mojoExecutionException, "Baton should have failed, but passed!");
    }

    @Then("no migration is performed")
    public void no_migration_is_performed() throws Exception {
        validateNumberOfMigrationsPerformed(0);
    }

}
