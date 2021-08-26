Feature: mode select

  Background:
    Given The users opens the app

  Scenario: Create user with out setting name
    When The user clicks regular
    Then The user is still on the mode select page
    When The user clicks driving
    Then The user is still on the mode select page

  Scenario: Create user and select regular mode
    When The users enters the name "player1"
    And The user clicks regular
    Then The user is on the category select page
    And Sends the correct regular mode values

  Scenario: Create user and select driver mode
    When The users enters the name "player1"
    And The user clicks regular
    Then The user is on the category select page
    And Sends the correct driver mode values