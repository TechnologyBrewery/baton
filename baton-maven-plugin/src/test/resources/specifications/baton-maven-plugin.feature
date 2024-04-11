@baton
Feature: Validate the definition of a migration target

  Scenario Outline: Group migrations can be defined and executed in their order
    Given groups "<order>" with migrations 
    When Baton executes
    Then groups are executed in the order "<order>"

    Examples:
       | order   |
       | A,C,B,D |
       | B,C,A,D |

  Scenario Outline: Manually ordered Migrations can be defined and executed in their order
    Given a group with type "ordered"
    And migrations "<order>" with implementations
    When Baton executes
    Then migrations are executed in the order "<order>"

    Examples:
      | order   |
      | A,C,D,B |
      | C,B,A,D |

  Scenario Outline: Versioned ordered migrations can be defined and executed in their order with a set minimum version
    Given a group with type "versioned"
    And migrations "<names>" with "<versions>" and implementations
    And minimum version "<minimumVersion>"
    When Baton executes
    Then migrations are executed in the order "<order>"

    Examples:
      | names   | versions                     | minimumVersion      | order     |
      | A,B,C,D | 4.0.0,2.0.0,3.0.0,1.0.0      | 0.0.0               | D,B,C,A   | 
      | A,B,C,D | 1.0.0,3.0.0,0.1.0,7.0.0      | 1.0.0               | A,B,D     |
      | A,B,C,D | 0.15.0,7.0.0,0.35.0,1.0.0    | 2.0.0               | B         |
      | A,B,C,D | 4.2.13,4.1.12,0.0.001,5.1.0  | 4.1.1               | B,A,D     |

  Scenario Outline: A migration can be deactivated via the plugin configuration
    Given a group with type "ordered"
    And migrations "<name>" with implementations
    And Baton is configured to ignore "<name>"
    When Baton executes
    Then no migration is performed

    Examples:
      | name                                          |
      | ignore-me-migration                           |
      | declared-somewhere-else-but-we-do-not-want-it |

  Scenario Outline: A mix of active and inactive migrations can be executed together
    Given a group with type "ordered"
    And migrations "Migration,<name>" with implementations
    And Baton is configured to ignore "<name>"
    When Baton executes
    Then a single migration is performed

    Examples:
      | name                                          |
      | ignore-me-migration                           |
      | declared-somewhere-else-but-we-do-not-want-it |