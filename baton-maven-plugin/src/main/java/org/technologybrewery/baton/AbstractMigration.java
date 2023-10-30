package org.technologybrewery.baton;

import org.apache.maven.shared.model.fileset.FileSet;
import org.apache.maven.shared.model.fileset.util.FileSetManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Set;

/**
 * Common migration logic to make it easier to implement custom migrations.
 */
public abstract class AbstractMigration implements Migration {

    private static final Logger logger = LoggerFactory.getLogger(AbstractMigration.class);

    protected String name;

    protected String description;

    //TODO: update json schema to support deactivating:
    protected boolean active = true;

    protected Set<String> fileNamePatterns;

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
                    try {
                        performMigration(fileToMigrate);
                        successfulMigrations++;

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
}
