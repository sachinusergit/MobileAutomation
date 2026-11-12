package com.enterprise.mobile.bdd.runner;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;

/**
 * Runs the full Meesho Supply end-to-end purchase journey against the OPPO A54
 * (dev environment). Uses {@code @e2e} scenarios only.
 */
@CucumberOptions(
        features = "src/test/resources/features/android",
        glue = "com.enterprise.mobile.bdd",
        plugin = {
                "pretty",
                "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm",
                "com.enterprise.mobile.bdd.plugin.StepLoggingPlugin"
        },
        tags = "@e2e and @android"
)
public class CucumberE2ETestNG extends AbstractTestNGCucumberTests {
}
