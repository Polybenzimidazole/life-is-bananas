Feature: Recipe management follows the business rules
  A bio-engineer may test and approve recipes only in the intended order.

  Background:
    Given the test data has been initialized

  Scenario: Complete a recipe before testing
    Given the bio-engineer is signed in
    When the bio-engineer completes "Banana Chips Formula" for testing with "300" "GRAM" of "Banana"
    Then "Banana Chips Formula" is tested and has 2 ingredients
    And it contains "300" "GRAM" of "Banana"

  Scenario: Testing cannot be skipped
    Given the bio-engineer is signed in
    When the bio-engineer tries to approve "Banana Chips Formula" without testing
    Then "Banana Chips Formula" remains in development
    And the system refuses to skip the testing phase

  Scenario: Unknown recipes are not shown in search results
    Given the bio-engineer is signed in
    When the bio-engineer searches for recipes containing "Chocolate"
    Then no recipes are found

  Scenario: An incorrect password does not grant access
    When someone attempts to sign in with an incorrect password
    Then access to recipe management is refused
