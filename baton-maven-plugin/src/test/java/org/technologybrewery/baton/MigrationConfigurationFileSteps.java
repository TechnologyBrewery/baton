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
import org.technologybrewery.baton.config.GroupTarget;
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

    private Map<String, GroupTarget> groupMap = new HashMap<>();

    private Map<String, GroupTarget> groupJsonMap = new HashMap<>();

    private ObjectMapper objectMapper = configureObjectMapper();


    private File groupsFile;

    private Exception encounteredException;

    @After("@migrationsConfig")
    public void cleanUp() {
        groupMap.clear();
        groupJsonMap.clear();
    }

    private ObjectMapper configureObjectMapper() {
        // TODO: move elsewhere
        ObjectMapper objectMapper = new ObjectMapper();

        SimpleModule module = new SimpleModule();
        module.addAbstractTypeMapping(AbstractValidatedElement.class, GroupTarget.class);

        objectMapper.registerModule(module);

        return objectMapper;
    }

    @Given("a group {string}") 
    public void a_group_of_migrations_with_group(String group) {
        GroupTarget groupTarget = new GroupTarget();
        groupTarget.setGroup(group);
        groupMap.put(group, groupTarget);
    }

    @Given("a migration described by {string} and {string} for group {string}")
    public void a_migration_described_by_and(String name, String implementation, String group) throws Exception {
        assertTrue(groupMap.containsKey(group), String.format("Group %s is not valid", group));
        GroupTarget groupTarget = groupMap.get(group);
        MigrationTarget migration = new MigrationTarget();
        if (StringUtils.isNotBlank(name)) {
            migration.setName(name);
        }

        if (StringUtils.isNotBlank(implementation)) {
            migration.setImplementation(implementation);
        }
        groupTarget.addMigration(migration);
        createTestMigrationsJson(groupTarget.getGroup(), groupTarget);

    }

    @Given("a migration with required fields as well as {string} for group {string}")
    public void a_migration_with_required_fields_as_well_as(String description, String group) throws Exception {
        assertTrue(group.contains(group), String.format("Group %s is not valid", group));
        GroupTarget groupTarget = groupMap.get(group);
        MigrationTarget migration = getMinimallyRequiredMigration();
        if (StringUtils.isNotBlank(description)) {
            migration.setDescription(description);
        }
        groupTarget.addMigration(migration);
        createTestMigrationsJson(groupTarget.getGroup(), groupTarget);
    }

    @Given("a valid migration with the file set definition of {string}, {string}, {string}, {string} for group {string}")
    public void a_valid_migration_with_the_file_set_definition_of(String directory, String includesAsSingleString, String excludesAsSingleString,
                                                                  String followSymlinksAsString, String group) throws Exception {
        assertTrue(groupMap.containsKey(group), String.format("Group %s is not valid", group));
        GroupTarget groupTarget = groupMap.get(group);
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
        groupTarget.addMigration(migration);
        createTestMigrationsJson(groupTarget.getGroup(), groupTarget);

    }

    @When("the configuration is read")
    public void the_configuration_is_read() {
        encounteredException = null;

        try {
            GroupTarget groupFromJson = GroupTarget.readAndValidateJson(groupsFile, objectMapper);
            assertNotNull(groupFromJson, "Could not read migrations file!");
            groupJsonMap.put(groupFromJson.getGroup(), groupFromJson);

        } catch (JsonException e) {
            encounteredException = e;
        }
    }

    @Then("a valid migration is available as {string} with {string} for group {string}")
    public void a_valid_migration_is_available_as_with(String expectedName, String expectedImplementation, String group) {
        checkForUnexpectedException();
        MigrationTarget foundMigration = retrieveMigrationFromGroup(group);
        assertEquals(expectedName, foundMigration.getName(), String.format("Unexpected migration name found for group %s!", group));
        assertEquals(expectedImplementation, foundMigration.getImplementation(), "Unexpected migration implementation found!");

    }

    @Then("a valid migration is available with {string} for group {string}")
    public void a_valid_migration_is_available_with(String expectedDescription, String group) {
        checkForUnexpectedException();
        MigrationTarget foundMigration = retrieveMigrationFromGroup(group);
        assertEquals(expectedDescription, foundMigration.getDescription(), "Unexpected migration description found!");
    }

    @Then("a valid migration is available as {string}, {string}, {string}, {string} for group {string}")
    public void a_valid_migration_is_available_as(String expectedDirectory, String expectedIncludesAsSingleString,
                                                  String expectedExcludesAsSingleString, String expectedFollowSymLinksAsString, String group) {

        checkForUnexpectedException();
        MigrationTarget foundMigration = retrieveMigrationFromGroup(group);
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

    private void createTestMigrationsJson(String name, GroupTarget group) throws IOException {
        groupsFile = new File(FileUtils.getTempDirectory(), name + "-migrations.json");
        objectMapper.writeValue(groupsFile, group);
        assertTrue(groupsFile.exists(), "Group configuration not written to file!");
    }

    private void checkForUnexpectedException() {
        if (encounteredException != null) {
            throw new BatonException("Exception encountered loading groups and migrations!", encounteredException);
        }
    }

    private MigrationTarget retrieveMigrationFromGroup(String group) {
        assertEquals(1, groupJsonMap.size(), "Expected exactly one group to be loaded!");
        assertTrue(groupJsonMap.containsKey(group), String.format("Could not get group %s in migration.json", group));
        GroupTarget foundGroup = groupJsonMap.get(group);
        List<MigrationTarget> migrations = foundGroup.getMigrations();
        assertEquals(1, migrations.size(), String.format("Expected exactly one migration to be loaded in group %s!", group));
        MigrationTarget foundMigration = migrations.iterator().next();
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
