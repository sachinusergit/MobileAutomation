package com.enterprise.mobile.bdd.runner;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;

/**
 * Runs the Android happy-path smoke/regression scenarios (app launch +
 * Meesho launch). Settings scenarios are tagged {@code @settings} and
 * sign-up scenarios {@code @signup}.
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
public class CucumberAndroidTestNG extends AbstractTestNGCucumberTests {
}
