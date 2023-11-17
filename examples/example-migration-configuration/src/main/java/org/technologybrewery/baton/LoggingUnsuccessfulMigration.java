package org.technologybrewery.baton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class LoggingUnsuccessfulMigration extends AbstractMigration {

    private static final Logger logger = LoggerFactory.getLogger(LoggingUnsuccessfulMigration.class);

    @Override
    protected boolean shouldExecuteOnFile(File file) {
        return true;
    }

    @Override
    protected boolean performMigration(File file) {
        logger.info("Performing unsuccessful migration `{}` on file `{}`", getName(), file.getAbsoluteFile());
        return false;
    }

}
