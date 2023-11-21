package org.technologybrewery.baton;

import com.vlkan.rfos.RotatingFileOutputStream;
import com.vlkan.rfos.RotationConfig;
import com.vlkan.rfos.policy.SizeBasedRotationPolicy;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.model.fileset.FileSet;
import org.apache.maven.shared.model.fileset.util.FileSetManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

/**
 * Common migration logic to make it easier to implement custom migrations.
 */
public abstract class AbstractMigration implements Migration {

    private static final Logger logger = LoggerFactory.getLogger(AbstractMigration.class);

    protected String name;

    protected String description;

    protected boolean active = true;

    protected Set<String> fileNamePatterns;

    protected boolean backupOriginalMigratedFiles;

    protected String backupCustomLocation;

    protected int numberOfBackupsToKeep;

    /**
     * Enables access to the runtime properties associated with the project's POM
     * configuration against which Baton is being executed.
     */
    protected MavenProject project;

    /**
     * If active, validates applicability of the migration each targeted file and then performs the migration.  Tracks
     * the number of files successfully and unsuccessfully changed during the process.
     *
     * @param fileSets the file sets to process with this migration
     * @return a summary of results
     */
    @Override
    public MigrationSummary execute(FileSet[] fileSets) {
        if (!active) {
            logger.info("Migration {}' is marked inactive - skipping", getName());
            return new MigrationSummary(0, 0);
        }

        FileSetManager fileSetManager = new FileSetManager();
        int successfulMigrations = 0;
        int unsuccessfulMigrations = 0;

        String[] includedFiles;
        for (FileSet fileSet : fileSets) {
            // excluded files are removed by the fileSetManager, so no need to explicitly process them:
            includedFiles = fileSetManager.getIncludedFiles(fileSet);

            for (String file : includedFiles) {
                File fileToMigrate = new File(fileSet.getDirectory(), file);
                if (shouldExecuteOnFile(fileToMigrate)) {

                    backupOriginalFile(fileToMigrate);

                    try {
                        boolean successful = performMigration(fileToMigrate);
                        if (successful) {
                            successfulMigrations++;
                        } else {
                            unsuccessfulMigrations++;
                        }

                    } catch (Exception e) {
                        unsuccessfulMigrations++;
                        logger.error(String.format("Problem performing migration on %s!", fileToMigrate.getAbsolutePath()), e);
                    }
                }
            }
        }

        return new MigrationSummary(successfulMigrations, unsuccessfulMigrations);
    }

    /**
     * Called to determine if this migration should be run on the passed file.
     *
     * @param file file to check
     * @return to execute or not
     */
    protected abstract boolean shouldExecuteOnFile(File file);


    /**
     * Called to perform the specific migration logic.
     *
     * @param file file to migrate
     * @return if successful or not
     */
    protected abstract boolean performMigration(File file);

    protected void backupOriginalFile(File originalFile) {
        if (shouldBackupMigratedOriginalFiles()) {
            try {
                String persistentBackupPathBase;
                if (StringUtils.isNotBlank(backupCustomLocation)) {
                    persistentBackupPathBase = getBasedir().getAbsolutePath() + "/" + backupCustomLocation;
                } else {
                    persistentBackupPathBase = FileUtils.getTempDirectoryPath();
                }

                String originalPathAsString = Paths.get(originalFile.toURI()).toString();
                String baseDirectoryFilePath = Paths.get(getBasedir().toURI()).toString();
                String localPath = originalPathAsString.replace(baseDirectoryFilePath, "");
                File backupFile = new File(persistentBackupPathBase, "baton/" + localPath + ".orig");
                FileUtils.createParentDirectories(backupFile);

                createBackupAndRotateAnyPriorVersions(originalFile, backupFile);

                createSymlinkInTargetToPersistentBackup(localPath, backupFile);

            } catch (IOException e) {
                throw new BatonException("Could not backup original file!", e);
            }
        }
    }

    private void createBackupAndRotateAnyPriorVersions(File originalFile, File backupFile) throws IOException {
        // Handles rotating files for use - we set the rotation size to be equal to the file's size to force
        // rotation on each migration.  This is needed b/c we are borrowing the rotation logic from a log
        // rotation library:
        RotationConfig config = RotationConfig
                .builder()
                .file(backupFile.getAbsoluteFile())
                .maxBackupCount(getNumberOfBacksUpsToKeep())
                .policy(new SizeBasedRotationPolicy(FileUtils.sizeOf(originalFile)))
                .build();

        try (RotatingFileOutputStream stream = new RotatingFileOutputStream(config)) {
            FileUtils.copyFile(originalFile, stream);
            logger.debug("Original copy backed up to: {}", backupFile.getAbsoluteFile());
        }
    }

    private void createSymlinkInTargetToPersistentBackup(String localPath, File backupFile) throws IOException {
        File symlinkBackupFile = new File(getBuildDir(), "baton" + localPath + ".orig");
        FileUtils.createParentDirectories(symlinkBackupFile);
        if (!symlinkBackupFile.exists()) {
            Path symlinkPath = Paths.get(symlinkBackupFile.toURI());
            Path backupFilePath = Paths.get(backupFile.toURI());
            Files.createSymbolicLink(symlinkPath, backupFilePath);
        }
    }

    protected File getBasedir() {
        return project.getBasedir();
    }

    protected String getBuildDir() {
        return project.getBuild().getDirectory();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Set<String> getFileNamePatterns() {
        return fileNamePatterns;
    }

    public void setFileNamePatterns(Set<String> fileNamePatterns) {
        this.fileNamePatterns = fileNamePatterns;
    }

    @Override
    public boolean shouldBackupMigratedOriginalFiles() {
        return backupOriginalMigratedFiles;
    }

    @Override
    public void setBackupMigratedOriginalFiles(boolean backupOriginalMigratedFiles) {
        this.backupOriginalMigratedFiles = backupOriginalMigratedFiles;
    }

    public boolean isBackupOriginalMigratedFiles() {
        return backupOriginalMigratedFiles;
    }

    public void setBackupOriginalMigratedFiles(boolean backupOriginalMigratedFiles) {
        this.backupOriginalMigratedFiles = backupOriginalMigratedFiles;
    }

    public String getBackupCustomLocation() {
        return backupCustomLocation;
    }

    public void setBackupCustomLocation(String backupCustomLocation) {
        this.backupCustomLocation = backupCustomLocation;
    }

    public int getNumberOfBacksUpsToKeep() {
        return Integer.valueOf(numberOfBackupsToKeep);
    }

    public void setNumberOfBacksUpsToKeep(int numberOfBacksUpsToKeep) {
        this.numberOfBackupsToKeep = numberOfBacksUpsToKeep;
    }

    public void setMavenProject(MavenProject project) {
        this.project = project;
    }

    public MavenProject getMavenProject() {
        return project;
    }
}
