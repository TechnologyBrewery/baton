package org.technologybrewery.baton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class LoggingSuccessfulMigration extends AbstractMigration {

    private static final Logger logger = LoggerFactory.getLogger(LoggingSuccessfulMigration.class);

    @Override
    protected boolean shouldExecuteOnFile(File file) {
        return true;
    }

    @Override
    protected boolean performMigration(File file) {
        logger.info("Performing successful migration `{}` on file `{}`", getName(), file.getPath());
        return true;
    }

}
