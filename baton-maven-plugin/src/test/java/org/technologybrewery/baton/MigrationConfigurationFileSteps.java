package org.technologybrewery.baton;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.cucumber.java.After;
import io.cucumber.java.ParameterType;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.technologybrewery.baton.config.FileSet;
import org.technologybrewery.baton.config.MigrationTarget;
import org.technologybrewery.commons.json.AbstractValidatedElement;
import org.technologybrewery.commons.json.JsonException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MigrationConfigurationFileSteps {

    private ObjectMapper objectMapper = configureObjectMapper();

    private Map<String, MigrationTarget> migrationMap = new HashMap<>();

    private File migrationsFile;

    private Exception encounteredException;

    @After("@migrationsConfig")
    public void cleanUp() {
    }

    private ObjectMapper configureObjectMapper() {
        // TODO: move elsewhere
        ObjectMapper objectMapper = new ObjectMapper();

        SimpleModule module = new SimpleModule();
        module.addAbstractTypeMapping(AbstractValidatedElement.class, MigrationTarget.class);

        objectMapper.registerModule(module);

        return objectMapper;
    }

    @Given("a migration described by {string} and {string}")
    public void a_migration_described_by_and(String name, String implementation) throws Exception {
        MigrationTarget migration = new MigrationTarget();
        if (StringUtils.isNotBlank(name)) {
            migration.setName(name);
        }

        if (StringUtils.isNotBlank(implementation)) {
            migration.setImplementation(implementation);
        }

        createTestMigrationsJson(migration.getName(), migration);

    }

    @Given("a migration with required fields as well as {string}")
    public void a_migration_with_required_fields_as_well_as(String description) throws Exception {
        MigrationTarget migration = getMinimallyRequiredMigration();
        if (StringUtils.isNotBlank(description)) {
            migration.setDescription(description);
        }

        createTestMigrationsJson(migration.getName(), migration);
    }

    @Given("a valid migration with the file set definition of {string}, {string}, {string}, {string}")
    public void a_valid_migration_with_the_file_set_definition_of(String directory, String includesAsSingleString, String excludesAsSingleString,
                                                                  String followSymlinksAsString) throws Exception {

        MigrationTarget migration = getMinimallyRequiredMigration();
        FileSet fileSet = new FileSet();
        migration.addFileSets(fileSet);

        if (StringUtils.isNotBlank(directory)) {
            fileSet.setDirectory(directory);
        }

        if (StringUtils.isNotBlank(includesAsSingleString)) {
            List<String> includes = splitCommaSeparatedString(includesAsSingleString);
            for (String include : includes) {
                fileSet.addInclude(include);
            }
        }

        if (StringUtils.isNotBlank(excludesAsSingleString)) {
            List<String> excludes = splitCommaSeparatedString(excludesAsSingleString);
            for (String exclude : excludes) {
                fileSet.addExclude(exclude);
            }
        }

        if (StringUtils.isNotBlank(followSymlinksAsString)) {
            fileSet.setFollowSymlinks(Boolean.valueOf(followSymlinksAsString));
        }

        createTestMigrationsJson(migration.getName(), migration);

    }

    @When("the configuration is read")
    public void the_configuration_is_read() {
        encounteredException = null;

        try {
            MigrationTarget migrationFromJson = MigrationTarget.readAndValidateJson(migrationsFile, objectMapper);
            assertNotNull(migrationFromJson, "Could not read migrations file!");

            migrationMap.put(migrationFromJson.getName(), migrationFromJson);

        } catch (JsonException e) {
            encounteredException = e;
        }
    }

    @Then("a valid migration is available as {string} with {string}")
    public void a_valid_migration_is_available_as_with(String expectedName, String expectedImplementation) {
        checkForUnexpectedException();

        MigrationTarget foundMigration = retrieveSingleResult();
        assertEquals(expectedName, foundMigration.getName(), "Unexpected migration name found!");
        assertEquals(expectedImplementation, foundMigration.getImplementation(), "Unexpected migration implementation found!");

    }

    @Then("a valid migration is available with {string}")
    public void a_valid_migration_is_available_with(String expectedDescription) {
        checkForUnexpectedException();

        MigrationTarget foundMigration = retrieveSingleResult();
        assertEquals(expectedDescription, foundMigration.getDescription(), "Unexpected migration description found!");
    }

    @Then("a valid migration is available as {string}, {string}, {string}, {string}")
    public void a_valid_migration_is_available_as(String expectedDirectory, String expectedIncludesAsSingleString,
                                                  String expectedExcludesAsSingleString, String expectedFollowSymLinksAsString) {

        checkForUnexpectedException();

        MigrationTarget foundMigration = retrieveSingleResult();
        List<FileSet> fileSets = foundMigration.getFileSets();
        assertNotNull(fileSets, "No file sets found!");
        assertEquals(1, fileSets.size(), "Expected exactly 1 file set!");
        FileSet fileSet = fileSets.iterator().next();

        if (StringUtils.isNotBlank(expectedDirectory)) {
            assertEquals(expectedDirectory, fileSet.getDirectory(), "Unexpected file set directory found!");
        }

        if (StringUtils.isNotBlank(expectedIncludesAsSingleString)) {
            List<String> expectedIncludes = splitCommaSeparatedString(expectedIncludesAsSingleString);
            List<String> foundIncludes = fileSet.getIncludes();
            assertNotNull(foundIncludes, "No includes found!");
            assertEquals(expectedIncludes.size(), foundIncludes.size(), "Unexpected number of includes found!");
            for (String foundInclude : foundIncludes) {
                assertTrue(expectedIncludes.contains(foundInclude), String.format("Include '%s` was not expected!", foundInclude));
            }
        }

        if (StringUtils.isNotBlank(expectedExcludesAsSingleString)) {
            List<String> expectedExcludes = splitCommaSeparatedString(expectedExcludesAsSingleString);
            List<String> foundExcludes = fileSet.getExcludes();
            assertNotNull(foundExcludes, "No excludes found!");
            assertEquals(expectedExcludes.size(), foundExcludes.size(), "Unexpected number of excludes found!");
            for (String foundExclude : foundExcludes) {
                assertTrue(expectedExcludes.contains(foundExclude), String.format("Exclude '%s` was not expected!", foundExclude));
            }
        }

        if (StringUtils.isNotBlank(expectedFollowSymLinksAsString)) {
            assertEquals(Boolean.valueOf(expectedFollowSymLinksAsString), fileSet.getFollowSymlinks(), "Unexpected follow symlinks found!");
        }

    }

    private MigrationTarget getMinimallyRequiredMigration() {
        MigrationTarget migration = new MigrationTarget();
        migration.setName("migration-with-description");
        migration.setImplementation("o.t.b.DescriptiveMigration");
        return migration;
    }

    private void createTestMigrationsJson(String name, MigrationTarget migration) throws IOException {
        migrationsFile = new File(FileUtils.getTempDirectory(), name + "-migrations.json");
        objectMapper.writeValue(migrationsFile, migration);
        assertTrue(migrationsFile.exists(), "Migration configuration not written to file!");
    }

    private void checkForUnexpectedException() {
        if (encounteredException != null) {
            throw new BatonException("Exception encountered loading migrations!", encounteredException);
        }
    }

    private MigrationTarget retrieveSingleResult() {
        assertEquals(1, migrationMap.size(), "Expected exactly one migration to be loaded!");
        MigrationTarget foundMigration = migrationMap.values().iterator().next();
        return foundMigration;
    }

    @ParameterType("[^,]+(?:,[^,]+)+")
    public List<String> listOfString(String listString) {
        List<String> finalList = new ArrayList<>();
        String[] stringArray = listString.split(", ");
        for (String string : stringArray) {
            finalList.add(string);
        }
        return finalList;
    }

    protected List<String> splitCommaSeparatedString(String values) {
        List<String> valuesAsList;

        if (StringUtils.isBlank(values)) {
            valuesAsList = Collections.EMPTY_LIST;
        } else {
            valuesAsList = new ArrayList<>();
            String[] valuesArray = StringUtils.split(values, ",");
            for (String value : valuesArray) {
                valuesAsList.add(value.trim());
            }
        }

        return valuesAsList;
    }

}
