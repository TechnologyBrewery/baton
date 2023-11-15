@migrationsConfig
Feature: Specify migration configuration files

  Scenario Outline: load a migrations file with only required fields
    Given a migration described by "<name>" and "<implementation>"
    When the configuration is read
    Then a valid migration is available as "<name>" with "<implementation>"

    Examples:
      | name       | implementation                                    |
      | migrateFoo | org.technologybrewery.baton.NoOpMigration         |
      | migrateBar | org.technologybrewery.baton.DoesNotExistMigration |

  Scenario Outline: load a migrations file with optional description field
    Given a migration with required fields as well as "<description>"
    When the configuration is read
    Then a valid migration is available with "<description>"

    Examples:
      | description                   |
      | This migration helps with ABC |
      | This migration helps with XYZ |

  Scenario Outline: load a migrations file with a file set
    Given a valid migration with the file set definition of "<directory>", "<includes>", "<excludes>", "<followSymLinks>"
    When the configuration is read
    Then a valid migration is available as "<directory>", "<includes>", "<excludes>", "<followSymLinks>"

    Examples:
      | directory                | includes     | excludes      | followSymLinks |
      | ./src/my-random-location |              |               |                |
      | ./src/main/resources     |              |               |                |
      |                          | **/*.foo     |               |                |
      |                          | *.toml, *.py |               |                |
      |                          |              | **/*          |                |
      |                          |              | *.xml, *.java |                |
      |                          |              |               | true           |
      |                          |              |               | false          |
      |                          |              |               |                |