Feature: Manage a recipe

  As a bio-engineer
  I want to complete a recipe and move it through its lifecycle
  So that only tested recipes can be approved for production

  Background:
    Given the test data has been initialized

  Scenario: Add an ingredient to a recipe and mark it as tested
    Given I log in as "sarah.johnson@lifeisbananas.com" with password "banaan123"
    When I search for recipes with "Banana"
    Then I should see the recipe "Banana Chips Formula" in the search results
    When I open the recipe "Banana Chips Formula"
    And I add the ingredient "Banana" with quantity "300" and unit "GRAM"
    And I change the status to "TESTED"
    Then the recipe should have 2 ingredients
    And the recipe should contain the ingredient "Banana" with quantity "300" and unit "GRAM"
    And the recipe status should be "TESTED"

  Scenario: A recipe cannot skip the testing phase
    Given I log in as "sarah.johnson@lifeisbananas.com" with password "banaan123"
    When I search for recipes with "Banana"
    And I open the recipe "Banana Chips Formula"
    And I change the status to "APPROVED"
    Then I should see the error "Overgang van IN_DEVELOPMENT naar APPROVED is niet toegelaten"
    And the recipe status should be "IN_DEVELOPMENT"

  Scenario: Searching for a recipe that does not exist gives no results
    Given I log in as "sarah.johnson@lifeisbananas.com" with password "banaan123"
    When I search for recipes with "Chocolate"
    Then I should see no search results

  Scenario: Logging in with a wrong password is refused
    Given I try to log in as "sarah.johnson@lifeisbananas.com" with password "verkeerd"
    Then I should see the error "Onbekend e-mailadres of verkeerd wachtwoord"
