/*
 * Apache 2.0 License
 *
 * Copyright (c) 2019 Intuit Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.intuit.cloudraider.cucumber.steps;

import com.intuit.cloudraider.core.interfaces.Route53Raider;
import com.intuit.cloudraider.cucumber.model.ExecutionStateCache;
import com.intuit.cloudraider.cucumber.util.DNSLookup;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;

/**
 * Cucumber Step Definitions for AWS Route 53 functionality.
 */
public class Route53StepDefinitions {

    @Autowired
    private ExecutionStateCache executionStateCache;

    @Autowired
    @Qualifier("r53RaiderBean")
    private Route53Raider route53Raider;

    /**
     * The Logger.
     */
    Logger logger = LoggerFactory.getLogger(this.getClass());


    private String r53HealthCheckId;
    private String r53PolicyName;

    /**
     * Gets execution state cache.
     *
     * @return the execution state cache
     */
    public ExecutionStateCache getExecutionStateCache() {
        return executionStateCache;
    }

    /**
     * Sets execution state cache.
     *
     * @param executionStateCache the execution state cache
     */
    public void setExecutionStateCache(ExecutionStateCache executionStateCache) {
        this.executionStateCache = executionStateCache;
    }

    /**
     * Instantiates a new Route 53 step definitions.
     */
    public Route53StepDefinitions() {

    }

    /**
     * Asserts that the r53 failover from the primary endpoint to the secondary endpoint succeeds
     *
     * @param primary   endpoint
     * @param secondary RegEx or endpoint
     * @throws Throwable the throwable
     */
    @Then("^assertTrue R53 failover from \"([^\"]*)\" to \"([^\"]*)\"$")
    public void assertTrueR53Failover(String primary, String secondary) throws Throwable {
        if (!isRegex(secondary)) {
            Assert.assertTrue("DNS Failover Check succeeded", DNSLookup.hostNameLookup(primary).equalsIgnoreCase(secondary));
        } else {
            Assert.assertTrue("DNS Failover Check succeeded", DNSLookup.hostNameLookup(primary).matches(secondary));
        }
    }

    /**
     * Asserts that the r53 failover from the primary endpoint to the secondary endpoint fails
     *
     * @param primary   endpoint
     * @param secondary RegEx or endpoint
     * @throws Throwable the throwable
     */
    @Then("^assertFalse R53 failover from \"([^\"]*)\" to \"([^\"]*)\"$")
    public void assertFalseR53Failover(String primary, String secondary) throws Throwable {
        if (!isRegex(secondary)) {
            Assert.assertFalse("DNS Failover Check failed", DNSLookup.hostNameLookup(primary).equalsIgnoreCase(secondary));
        } else {
            Assert.assertFalse("DNS Failover Check failed", DNSLookup.hostNameLookup(primary).matches(secondary));
        }
    }

    /**
     * Sets the r53 health check id.
     *
     * @param r53HealthCheckId health check id
     * @return the route 53 step definitions
     */
    @Given("^R53 Healthcheck ID \"([^\"]*)\"$")
    public Route53StepDefinitions givenR53HealthCheckID(String r53HealthCheckId) {
        this.r53HealthCheckId = r53HealthCheckId;
        return this;
    }

    /**
     * Assert that the previously set health check id has the same status as the expected.
     *
     * @param expectedState expected state
     */
    @Then("^assertR53 HealthCheck state = \"([^\"]*)\"$")
    public void assertCloudWatchAlarmState(String expectedState) {
        logger.info("Route53 healtch check for id: "+ r53HealthCheckId+ " = " + route53Raider.getSimpleHealthCheckerStatus(r53HealthCheckId));
        org.testng.Assert.assertEquals(route53Raider.getSimpleHealthCheckerStatus(r53HealthCheckId).toString(), expectedState);
    }

    /**
     * Assert that the provided health check id has the same status as the expected.
     *
     * @param route53HealthCheckId health check id
     * @param expectedState        expected state
     */
    @Then("^assertR53 HealthCheckId \"([^\"]*)\" with state = \"([^\"]*)\"$")
    public void assertCloudWatchAlarmState(String route53HealthCheckId, String expectedState) {
        logger.info("Route53 healtch check for id: "+ r53HealthCheckId+ " = " + route53Raider.getSimpleHealthCheckerStatus(r53HealthCheckId));
        logger.info("Route53 healtch check for id: "+ r53HealthCheckId+ " = " + route53Raider.getSimpleHealthCheckerStatus(r53HealthCheckId));
        org.testng.Assert.assertEquals(route53Raider.getSimpleHealthCheckerStatus(route53HealthCheckId).toString(), expectedState);
    }

    /**
     * Sets the r53 policy name to the one specified.
     *
     * @param r53PolicyName policy name
     * @return the route 53 step definitions
     * @throws Throwable the throwable
     */
    @Given("^R53 traffic policy name \"([^\"]*)\"$")
    public Route53StepDefinitions giveR53TrafficPolicyName(String r53PolicyName) throws Throwable {
        this.r53PolicyName = r53PolicyName;
        return this;
    }

    /**
     * Updates the r53 traffic policy version to the one specified.
     *
     * @param version version number
     * @throws Throwable the throwable
     */
    @When("^R53 update traffic policy version to (\\d+)$")
    public void updateR53PolicyVersion(int version) throws Throwable {
        // Write code here that turns the phrase above into concrete actions
        route53Raider.updateTrafficPolicy(r53PolicyName, version);
    }

    /**
     * Assert that the r53 traffic policy version matches the one specified.
     *
     * @param expectedVersion version number
     * @throws Throwable the throwable
     */
    @Then("^assertR53 traffic policy versionId = (\\d+)$")
    public void assertR53VersionId(int expectedVersion) throws Throwable {
        org.testng.Assert.assertEquals(route53Raider.getTrafficPolicyVersion(r53PolicyName), expectedVersion);
    }

    /**
     * Updates the r53 health check regions to only the ones specified.
     *
     * @param regions regions to update
     * @throws Throwable the throwable
     */
    @When("^R53 update HealthCheck regions to \"([^\"]*)\"$")
    public void updatR53HealthCheckRegions(String regions) throws Throwable {
        List<String> regionsList = Arrays.asList(regions.split("\\s*,\\s*"));
        route53Raider.updateHealthCheckRegions(r53HealthCheckId, regionsList);
    }

    /**
     * Assert that the r53 health check regions include the ones specified.
     *
     * @param regions regions to check; separated by "," (comma)
     * @throws Throwable the throwable
     */
    @Then("^assertR53 HealthCheck regions = \"([^\"]*)\"$")
    public void assertR53HealthCheckRegions(String regions) throws Throwable {
        // Write code here that turns the phrase above into concrete actions
        List<String> expectedRegionsList = Arrays.asList(regions.split("\\s*,\\s*"));
        assertThat(route53Raider.getHealthCheckRegions(r53HealthCheckId), containsInAnyOrder(expectedRegionsList.toArray()));

    }

    /**
     * Resets the R53 health check to the default regions
     *
     * @throws Throwable the throwable
     */
    @When("^R53 reset HealthCheck to default regions$")
    public void resetR53HealthCheckRegions() throws Throwable {
        route53Raider.updateHealthCheckToDefaultRegions(r53HealthCheckId);
    }

    /**
     * Checks if the string is a regular expression
     *
     * @param str string to analyze
     * @return true if the string is a regular expression
     */
    private boolean isRegex(final String str) {
        try {
            java.util.regex.Pattern.compile(str);
            return true;
        } catch (java.util.regex.PatternSyntaxException e) {
            return false;
        }
    }
}
