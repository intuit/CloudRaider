
package com.intuit.cloudraider.client.cucumber;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import cucumber.api.testng.AbstractTestNGCucumberTests;
import org.junit.runner.RunWith;


/*
 * This is a TestNG wrapper test to execute Gherkin/Cucumber files by providing cucumber options
 *  Change tags "@YourFeature" to point to correct tag.
 */

/**
 * The type Application load balancer cuke test.
 */
@RunWith(Cucumber.class)
@CucumberOptions(
        monochrome = true,
        features = "src/test/features",
        plugin = {"pretty", "html:target/surefire-reports/cucumber-html-report", "json:target/surefire-reports/cucumber-results.json", "junit:target/surefire-reports/cucumber-results.xml"},
        tags = {"@YourFeature"},
        glue = "com.intuit.cloudraider.cucumber.steps"
)
public class ApplicationLoadBalancerCukeTest extends AbstractTestNGCucumberTests {

}