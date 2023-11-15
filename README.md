# Baton
[![Maven Central](https://img.shields.io/maven-central/v/org.technologybrewery.baton/baton.svg)](https://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22org.technologybrewery.baton%22%20AND%20a%3A%22baton%22)
[![License](https://img.shields.io/github/license/mashape/apistatus.svg)](https://opensource.org/licenses/mit)
[![Build (github)](https://github.com/TechnologyBrewery/baton/actions/workflows/maven.yaml/badge.svg)](https://github.com/TechnologyBrewery/baton/actions/workflows/maven.yaml)

Baton pays homage to [Burton Baton](https://dogfishalehouse.com/beers/burton-baton/), a fabulous beer that is a blend of 
a traditional English-style Old Ale and a modern imperial IPA. This "two thread" beer brings both source beers together 
into something arguably better than either beer was individually. Like the beer, this project blends your 
existing codebase with new migrations that helps modernize and keep your old code relevant. The result is 
more substantive and satisfying than either old or new code alone could accomplish.

## Why Do You Need Baton?
Baton is a Maven plugin that helps generically provide lightweight, convention-driven migration capabilities to make it 
easier to evolve your projects over time.  It makes it easy to embed automated migrations into your Maven build.
Additionally, by using classpath-driven configurations, version upgrades of your migration jars control what gets applied
when.  This makes it easy to release a set of artifacts that correspond to a general release of a framework and get just
the migration you need at the appropriate time.

## Requirements

In order to use Baton, the following prerequisites must be installed:

* Maven 3.9+
* Java 11+

## Usage
Creating and applying migrations is easy.  Follow these steps and you'll be migrating your code in no time.

### Create migration class
To start, implement your migration by extending AbstractMigration, adding your specific logic to:
1. Determine if the migration applies to a given file
2. If applicable, perform the migration

These steps are outlined below is a simple class that migrates files ending in `.foo` to now end in `.bar`:
```java
public class FooToBarMigration extends AbstractMigration {

    @Override
    protected boolean shouldExecuteOnFile(File file) {
        return file.getName().contains(".foo");
    }

    @Override
    protected boolean performMigration(File file) {
        FileUtils.moveFile(file, new File(file.getParent(),  file.getName().replace(".foo", ".bar")));
        return true;
    }
}
```

### Configure Baton to use the migration
With a migration to apply, we both configure and tailor that use through a simple json file.  This file can live anywhere
in Baton's classpath and is named `migrations.json` by default.

The following example configures the migration to only look in the `./src/main/resources/legacy` folder while leaving 
one file, `original-specification-example.foo` alone.
```json
[
  {
    "name": "upgrade-foo-extension-files-migration",
    "implementation": "org.technologybrewery.baton.example.FooToBarMigration",
    "fileSets": [
      {
        "includes": ["**/legacy/*.foo"],
        "excludes": ["original-specification-example.foo"]
      }
    ]
  }
]
```

### Add `baton-maven-plugin` to your Maven build
The last step is to add `baton-maven-plugin` to your Maven build process just like any other plugin.

The following example highlight the default plugin configuration as well as a notional dependency containing both the 
Migration class and configuration json file above.

```xml
                <plugin>
                    <groupId>org.technologybrewery.baton</groupId>
                    <artifactId>baton-maven-plugin</artifactId>
                    <version>0.1.0</version>
                    <extensions>true</extensions>
                    <executions>
                        <execution>
                            <id>default</id>
                            <goals>
                                <goal>baton-migrate</goal>
                            </goals>
                        </execution>
                    </executions>
                    <dependencies>
                        <dependency>
                            <groupId>org.technologybrewery.baton.example</groupId>
                            <artifactId>example-migration-configuration</artifactId>
                            <version>1.0.0</version>
                        </dependency>
                    </dependencies>
                </plugin>
```

### Example result output
When executing your next Maven build, Baton will execute as part of the `initialize` build lifecycle and output similar
to the following will result:

```bash
[INFO] --- baton:0.1.0:baton-migrate (default) @ toml-file-update ---
[INFO] Loading migrations from: jar:file:/Users/dfh/.m2/repository/org/technologybrewery/baton/example/example-migration-configuration/1.0.0/example-migration-configuration-1.0.0-SNAPSHOT.jar!/migrations.json
[INFO] Found 1 migrations
[INFO] Migrations Processed: 1, Successfully Migrated Files: 1, Unsuccessfully Migrated Files: 0
```

## Plugin Configuration
All Baton configurations may be set either via the `baton-maven-plugin`'s `<configuration>` definition, Maven POM 
properties, or `-D` on the command line and follow a consistent naming pattern for the different configuration 
approaches.  For setting configurations via POM properties or `-D` on the command line, all configuration keys may be 
prepended with `baton.`.  For example, `migrationsConfigurationFile` controls the file name that Baton will use to find 
migrations and may be configured using the following approaches:

1. Plugin `<configuration>`

```xml
	<plugin>
		<groupId>org.technologybrewery.baton</groupId>
		<artifactId>baton-maven-plugin</artifactId>
		<extensions>true</extensions>
		<configuration>
			<migrationsConfigurationFile>alternative-migrations.json</migrationsConfigurationFile>
		</configuration>
	</plugin>
```

2. `-D` via command line

```shell
mvn clean install -DmigrationsConfigurationFile=alternative-migrations.json
```

3. POM properties

```xml
	<properties>
		<migrationsConfigurationFile>alternative-migrations.json</migrationsConfigurationFile>
	</properties>
```

**NOTE:** The above list's order reflects the precedence in which configurations will be applied.  For example, 
configuration values that are specified in the plugin's `<configuration>` definition will always take precedence, while 
system properties via the command line (`-D`) will take precedence over `<properties>` definitions.

### baseDirectory
The desired base directory to use when Baton looks for files on which to perform migrations.  By default, only `pom.xml`
and `*.toml` from the base directory will be included.

Default: `${project.basedir}`

### sourceDirectory
The desired source directory to include when Baton looks for files on which to perform migrations.

Default: `${project.basedir}/src`

### testDirectory
The desired test directory to include, if desired, when Baton looks for files on which to perform migrations.

Default: None

### fileSets
A [standard Maven fileSets block](https://maven.apache.org/shared/file-management/examples/mojo.html) that can be used 
to include any sets of file you desire.  If used, no additional defaulting of fileSet values will be performed by Baton 
and *only* these filesets will be used for the plugin's execution.

**NOTE:** File set configurations specified in your `migrations.json` will override those specified to your plugin at
large.

Default: None

### migrationsConfigurationFile
The configurations file name to look for in the classpath (all matches will be used).

Default: `migrations.json`

## Migrations JSON File Configuration
When specifying your configurations in `migrations.json` or your custom `migrationsConfigurationFile` file name, the 
following options are available.

### name
The name of the migration to perform.  As of 0.1.0, `name` is not particularly impactful.  However, in subsequent 
releases it will gain importance as a means to order migration execution (convention-driven in the style of Flyway) as 
well as inactivate specific migrations.

Required? `true`

Default: None

### description
The description of the migration.  This is intended to provide context on why the migration is needed.

Required? `false`

Default: None

### implementation
The fully qualified Java class name that will perform the migration.  This **MUST** implement the 
`org.technologybrewery.baton.Migration` interface, however it is recommended that it extend 
`org.technologybrewery.baton.AbstractMigration` to allow implementations to be more consistent and focus on migration
logic rather than Baton plumbing.  The implementation **MUST** have a default constructor.

Required? `true`

Default: None

### fileSet
A Maven-inspired object that allows specification of common file sets.  **MUST** be added as a list item.

Required? `false`

Default: None

#### fileSet/directory
The directory on which this file set should operate.

Required? `false`

Default: `./` (project base directory)

#### fileSet/includes
A list of specific inclusions following [standard Maven conventions](https://maven.apache.org/shared/file-management/examples/mojo.html).

Required? `false`

Default: None

#### fileSet/excludes
A list of specific exclusions following [standard Maven conventions](https://maven.apache.org/shared/file-management/examples/mojo.html).

Required? `false`

Default: None

#### fileSet/followSymlinks
Whether to follow symlinks following [standard Maven conventions](https://maven.apache.org/shared/file-management/examples/mojo.html).

Required? `false`

Default: None

## Building Baton
If you are working on Baton, please be aware of some nuances in working with a plugin that defines a custom Maven build 
lifecycle and packaging. `examples` are utilized to immediately test the `baton-maven-plugin`. If `baton-maven-plugin` 
has not been previously built, developers must manually build the `baton-maven-plugin` and then execute another, separate 
build of `examples` (and any other baton module) to use the updated `baton-maven-plugin`. That said, once an initial 
build has been completed, a single build may be used to build `baton-maven-plugin` and apply the updates to `examples`. 
To assist, there are two profiles available in the build:

- `mvn clean install -Pbootstrap`: Builds the baton-maven-plugin such that it may be utilized within subsequent builds.
- `mvn clean install -Pdefault`: (ACTIVE BY DEFAULT - `-Pdefault` does not need to be specified) builds all modules. Developers may use this profile to build and apply changes to existing `baton-maven-plugin` Mojo classes.