package org.technologybrewery.baton.toml;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.file.FileConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.technologybrewery.baton.AbstractMigration;

import java.io.File;
import java.util.Optional;

public class TestTomlMigration extends AbstractMigration {

    private static final Logger logger = LoggerFactory.getLogger(TestTomlMigration.class);

    protected boolean containsEntry(File file, String entry)  {
        boolean entryFound = false;
        try (FileConfig tomlFileConfig = FileConfig.of(file)) {
            tomlFileConfig.load();

            Optional<Config> optionalEntry = tomlFileConfig.getOptional(entry);
            if (optionalEntry.isPresent()) {
                entryFound = true;
                logger.debug("Found `{}` in file: {}", entry, file.getAbsoluteFile());

            }
        }

        return entryFound;
    }

    @Override
    protected boolean shouldExecuteOnFile(File file) {
        return containsEntry(file, "tool.poetry.group.monorepo.dependencies");
    }

    @Override
    protected boolean performMigration(File file) {
        return true;
    }

}
