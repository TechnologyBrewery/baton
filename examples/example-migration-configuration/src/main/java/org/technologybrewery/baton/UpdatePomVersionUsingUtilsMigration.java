
package org.technologybrewery.baton;

import org.apache.maven.model.InputLocation;
import org.apache.maven.model.Model;
import org.apache.maven.project.MavenProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.technologybrewery.baton.util.CommonUtils;
import org.technologybrewery.baton.util.pom.PomHelper;
import org.technologybrewery.baton.util.pom.PomModifications;

import java.io.File;

public class UpdatePomVersionUsingUtilsMigration extends AbstractMigration {
    private static final Logger logger = LoggerFactory.getLogger(UpdatePomVersionUsingUtilsMigration.class);
    public static final String VERSION_XML_TAG = "version";

    String oldVersion;
    String currentVersion;

    @Override
    protected boolean shouldExecuteOnFile(File file) {
        logger.info("Performing pom file migration `{}` on file `{}` using utilities", getName(), file.getAbsoluteFile());

        boolean shouldExecute = false;

        MavenProject project = getMavenProject();
        currentVersion = project.getVersion();

        Model pomModel = PomHelper.getLocationAnnotatedModel(file);
        oldVersion = pomModel.getParent().getVersion();

        shouldExecute = CommonUtils.isLessThanVersion(oldVersion, currentVersion);

        return shouldExecute;
    }

    @Override
    protected boolean performMigration(File file) {
        PomModifications mods = getModifications(file, currentVersion, oldVersion);

        boolean migrationPerformed = PomHelper.writeModifications(file, mods.finalizeMods());
        reverseMigration(file); // For redemonstrating this example project only
        return migrationPerformed;
    }

    private static PomModifications getModifications(File file, String currentVersion, String oldVersion) {
        Model model = PomHelper.getLocationAnnotatedModel(file);
        InputLocation replaceStart = model.getParent().getLocation(VERSION_XML_TAG);
        InputLocation replaceEnd = PomHelper.incrementColumn(replaceStart, oldVersion.length());

        PomModifications.Replacement replacement = new PomModifications.Replacement(replaceStart, replaceEnd, currentVersion);

        PomModifications mods = new PomModifications();
        mods.add(replacement);
        return mods;
    }

    /**
     * For demonstration purposes - populates example project pom-version-update-with-utils-example
     * with a fresh test pom to demonstrate this migration example.
     */
    private void reverseMigration(File file) {
        logger.info ("Reversing pom file for example project");
        PomModifications mods = getModifications(file, oldVersion, currentVersion);
        boolean migrationReversed = PomHelper.writeModifications(file, mods.finalizeMods());
        if (!migrationReversed) {
            logger.error("Unable to reverse pom file migration for example");
        }
    }
}
