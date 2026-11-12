package com.enterprise.mobile.bdd.runner;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;

/**
 * Parallel suite: same Android happy path as {@link CucumberAndroidTestNG},
 * bound to a suite that runs scenarios across registered devices.
 */
@CucumberOptions(
        features = "src/test/resources/features/android",
        glue = "com.enterprise.mobile.bdd",
        plugin = {
                "pretty",
                "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm",
                "com.enterprise.mobile.bdd.plugin.StepLoggingPlugin"
        },
        tags = "not @settings and not @signup"
)
public class CucumberParallelTestNG extends AbstractTestNGCucumberTests {
}
