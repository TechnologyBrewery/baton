package org.technologybrewery.baton;

import io.cucumber.java.After;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.apache.maven.model.InputLocation;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.technologybrewery.baton.util.pom.LocationAwareMavenReader;
import org.technologybrewery.baton.util.pom.PomHelper;
import org.technologybrewery.baton.util.pom.PomModifications;
import org.technologybrewery.baton.util.pom.PomModifications.Modification;
import org.technologybrewery.baton.util.pom.PomModifications.Deletion;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.technologybrewery.baton.util.pom.PomHelper.writeModifications;
import static java.nio.file.Files.copy;

public class PomUtilsTestSteps {
    private static final String TEST_FILE_NAME = "pom.xml";
    private static final String TEST_ELEMENT_TAG = "inherited";
    private static final String TEST_REPLACEMENT_VALUE = "true";
    private static final String TEST_REPLACEMENT
            = "<"+TEST_ELEMENT_TAG+">"+TEST_REPLACEMENT_VALUE+"</"+TEST_ELEMENT_TAG+">";
    private static final String TEST_INSERTION = "<configuration></configuration>";
    File testFile = new File("target/pom-utils-test/", TEST_FILE_NAME);
    File testFileTemplate = new File("src/test/resources/test-files/", TEST_FILE_NAME);
    Model testFileModel;

    String artifactId;
    boolean migrationRan;

    @Given("a pom file with existing elements")
    public void a_pom_file_with_existing_elements() throws BatonException {
        try {
                testFile.getParentFile().mkdirs();
                copy(testFileTemplate.toPath(), testFile.toPath());
                testFileModel = PomHelper.getLocationAnnotatedModel(testFile);
        } catch (IOException e) {
            throw new BatonException("Unable to copy test POM file to target directory", e);
        }
    }

    @When("I write a deletion for an existing element")
    public void i_write_a_deletion_for_an_existing_element() {
        Element testElement = getTestElement();
        Modification delete = new Deletion(testElement.startLocation, testElement.endLocation);
        migrationRan = applyModificationsToFile(delete);
    }

    @When("I write a replacement for an existing element")
    public void i_write_a_replacement_for_an_existing_element() {
        Element testElement = getTestElement();

       Modification replace = new PomModifications.Replacement(testElement.startLocation, testElement.endLocation, TEST_REPLACEMENT);
       migrationRan = applyModificationsToFile(replace);
    }

    @When("I write an insertion for a new element")
    public void i_write_an_insertion_for_a_new_element() {
        Plugin plugin = testFileModel.getBuild().getPlugins().get(0);
        artifactId = plugin.getArtifactId();
        InputLocation insertLocation = plugin.getLocation(TEST_ELEMENT_TAG + LocationAwareMavenReader.END);

        Modification insert = new PomModifications.Insertion(insertLocation,3, indent -> indent+TEST_INSERTION);
        migrationRan = applyModificationsToFile(insert);
    }

    @Then("the existing element should not be present in the pom file")
    public void the_existing_element_should_not_be_present_in_the_pom_file() {
        assertTrue(migrationRan, "POM modification was not run successfully");

        testFileModel = PomHelper.getLocationAnnotatedModel(testFile); // Reload the file
        List<Plugin> plugins = testFileModel.getBuild().getPlugins();

        boolean anElementWasRemoved = false;

        for (Plugin plugin : plugins) {
            if (artifactId.equals(plugin.getArtifactId())) {
                InputLocation location = plugin.getLocation(TEST_ELEMENT_TAG + LocationAwareMavenReader.START);
                anElementWasRemoved = location == null;
                break;
            }
        }
        assertTrue(anElementWasRemoved, "element found when it should be deleted");
    }

    @Then("the new element should be present in the pom file in place of the existing element")
    public void the_new_element_should_be_present_in_the_pom_file_in_place_of_the_existing_element() {
        // Write code here that turns the phrase above into concrete actions
        assertTrue(migrationRan, "POM modification was not run successfully");

        testFileModel = PomHelper.getLocationAnnotatedModel(testFile); // Reload the file
        List<Plugin> plugins = testFileModel.getBuild().getPlugins();

        boolean elementWasFound = false;

        for (Plugin plugin : plugins) {
            if (artifactId.equals(plugin.getArtifactId())) {
                elementWasFound = TEST_REPLACEMENT_VALUE.equals(plugin.getInherited());
                break;
            }
        }
        assertTrue(elementWasFound, "Existing element replacement not matching");

    }

    @Then("the new element should be present in the pom file")
    public void the_new_element_should_be_present_in_the_pom_file() {
        assertTrue(migrationRan, "POM modification was not run successfully");

        testFileModel = PomHelper.getLocationAnnotatedModel(testFile); // Reload the file
        List<Plugin> plugins = testFileModel.getBuild().getPlugins();

        boolean elementWasFound = false;

        for (Plugin plugin : plugins) {
            if (artifactId.equals(plugin.getArtifactId())) {
                elementWasFound = plugin.getConfiguration() != null;
                break;
            }
        }
        assertTrue(elementWasFound, "element not found when it should be inserted");
    }

    @After
    public void cleanup() {
        if (testFile != null && testFile.exists()) {
            testFile.delete();
        }
    }

    private boolean applyModificationsToFile(Modification mod) {
        PomModifications pomMods = new PomModifications();
        pomMods.add(mod);
        return writeModifications(testFile, pomMods.finalizeMods());
    }

    private Element getTestElement() {
        Plugin plugin = testFileModel.getBuild().getPlugins().get(0);
        artifactId = plugin.getArtifactId();
        InputLocation startLocation = plugin.getLocation(TEST_ELEMENT_TAG + LocationAwareMavenReader.START);
        InputLocation endLocation = plugin.getLocation(TEST_ELEMENT_TAG + LocationAwareMavenReader.END);
        Element element = new Element(startLocation, endLocation);
        return element;
    }

    private static class Element {
        public final InputLocation startLocation;
        public final InputLocation endLocation;

        public Element(InputLocation startLocation, InputLocation endLocation) {
            this.startLocation = startLocation;
            this.endLocation = endLocation;
        }
    }
}
