@android @smoke @regression
Feature: Application launch
  Smoke test - the configured Android application launches and reaches its main screen.

  Scenario: The configured application launches and reaches its main screen
    Then the configured application should be in the foreground
