package org.technologybrewery.baton;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OriginalBackupSteps {

    private List<String> fileNamesToMigrate = new ArrayList<>();
    private List<File> filesToMigrate = new ArrayList<>();

    private File targetDirectory = new File("./target/filesToMigrate");

    private File customBackupLocation = new File(BackupFileTestMigration.CUSTOM_BACKUP_LOCATION);

    @Before("@backupOriginals")
    public void cleanUp() throws Exception {
        if (targetDirectory.exists()) {
            FileUtils.forceDelete(targetDirectory);
        }

        if (customBackupLocation.exists()) {
            FileUtils.forceDelete(customBackupLocation);
        }

        fileNamesToMigrate.clear();
        filesToMigrate.clear();
    }

    @Given("a file to migrate {string}")
    public void a_file_to_migrate(String fileToMigrate) throws Exception {
        fileNamesToMigrate.add(fileToMigrate);
        createFiles();
    }

    @When("the migration backup is executed")
    public void the_migration_backup_is_executed() {
        executeMigration(1,1);
    }

    @When("the migration backup is executed {int} and {int}")
    public void the_migration_backup_is_executed(Integer numberOfExecutions, Integer numberOfMigrationsToKeep) {
        executeMigration(numberOfExecutions, numberOfMigrationsToKeep);
    }
    @Then("one copy of the file can be found in the project's target location")
    public void one_copy_of_the_file_can_be_found_in_the_project_s_target_location() {
        verifyExpectedFilesFound(targetDirectory, 1);
    }
    @Then("{int} plus one copies of the file can be found in the project's backup location")
    public void plus_one_copies_of_the_file_can_be_found_in_the_project_s_backup_location(Integer expectedBackups) {
        //expected backups represents the files already rolled over, not including the current backup:
        verifyExpectedFilesFound(customBackupLocation, expectedBackups + 1);
    }


    @Then("the file can be found in the project's target location")
    public void the_file_can_be_found_in_the_project_s_target_location() throws Exception {
        verifyExpectedFilesFound(targetDirectory, 1);
    }

    @Then("the file can be found in the project's backup location")
    public void the_file_can_be_found_in_the_project_s_backup_location() {
        verifyExpectedFilesFound(customBackupLocation, 1);
    }

    protected void createFiles() throws Exception {
        File sourceFile = new File("./pom.xml");
        FileUtils.createParentDirectories(targetDirectory);
        for (String filesName : fileNamesToMigrate) {
            File targetFile = new File(targetDirectory, filesName);
            FileUtils.copyFile(sourceFile, targetFile);
            filesToMigrate.add(targetFile);
        }
    }

    private void executeMigration(int numberOfExecutions, int numberOfBackupFilesToKeep) {
        Migration backupTestMigration = new BackupFileTestMigration(filesToMigrate, numberOfBackupFilesToKeep);
        for (int i = 0; i < numberOfExecutions; i++) {
            backupTestMigration.execute(null);
        }
    }

    protected void verifyExpectedFilesFound(File backupDirectory, int expectedSize) {
        Collection<File> backedUpFiles = FileUtils.listFiles(backupDirectory, FileFilterUtils.trueFileFilter(), FileFilterUtils.trueFileFilter());
        assertEquals(expectedSize, backedUpFiles.size(), "Unexpected number of migrated files found!");

        for (File foundFileName : backedUpFiles) {
            String fileNameToMatch = foundFileName.getName();
            if (fileNameToMatch.contains(".orig")) {
                fileNameToMatch = fileNameToMatch.substring(0, fileNameToMatch.lastIndexOf(".orig"));
            }
            boolean foundMatch = fileNamesToMigrate.contains(fileNameToMatch);
            assertTrue(foundMatch, "Did not find an expected file for " + foundFileName);
        }
    }

}
