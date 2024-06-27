package org.technologybrewery.baton;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.technologybrewery.baton.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class UtilsTestSteps {

    private static final File DEFAULT_TEST_DIRECTORY = new File("target/utils-test/");
    private static final String DEFAULT_TEST_FILE_NAME = "text_file.txt";
    File testFile;
    Boolean match;
    String inputAsString;
    List<String> captureGroups;

    @Given("I have a string containing {string}")
    public void i_have_a_string_containing(String str) throws Throwable {
        inputAsString = str;
    }

    @When("I use the regex {string} to retrieve capture groups")
    public void i_use_the_regex_to_retrieve_capture_groups(String regex) throws IOException {
        if (testFile != null) {
            captureGroups = FileUtils.getRegExCaptureGroups(regex, testFile);
        } else {
            captureGroups = FileUtils.getRegExCaptureGroups(regex, inputAsString);
        }
    }

    @Then("the size of the retrieved capture groups should be \"{int}\"")
    public void the_size_of_the_retrieved_capture_groups_should_be(int groups) {
        assertEquals(groups, captureGroups.size(),"Unexpected size for capture groups");
    }

    @Then("the capture groups should include")
    public void the_capture_groups_should_include(List<String> expectedGroups) {
        assertEquals(expectedGroups, captureGroups);
    }

    @Given("I have a file containing the string {string}")
    public void i_have_a_file_containing_the_string(String string) throws IOException {
        testFile = new File(DEFAULT_TEST_DIRECTORY, DEFAULT_TEST_FILE_NAME);
        if (DEFAULT_TEST_DIRECTORY.mkdirs()) {
            FileUtils.writeFile(testFile, Collections.singletonList(string));
        }
    }

    @When("I use the regex {string} to substitute {string}")
    public void i_use_the_regex_to_substitute(String regex, String substitute) throws IOException {
        FileUtils.replaceInFile(testFile, regex, substitute);
    }

    @When("I use the literal {string} to substitute {string}")
    public void iUseTheLiteralToSubstitute(String literal, String substitute) throws IOException {
        FileUtils.replaceLiteralInFile(testFile, literal, substitute);
    }

    @Then("the file should now contain the string {string}")
    public void the_file_should_now_contain_the_string(String string) throws IOException {
        assertTrue(FileUtils.hasRegExMatch(string, testFile), String.format("File does not contain expected string %s",string));
    }

    @When("I use the regex {string} to substitute {string} with {string}")
    public void i_use_the_regex_to_substitute_with(String regex, String target, String substitute) {
        FileUtils.modifyRegexMatchInFile(testFile, regex, target, substitute);
    }

    @When("I use the regex {string} to search for matches in a file")
    public void i_use_the_regex_to_search_for_matches_in_a_file(String regex) throws IOException {
        match = FileUtils.hasRegExMatch(regex, testFile);

    }

    @Then("the match result should be \"{booleanValue}\"")
    public void the_match_result_should_be(Boolean expected) {
        assertEquals(expected, match, "RegEx file matcher did not return expected result");

    }
}
