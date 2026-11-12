@ios @smoke
Feature: iOS Settings navigation
  Reference implementation: the built-in iOS Settings app demonstrates XCUITest page objects.

  Scenario: Search settings
    Given the iOS Settings home screen is displayed
    When the user searches for "General" in settings
    Then the settings results should contain "General"

  Scenario: Open General settings
    Given the iOS Settings home screen is displayed
    When the user opens General settings
    Then the General settings screen should be displayed
