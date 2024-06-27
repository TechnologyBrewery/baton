package org.technologybrewery.baton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.technologybrewery.baton.util.FileUtils;

import java.io.File;

public class ReplaceRegexMatchUsingUtilsMigration extends AbstractMigration {
    private static final Logger logger = LoggerFactory.getLogger(ReplaceRegexMatchUsingUtilsMigration.class);
    private static final String JAVA_FILE_REGEX = "JavaFunctionToBeMigrated";
    private static final String NEW_JAVA_FILE_NAME = "MigratedJavaFunction";
    @Override
    protected boolean shouldExecuteOnFile(File file) {
        boolean shouldExecuteMigration = false;
        try {
            logger.info("Detecting migrations for {} to {}", JAVA_FILE_REGEX, NEW_JAVA_FILE_NAME);
            shouldExecuteMigration = FileUtils.hasRegExMatch(JAVA_FILE_REGEX, file);
        } catch (Exception e) {
            logger.error("Caught exception checking Java files for migration legibility", e);
        }

        return shouldExecuteMigration;
    }

    @Override
    protected boolean performMigration(File file) {
        boolean migrationPerformed = false;
        try {
            migrationPerformed = FileUtils.replaceInFile(file, JAVA_FILE_REGEX, NEW_JAVA_FILE_NAME);
            logger.info("Example migration for {} to {} performed successfully", JAVA_FILE_REGEX, NEW_JAVA_FILE_NAME);

            FileUtils.replaceInFile(file, NEW_JAVA_FILE_NAME, JAVA_FILE_REGEX);
            logger.info("Reversed {} Migration to {} for demonstration purposes", JAVA_FILE_REGEX, NEW_JAVA_FILE_NAME);
        } catch (Exception e) {
            logger.error("Caught exception migrating Java function name");
        }

        return migrationPerformed;
    }
}
