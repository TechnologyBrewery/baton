@migrationsConfig
Feature: Specify migration configuration files

  Scenario Outline: load a migrations file with only required fields
    Given a group "<group>"
    And a migration described by "<name>" and "<implementation>" for group "<group>"
    When the configuration is read
    Then a valid migration is available as "<name>" with "<implementation>" for group "<group>"

    Examples:
    | group     | name       | implementation                                    |
    | Foo       | migrateFoo | org.technologybrewery.baton.NoOpMigration         |
    | Bar       | migrateBar | org.technologybrewery.baton.DoesNotExistMigration |

  Scenario Outline: load a migrations file with optional description field
    Given a group "<group>"
    And a migration with required fields as well as "<description>" for group "<group>"
    When the configuration is read
    Then a valid migration is available with "<description>" for group "<group>"

    Examples:
     | group | description                   |
     | Foo   | This migration helps with ABC |
     | Bar   | This migration helps with XYZ |

  Scenario Outline: load a migrations file with a file set
    Given a group "<group>"
    And a valid migration with the file set definition of "<directory>", "<includes>", "<excludes>", "<followSymLinks>" for group "<group>"
    When the configuration is read
    Then a valid migration is available as "<directory>", "<includes>", "<excludes>", "<followSymLinks>" for group "<group>"

    Examples:
    | group    | directory                | includes     | excludes      | followSymLinks |
    | Foo      | ./src/my-random-location |              |               |                |
    | Bar      | ./src/main/resources     |              |               |                |
    | Foo-Bar  |                          | **/*.foo     |               |                |
    | Bar-Foo  |                          | *.toml, *.py |               |                |
    | Foo-Foo  |                          |              | **/*          |                |
    | Bar-Bar  |                          |              | *.xml, *.java |                |
    | Group-1  |                          |              |               | true           |
    | Group-2  |                          |              |               | false          |
    | Group-3  |                          |              |               |                |