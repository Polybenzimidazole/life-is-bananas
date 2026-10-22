Feature: Recipe management follows the business rules
  A bio-engineer may test and approve recipes only in the intended order.

  Background:
    Given the test data has been initialized

  Scenario: Complete a recipe before testing
    Given the bio-engineer is signed in
    When the bio-engineer completes a recipe for testing by adding an ingredient
    Then the recipe is tested
    And the added ingredient is included in the recipe

  Scenario: Testing cannot be skipped
    Given the bio-engineer is signed in
    When the bio-engineer tries to approve a recipe without testing
    Then the recipe remains in development
    And the system refuses to skip the testing phase

  Scenario: Unknown recipes are not shown in search results
    Given the bio-engineer is signed in
    When the bio-engineer searches for a recipe that does not exist
    Then no recipes are found

  Scenario: An incorrect password does not grant access
    Given the bio-engineer is not signed in
    When someone attempts to sign in with an incorrect password
    Then access to recipe management is refused
