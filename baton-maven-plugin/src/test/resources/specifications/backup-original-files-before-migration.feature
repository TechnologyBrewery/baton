@backupOriginals
Feature: Support backing up original files prior to migrations for comparison and some added safety

  Scenario Outline: A file is backed up automatically to target for ease of tracking to the current build
    Given a file to migrate "<fileName>"
    When the migration backup is executed
    Then the file can be found in the project's target location

    Examples:
      | fileName  |
      | fileA.txt |
      | fileB.txt |

  Scenario Outline: A file is backed up automatically to system temp so it is not subjected to Maven's clean lifecycle
    Given a file to migrate "<fileName>"
    When the migration backup is executed
    Then the file can be found in the project's backup location

    Examples:
      | fileName  |
      | fileC.txt |
      | fileD.txt |

  Scenario Outline: Multiple copies of a file are available until it hits the backup limit
    Given a file to migrate "<fileName>"
    When the migration backup is executed <numberOfExecutions> and <backupLimit>
    Then one copy of the file can be found in the project's target location
    And <backupLimit> plus one copies of the file can be found in the project's backup location

    Examples:
      | fileName  | numberOfExecutions | backupLimit |
      | fileE.txt | 2                  | 1           |
      | fileF.txt | 3                  | 2           |
      | fileF.txt | 10                 | 9           |
      | fileG.txt | 5                  | 1           |
      | fileH.txt | 8                  | 2           |