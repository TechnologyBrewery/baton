@baton
Feature: Validate the definition of a migration target

  Scenario: Group migrations can be defined and executed in their order
    Given groups A,B,C,D with migrations 
    When Baton executes
    Then groups are executed in the order they appear
    

  Scenario Outline: A migration can be deactivated via the plugin configuration
    Given a group
    And a migration target with a name "<name>" and a implementation
    And Baton is configured to ignore "<name>"
    When Baton executes
    Then no migration is performed

    Examples:
      | name                                          |
      | ignore-me-migration                           |
      | declared-somewhere-else-but-we-do-not-want-it |

  Scenario Outline: A mix of active and inactive migrations can be executed together
    Given a group
    And a migration target with a name "Migration" and a implementation
    And a migration target with a name "<name>" and a implementation
    And Baton is configured to ignore "<name>"
    When Baton executes
    Then a single migration is performed

    Examples:
      | name                                          |
      | ignore-me-migration                           |
      | declared-somewhere-else-but-we-do-not-want-it |