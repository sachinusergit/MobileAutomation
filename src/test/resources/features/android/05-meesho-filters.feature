@android @meesho @filters
Feature: Meesho Supply home page filters
  On the Home page, apply Gender, Discount, Price and Color filters from the
  filter panel and confirm them with the Done button.

  Scenario: Apply Gender, Discount, Price and Color filters from the home page
    Given the Meesho Supply app is launched and ready
    And the footer Home tab should be the active tab
    When the user taps the Filters option below the search bar on the right
    Then the filter panel should be displayed
    When the user opens the Gender section and selects Men
    And the user opens the Discount section and selects 50% And Above
    And the user opens the Price section and selects Under Rs 499
    And the user opens the Color section and selects the available color
    Then the selected filters should be applied to the home page
    When the user taps the Done button
