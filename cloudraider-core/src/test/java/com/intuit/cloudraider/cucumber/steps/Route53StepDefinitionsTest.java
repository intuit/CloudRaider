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

import com.intuit.cloudraider.commons.Route53Delegator;
import com.intuit.cloudraider.core.impl.Route53RaiderImpl;
import com.intuit.cloudraider.cucumber.model.ExecutionStateCache;
import com.intuit.cloudraider.cucumber.util.DNSLookup;
import com.intuit.cloudraider.model.BasicCredentials;
import com.intuit.cloudraider.model.Credentials;
import com.intuit.cloudraider.model.HealthCheckStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.net.UnknownHostException;


@PowerMockIgnore({"javax.management.*","javax.net.ssl.*"})
@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@PrepareForTest({Route53RaiderImpl.class, Route53StepDefinitions.class, DNSLookup.class})
public class Route53StepDefinitionsTest {


    @Autowired
    private Route53StepDefinitions route53StepDefinitions;

    @Autowired
    private Route53RaiderImpl route53Raider;

    @Autowired
    private ExecutionStateCache executionStateCache;

    private static String PRIMARY="primary.com";
    private static String SECONDARY="secondary.com";

    /**
     * Sets method.
     *
     * @throws Exception the exception
     */
    @Before
    public void setupMethod() throws Exception{

       executionStateCache.clear();
       PowerMockito.mockStatic(DNSLookup.class);
       PowerMockito.when(DNSLookup.hostNameLookup(PRIMARY)).thenReturn(SECONDARY);
    }

    /**
     * Test assert true r 53 failover.
     *
     * @throws Throwable the throwable
     */
    @Test
    public void testAssertTrueR53Failover() throws Throwable
    {

        route53StepDefinitions.assertTrueR53Failover(PRIMARY, SECONDARY);
    }

    /**
     * Test assert true r 53 failover invalid.
     *
     * @throws Throwable the throwable
     */
    @Test  (expected = AssertionError.class)
    public void testAssertTrueR53FailoverInvalid() throws Throwable
    {
        route53StepDefinitions.assertTrueR53Failover(PRIMARY, "\\");
    }


    /**
     * Test assert true r 53 failover unknown host.
     *
     * @throws Throwable the throwable
     */
    @Test  (expected = UnknownHostException.class)
    public void testAssertTrueR53FailoverUnknownHost() throws Throwable
    {
        PowerMockito.when(DNSLookup.hostNameLookup("primary")).thenThrow(new UnknownHostException());
        route53StepDefinitions.assertTrueR53Failover("primary", "secondary");
    }


    /**
     * Test assert true r 53 failover mismatch host.
     *
     * @throws Throwable the throwable
     */
    @Test  (expected = AssertionError.class)
    public void testAssertTrueR53FailoverMismatchHost() throws Throwable
    {
        route53StepDefinitions.assertTrueR53Failover(PRIMARY, "(([^/])+)?third.com.");
    }


    /**
     * Test assert false r 53 failover.
     *
     * @throws Throwable the throwable
     */
    @Test
    public void testAssertFalseR53Failover() throws Throwable
    {
        route53StepDefinitions.assertFalseR53Failover(PRIMARY, "(([^/])+)?blah.com.");

    }

    /**
     * Test assert false r 53 failover invalid.
     *
     * @throws Throwable the throwable
     */
    @Test
    public void testAssertFalseR53FailoverInvalid() throws Throwable
    {
        route53StepDefinitions.assertFalseR53Failover(PRIMARY, "");
    }

    /**
     * Test assert false r 53 failover unknown host.
     *
     * @throws Throwable the throwable
     */
    @Test  (expected = UnknownHostException.class)
    public void testAssertFalseR53FailoverUnknownHost() throws Throwable
    {
        PowerMockito.when(DNSLookup.hostNameLookup("primary")).thenThrow(new UnknownHostException());
        route53StepDefinitions.assertFalseR53Failover("primary", "secondary");
    }


    /**
     * Test assert false r 53 failover mismatch host.
     *
     * @throws Throwable the throwable
     */
    @Test  (expected = AssertionError.class)
    public void testAssertFalseR53FailoverMismatchHost() throws Throwable
    {
        route53StepDefinitions.assertFalseR53Failover(PRIMARY, "(([^/])+)?.com");
    }


    /**
     * Test given r 53 health check id.
     *
     * @throws Throwable the throwable
     */
    @Test
    public void testGivenR53HealthCheckID() throws Throwable
    {
        route53StepDefinitions.givenR53HealthCheckID("r53HealthCheckId");
    }

    /**
     * Test assert cloud watch alarm state no check id.
     *
     * @throws Throwable the throwable
     */
    @Test
    public void testAssertCloudWatchAlarmStateNoCheckId() throws Throwable
    {
        route53StepDefinitions.givenR53HealthCheckID("r53HealthCheckId");
        PowerMockito.when(route53Raider.getSimpleHealthCheckerStatus("r53HealthCheckId")).thenReturn(HealthCheckStatus.SUCCESS);
        route53StepDefinitions.assertCloudWatchAlarmState("SUCCESS");
    }

    /**
     * Test assert cloud watch alarm state.
     *
     * @throws Throwable the throwable
     */
    @Test
    public void testAssertCloudWatchAlarmState() throws Throwable
    {
        PowerMockito.when(route53Raider.getSimpleHealthCheckerStatus("r53HealthCheckId")).thenReturn(HealthCheckStatus.SUCCESS);
        route53StepDefinitions.assertCloudWatchAlarmState("r53HealthCheckId", "SUCCESS");
    }

    /**
     * Test given r 53 traffic policy name.
     *
     * @throws Throwable the throwable
     */
    @Test
    public void testGivenR53TrafficPolicyName() throws Throwable
    {
        route53StepDefinitions.giveR53TrafficPolicyName("r53PolicyName");
    }

    /**
     * Test update r 53 policy version.
     *
     * @throws Throwable the throwable
     */
    @Test
    public void testUpdateR53PolicyVersion() throws Throwable
    {
        route53StepDefinitions.giveR53TrafficPolicyName("r53PolicyName");
        route53StepDefinitions.updateR53PolicyVersion(2);
    }

    /**
     * Test assert r 53 version id.
     *
     * @throws Throwable the throwable
     */
    @Test
    public void testAssertR53VersionId() throws Throwable
    {
        route53StepDefinitions.giveR53TrafficPolicyName("r53PolicyName");
        PowerMockito.when(route53Raider.getTrafficPolicyVersion("r53PolicyName")).thenReturn(2);
        route53StepDefinitions.assertR53VersionId(2);
    }

    /**
     * Test updat r 53 health check regions.
     *
     * @throws Throwable the throwable
     */
    @Test
    public void testUpdatR53HealthCheckRegions() throws Throwable
    {
        route53StepDefinitions.givenR53HealthCheckID("r53HealthCheckId");
        route53StepDefinitions.updatR53HealthCheckRegions("sa-east-1, us-west-1");
    }

    /**
     * Test reset r 53 health check regions.
     *
     * @throws Throwable the throwable
     */
    @Test
    public void testResetR53HealthCheckRegions() throws Throwable
    {
        route53StepDefinitions.givenR53HealthCheckID("r53HealthCheckId");
        route53StepDefinitions.resetR53HealthCheckRegions();
    }


    /**
     * The type R 53 step definition test context configuration.
     */
    @Configuration
    protected static class R53StepDefinitionTestContextConfiguration {


        /**
         * Route 53 delegator route 53 delegator.
         *
         * @return the route 53 delegator
         */
        @Bean
        public Route53Delegator route53Delegator() {
            return Mockito.mock(Route53Delegator.class);
        }

        /**
         * Route 53 raider route 53 raider.
         *
         * @return the route 53 raider
         */
        @Bean (name={"r53RaiderBean"})
        public Route53RaiderImpl route53Raider() {
            return  Mockito.mock(Route53RaiderImpl.class);
        }


        /**
         * Credentials credentials.
         *
         * @return the credentials
         */
        @Bean
        public Credentials credentials() {
            return new BasicCredentials();
        }

        /**
         * Route 53 step definitions route 53 step definitions.
         *
         * @return the route 53 step definitions
         */
        @Bean
        public Route53StepDefinitions route53StepDefinitions() {
            return new Route53StepDefinitions();
        }

        /**
         * Execution state cache execution state cache.
         *
         * @return the execution state cache
         */
        @Bean
        public ExecutionStateCache executionStateCache() {
            return new ExecutionStateCache();
        }


    }

}
