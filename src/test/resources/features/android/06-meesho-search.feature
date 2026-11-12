@android @meesho @search
Feature: Meesho Supply home search bar
  On the home page, search "Men shirts" and select the fifth autosuggestion.

  Scenario: Search for Men shirts and select the fifth suggestion
    Given the Meesho Supply app is launched and ready
    And the footer Home tab should be the active tab
    When the user taps the search bar on the home page
    And the user clears the search bar and types "Men shirts"
    Then the autosuggestions should be displayed
    And the user reads the autosuggestions
    When the user selects the fifth autosuggestion
    Then the search results should be displayed for the selection
