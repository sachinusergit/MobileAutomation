@android @smoke @regression @meesho
Feature: Meesho Supply app launch
  The Meesho Supply app launches and the package is reported by the driver.

  Scenario: Meesho Supply launches into its home activity
    Then Meesho Supply should be in the foreground
    And the current package should match the configured application package
