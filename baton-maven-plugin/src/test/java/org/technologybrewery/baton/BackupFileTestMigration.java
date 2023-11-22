package org.technologybrewery.baton;

import org.apache.maven.shared.model.fileset.FileSet;

import java.io.File;
import java.util.List;

public class BackupFileTestMigration extends AbstractMigration {

    public static final String CUSTOM_BACKUP_LOCATION = "./target/customBackupLocation";

    protected List<File> testFilesToMigrate;

    public BackupFileTestMigration(List<File> testFilesToMigrate, int numberOfBackupFilesToKeep) {
        this.testFilesToMigrate = testFilesToMigrate;
        this.backupCustomLocation = CUSTOM_BACKUP_LOCATION;
        this.backupOriginalMigratedFiles = true;
        setNumberOfBacksUpsToKeep(numberOfBackupFilesToKeep);
    }

    public MigrationSummary execute(FileSet[] fileSets) {
        for (File testFile : testFilesToMigrate) {
            backupOriginalFile(testFile);
        }

        return new MigrationSummary(testFilesToMigrate.size(), 0);
    }

    @Override
    protected File getBasedir() {
        return new File("./");
    }

    @Override
    protected String getBuildDir() {
        return "./target";
    }

    @Override
    protected boolean shouldExecuteOnFile(File file) {
        throw new UnsupportedOperationException("Cannot check if execution status - this migration is only used to backup files for test purposes!");
    }

    @Override
    protected boolean performMigration(File file) {
        throw new UnsupportedOperationException("Cannot perform migration - this migration is only used to backup files for test purposes!");
    }
}
