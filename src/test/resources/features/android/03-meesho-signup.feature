@android @meesho @signup
Feature: Meesho Supply sign-up
  Account tab -> Sign up -> Continue, using the configured phone number.

  Scenario: Open the sign-up flow and continue
    When the user opens the account tab
    Then the account screen should be displayed
    When the user taps Sign up
    Then the sign-up step should be displayed
    When the user enters the configured phone number
    And the user taps Continue
