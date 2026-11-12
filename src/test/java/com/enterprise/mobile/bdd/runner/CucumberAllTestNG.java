package com.enterprise.mobile.bdd.runner;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;

/**
 * Runs every Android scenario that can execute against the configured Android
 * application, in suite order: app launch, Meesho launch, sign-up and the
 * categories browsing cases. The long @e2e purchase journey runs separately
 * with {@code ./mvnw test -Pe2e} (or together via {@code -Pall-tests}).
 */
@CucumberOptions(
        features = {
                "src/test/resources/features/android/01-app-launch.feature",
                "src/test/resources/features/android/02-meesho-launch.feature",
                "src/test/resources/features/android/03-meesho-signup.feature",
                "src/test/resources/features/android/04-meesho-categories.feature",
                "src/test/resources/features/android/05-meesho-filters.feature",
                "src/test/resources/features/android/06-meesho-search.feature"
        },
        glue = "com.enterprise.mobile.bdd",
        plugin = {
                "pretty",
                "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm",
                "com.enterprise.mobile.bdd.plugin.StepLoggingPlugin"
        },
        tags = "not @settings and not @e2e"
)
public class CucumberAllTestNG extends AbstractTestNGCucumberTests {
}
