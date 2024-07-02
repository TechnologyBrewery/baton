@baton
Feature: Baton utilities can be used to assist with migration logic pertaining to Maven POM files
  Scenario: I can leverage POM helper utilities to delete an element from a pom file
    Given a pom file with existing elements
    When I write a deletion for an existing element
    Then the existing element should not be present in the pom file

  Scenario: I can leverage POM helper utilities to replace an element in a pom file
    Given a pom file with existing elements
    When I write a replacement for an existing element
    Then the new element should be present in the pom file in place of the existing element

  Scenario: I can leverage POM helper utilities to insert an element in a pom file
    Given a pom file with existing elements
    When I write an insertion for a new element
    Then the new element should be present in the pom file

  Scenario: I can leverage POM helper utilities to write to a POM file
