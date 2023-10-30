Feature: Validate the definition of a migration target

  Scenario: A simple migration can be defined and executed
    Given a migration target with a name and and implementation
    When Baton executes
    Then a single migration is performed
