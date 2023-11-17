package org.technologybrewery.baton;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.shared.model.fileset.FileSet;
import org.technologybrewery.baton.config.MigrationTarget;
import org.technologybrewery.commons.json.AbstractValidatedElement;
import org.technologybrewery.commons.json.ValidatedElement;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A Maven plugin that allows migration logic to be executed against a Maven module based on a classpath-provided
 * migrations.  These are intended to run every time the build runs such that they perform a migration or no-op
 * appropriately.
 */
@Mojo(name = "baton-migrate", defaultPhase = LifecyclePhase.VALIDATE, requiresDependencyResolution = ResolutionScope.COMPILE, threadSafe = true)
public class BatonMojo extends AbstractMojo {

    /**
     * Base directory in which to operate.
     */
    @Parameter(property = "baton.baseDirectory", required = true, defaultValue = "${project.basedir}")
    protected File baseDirectory;

    /**
     * Folder in which source files are located.
     */
    @Parameter(property = "baton.sourceDirectory", required = true, defaultValue = "${project.basedir}/src")
    protected File sourceDirectory;

    /**
     * Folder in which test files are located, if not already covered by sourceDirectory (e.g., with typical python
     * project standards).
     */
    @Parameter(property = "baton.testDirectory", required = false)
    protected File testDirectory;

    /**
     * A list of fileSet rules to select files and directories.  Will be defaulted based on project information
     * if not specified.
     */
    @Parameter
    protected FileSet[] fileSets;

    /**
     * The configurations file name to look for in the classpath (all matches will be used).  Defaults to
     * migrations.json.
     */
    @Parameter(property = "baton.migrationsConfigurationFile", required = false, defaultValue = "migrations.json")
    protected String migrationsFileName;

    @Parameter(property = "baton.deactivateMigrations", required = false)
    protected Set<String> deactivateMigrations;

    private final ObjectMapper objectMapper = initializeObjectMapper();

    private Map<String, MigrationTarget> migrations = new HashMap<>();

    protected ObjectMapper initializeObjectMapper() {
        ObjectMapper localObjectMapper = new ObjectMapper();

        SimpleModule module = new SimpleModule();
        module.addAbstractTypeMapping(ValidatedElement.class, MigrationTarget.class);

        localObjectMapper.registerModule(module);

        return localObjectMapper;
    }

    protected void defaultFileSets() {
        if (fileSets == null) {
            getLog().debug("Defining default file set...");

            List<FileSet> defaultFileSets = new ArrayList<>();
            // add source:
            FileSet sourceFileSet = new FileSet();
            sourceFileSet.setDirectory(sourceDirectory.getAbsolutePath());
            defaultFileSets.add(sourceFileSet);
            getLog().debug(String.format("\tAdded: %s", sourceFileSet.getDirectory()));

            // add test:
            if (testDirectory != null) {
                FileSet testFileSet = new FileSet();
                testFileSet.setDirectory(testDirectory.getAbsolutePath());
                defaultFileSets.add(testFileSet);
                getLog().debug(String.format("\tAdded: %s", testFileSet.getDirectory()));
            }

            // add common root files:
            FileSet rootFileSets = new FileSet();
            rootFileSets.setDirectory(baseDirectory.getAbsolutePath());
            rootFileSets.addInclude("pom.xml");
            rootFileSets.addInclude("*.toml");
            defaultFileSets.add(rootFileSets);
            getLog().debug(String.format("\tAdded: %s with includes: %s", rootFileSets.getDirectory(), rootFileSets.getIncludes()));

            fileSets = defaultFileSets.toArray(new FileSet[0]);

            getLog().debug("Default file set definition completed");
        }
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        loadMigrations();

        defaultFileSets();

        BatonExecutionSummary summary = performMigration(migrations.values());
        getLog().info(summary.getSummary());

    }

    BatonExecutionSummary performMigration(Collection<MigrationTarget> targets) {
        BatonExecutionSummary executionSummary = new BatonExecutionSummary();
        for (MigrationTarget target : targets) {
            if (isActive(target)) {
                try {
                    getLog().debug(String.format("Executing Migration: %s (%s)", target.getName(), target.getImplementation()));
                    Class<Migration> implementationClass = (Class<Migration>) Class.forName(target.getImplementation());
                    Constructor<Migration> constructor = implementationClass.getConstructor();
                    Migration migration = constructor.newInstance();
                    migration.setName(target.getName());
                    migration.setDescription(target.getDescription());
                    FileSet[] migrationSpecificFileSets = (CollectionUtils.isNotEmpty(target.getFileSets())) ? getFileSetsForTarget(target) : fileSets;
                    MigrationSummary migrationSummary = migration.execute(migrationSpecificFileSets);
                    executionSummary.addMigrationSummary(migrationSummary);

                } catch (Exception e) {
                    throw new BatonException("Could not complete migrations!", e);
                }
            }
        }

        return executionSummary;
    }

    protected boolean isActive(MigrationTarget migrationTarget) {
        boolean isActive = true;
        if (CollectionUtils.isNotEmpty(deactivateMigrations)) {
            String name = migrationTarget.getName();
            if (deactivateMigrations.contains(name)) {
                isActive = false;
                getLog().info(String.format("Skipping deactivated migration: %s", name));
            }
        }

        return isActive;
    }

    /**
     * Scans the classpath for any migrations.json files and loads all defined {@link MigrationTarget} configurations.
     */
    protected void loadMigrations() {
        Enumeration<URL> migrationsEnumeration = null;
        try {
            migrationsEnumeration = getClass().getClassLoader().getResources(migrationsFileName);

        } catch (IOException ioe) {
            throw new BatonException("Unable to find migrations!", ioe);
        }

        URL migrationsResource;
        while (migrationsEnumeration.hasMoreElements()) {
            migrationsResource = migrationsEnumeration.nextElement();
            getLog().info(String.format("Loading migrations from: %s", migrationsResource.toString()));

            try (InputStream migrationsStream = migrationsResource.openStream()) {
                File tempMigrationsFile = File.createTempFile("migrations", ".json");
                Files.copy(
                        migrationsStream,
                        tempMigrationsFile.toPath(),
                        StandardCopyOption.REPLACE_EXISTING);
                migrations = loadMigrationsJson(tempMigrationsFile, migrations);

                getLog().info(String.format("Found %d migrations", migrations.size()));
            } catch (IOException e) {
                throw new BatonException("Unable to parse " + migrationsFileName, e);
            }
        }

    }

    /**
     * Loads all {@link MigrationTarget}s contained within the given {@link InputStream}, which is expected to
     * reference the desired migrations.json file to load.
     *
     * @param migrationsFile   {@link File} referencing migrations.json file desired to load.
     * @param migrationTargets the migration targets already loaded to this point
     * @return {@link Map} containing all loaded {@link MigrationTarget}s with their corresponding name as the map key.
     */
    protected Map<String, MigrationTarget> loadMigrationsJson(File migrationsFile,
                                                              Map<String, MigrationTarget> migrationTargets) {

        List<MigrationTarget> loadedMigrations = AbstractValidatedElement.readAndValidateJsonList(migrationsFile,
                objectMapper, MigrationTarget.class);

        for (MigrationTarget migrationTarget : loadedMigrations) {
            migrationTargets.put(migrationTarget.getName(), migrationTarget);
        }

        return migrationTargets;
    }

    protected FileSet[] getFileSetsForTarget(MigrationTarget target) {
        List<FileSet> localFileSets = new ArrayList<>();
        for (org.technologybrewery.baton.config.FileSet targetFileSet : target.getFileSets()) {
            FileSet fileSet = new FileSet();
            fileSet.setDirectory(targetFileSet.getDirectory());
            if (fileSet.getDirectory() == null) {
                fileSet.setDirectory(baseDirectory.getAbsolutePath());
            }

            if (CollectionUtils.isNotEmpty(targetFileSet.getIncludes())) {
                for (String include : targetFileSet.getIncludes()) {
                    fileSet.addInclude(include);
                }
            }

            if (CollectionUtils.isNotEmpty(targetFileSet.getExcludes())) {
                for (String exclude : targetFileSet.getExcludes()) {
                    fileSet.addExclude(exclude);
                }
            }

            if (targetFileSet.getFollowSymlinks() != null) {
                fileSet.setFollowSymlinks(targetFileSet.getFollowSymlinks());
            }

            localFileSets.add(fileSet);
        }

        return (localFileSets.isEmpty()) ?  fileSets: localFileSets.toArray(new FileSet[0]);
    }
}
