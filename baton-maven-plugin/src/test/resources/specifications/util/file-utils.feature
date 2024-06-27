@baton
Feature: Baton utilities can be used to assist with migration logic pertaining to files

  Scenario: I want to easily replace a string in a file using a literal expression
    Given I have a file containing the string "123abc456"
    When I use the literal "abc" to substitute "def"
    Then the file should now contain the string "123def456"

  Scenario: I want to easily replace a string in a file using a regex expression
    Given I have a file containing the string "123abc456"
    When I use the regex "(?<=123).*(?=456)" to substitute "def"
    Then the file should now contain the string "123def456"

  Scenario: I want to easily replace a substring of a string in a file using a regex expression
    Given I have a file containing the string "123abc456"
    When I use the regex "123.*456" to substitute "abc" with "def"
    Then the file should now contain the string "123def456"

  Scenario: I want to easily test if a string is present in a file using a regex expression
    Given I have a file containing the string "123abc456"
    When I use the regex "123.*456" to search for matches in a file
    Then the match result should be "true"

  Scenario: I want to easily be able to pull out specific substrings from a string using regex capture groups
    Given I have a string containing "123abc456"
    When I use the regex "123(.*)456" to retrieve capture groups
    Then the size of the retrieved capture groups should be "1"
    And the capture groups should include
      | abc |

  Scenario: I want to easily be able to pull out specific strings from a file using regex capture groups
    Given I have a file containing the string "123abc456"
    When I use the regex "(123).*(456)" to retrieve capture groups
    Then the size of the retrieved capture groups should be "2"
    And the capture groups should include
      | 123 |
      | 456 |

  Scenario: I want to easily retrieve the type of indexing being used in a string

  Scenario: I want to easily indent a group of strings with spaces

  Scenario: I want to easily retrieve the contents of a file

  Scenario: I want to easily write to a file
