Feature: Validate the definition of a migration target

  Scenario: A simple migration can be defined and executed
    Given a migration target with a name and a implementation
    When Baton executes
    Then a single migration is performed

  Scenario Outline: A migration can be deactivated via the plugin configuration
    Given a migration target with a "<name>" and a implementation
    And Baton is configured to ignore "<name>"
    When Baton executes
    Then no migration is performed

    Examples:
      | name                                          |
      | ignore-me-migration                           |
      | declared-somewhere-else-but-we-do-not-want-it |

  Scenario Outline: A mix of active and inactive migrations can be executed together
    Given a migration target with a name and a implementation
    And a migration target with a "<name>" and a implementation
    And Baton is configured to ignore "<name>"
    When Baton executes
    Then a single migration is performed

    Examples:
      | name                                          |
      | ignore-me-migration                           |
      | declared-somewhere-else-but-we-do-not-want-it |