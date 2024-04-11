@migrationsConfig
Feature: Specify migration configuration files

  Scenario Outline: load a migrations file with only required fields for manually ordered migrations
    Given a group "<group>" with type "<type>"
    And a migration described by "<name>" and "<implementation>" for group "<group>"
    When the configuration is read
    Then a valid migration is available as "<name>" with "<implementation>" for group "<group>" with type "<type>" 

    Examples:
    | group     | name       | implementation                                    | type        |
    | Foo       | migrateFoo | org.technologybrewery.baton.NoOpMigration         | ordered     |
    | Bar       | migrateBar | org.technologybrewery.baton.DoesNotExistMigration | ordered     |

  Scenario Outline: load a migrations file with only required fields for versioned ordered migrations
    Given a group "<group>" with type "<type>"
    And a migration described by "<name>","<implementation>", and "<version>" for group "<group>" 
    When the configuration is read
    Then a valid migration is available as "<name>","<implementation>", and "<version>" for group "<group>" with type "<type>"

    Examples:
    | group     | name       | implementation                                    | type          | version |
    | Foo       | migrateFoo | org.technologybrewery.baton.NoOpMigration         | versioned     | 3.0.1   |
    | Bar       | migrateBar | org.technologybrewery.baton.DoesNotExistMigration | versioned     | 2.3.4   |

  Scenario Outline: load a migrations file with optional description field
    Given a group "<group>" with type "ordered"
    And a migration with required fields as well as "<description>" for group "<group>"
    When the configuration is read
    Then a valid migration is available with "<description>" for group "<group>"

    Examples:
     | group | description                   |
     | Foo   | This migration helps with ABC |
     | Bar   | This migration helps with XYZ |

  Scenario Outline: load a migrations file with a file set
    Given a group "<group>" with type "ordered"
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