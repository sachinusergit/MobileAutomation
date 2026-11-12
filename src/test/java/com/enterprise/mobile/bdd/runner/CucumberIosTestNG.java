package com.enterprise.mobile.bdd.runner;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;

/**
 * Runs the iOS scenarios (Settings smoke).
 */
@CucumberOptions(
        features = "src/test/resources/features/ios",
        glue = "com.enterprise.mobile.bdd",
        plugin = {
                "pretty",
                "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm",
                "com.enterprise.mobile.bdd.plugin.StepLoggingPlugin"
        },
        tags = "@ios"
)
public class CucumberIosTestNG extends AbstractTestNGCucumberTests {
}
