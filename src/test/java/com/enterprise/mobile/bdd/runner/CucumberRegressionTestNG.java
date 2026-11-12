package com.enterprise.mobile.bdd.runner;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;

/**
 * Android regression suite: application launch + Meesho Supply launch.
 */
@CucumberOptions(
        features = "src/test/resources/features/android",
        glue = "com.enterprise.mobile.bdd",
        plugin = {
                "pretty",
                "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm",
                "com.enterprise.mobile.bdd.plugin.StepLoggingPlugin"
        },
        tags = "@regression and not @settings"
)
public class CucumberRegressionTestNG extends AbstractTestNGCucumberTests {
}
