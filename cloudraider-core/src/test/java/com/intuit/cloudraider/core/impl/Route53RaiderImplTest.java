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

package com.intuit.cloudraider.core.impl;

import com.amazonaws.services.route53.AmazonRoute53;
import com.amazonaws.services.route53.model.*;
import com.intuit.cloudraider.commons.Route53Delegator;
import com.intuit.cloudraider.core.interfaces.Route53Raider;
import com.intuit.cloudraider.model.BasicCredentials;
import com.intuit.cloudraider.model.Credentials;
import com.intuit.cloudraider.model.HealthCheckStatus;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


@PowerMockIgnore({"javax.management.*","javax.net.ssl.*"})
@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@PrepareForTest({Route53RaiderImpl.class, Credentials.class, Route53Raider.class})
public class Route53RaiderImplTest {
    /**
     * The Mockito rule.
     */
    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    /**
     * The Mock amazon route 53.
     */
    @Mock
    AmazonRoute53 mockAmazonRoute53;

    @Autowired
    private  Route53Raider route53RaiderUnderTest;

    @Autowired
    private Route53Delegator route53Delegator;


    private String aLoadBalancerName = "elbName";
    private final String SW1_R53_HEALTH_CHECK_ID = "d5c9f928-c514-4318-86b4-e26313d2188a";
    private final String SW2_R53_HEALTH_CHECK_ID = "281406ee-8e57-45c1-9b44-fc2a05c5f011";


    /**
     * Sets test.
     *
     * @throws Exception the exception
     */
    @Before
    public void setupTest() throws Exception {

        PowerMockito.when(route53Delegator.getAmazonRoute53()).thenReturn(mockAmazonRoute53);

    }

    /**
     * Test simple get health checker status when failure.
     */
    @Test
    public void testSimpleGetHealthCheckerStatusWhenFailure() {
        Mockito.when(mockAmazonRoute53.getHealthCheckStatus(Mockito.any()))
                .thenReturn(new GetHealthCheckStatusResult().withHealthCheckObservations(Arrays.asList(createFailureHealthCheckOb())));

        HealthCheckStatus actualHealthCheckResponse = route53RaiderUnderTest.getSimpleHealthCheckerStatus(SW2_R53_HEALTH_CHECK_ID);

        Assert.assertEquals(HealthCheckStatus.FAILURE, actualHealthCheckResponse);
    }

    /**
     * Test simple get health checker status when success.
     */
    @Test
    public void testSimpleGetHealthCheckerStatusWhenSuccess() {
        Mockito.when(mockAmazonRoute53.getHealthCheckStatus(Mockito.any()))
                .thenReturn(new GetHealthCheckStatusResult().withHealthCheckObservations(Arrays.asList(createSuccessHealthCheckOb())));

        HealthCheckStatus actualHealthCheckResponse = route53RaiderUnderTest.getSimpleHealthCheckerStatus(SW2_R53_HEALTH_CHECK_ID);

        Assert.assertEquals(HealthCheckStatus.SUCCESS, actualHealthCheckResponse);
    }

    /**
     * Test simple get health checker status when multiple successes and failures should be failure.
     */
    @Test
    public void testSimpleGetHealthCheckerStatusWhenMultipleSuccessesAndFailuresShouldBeFailure() {
        List<HealthCheckObservation> observations = createMultipleSuccessHealthObs();
        observations.add(createFailureHealthCheckOb());
        GetHealthCheckStatusResult healthCheckResult = new GetHealthCheckStatusResult().withHealthCheckObservations(observations);
        Mockito.when(mockAmazonRoute53.getHealthCheckStatus(Mockito.any())).thenReturn(healthCheckResult);

        HealthCheckStatus actualHealthCheckResponse = route53RaiderUnderTest.getSimpleHealthCheckerStatus(SW2_R53_HEALTH_CHECK_ID);

        Assert.assertEquals(HealthCheckStatus.FAILURE, actualHealthCheckResponse);
    }

    /**
     * Test simple get health checker status when multiple successes should be success.
     */
    @Test
    public void testSimpleGetHealthCheckerStatusWhenMultipleSuccessesShouldBeSuccess() {
        List<HealthCheckObservation> observations = createMultipleSuccessHealthObs();
        observations.add(createSuccessHealthCheckOb());
        GetHealthCheckStatusResult healthCheckResult = new GetHealthCheckStatusResult().withHealthCheckObservations(observations);
        Mockito.when(mockAmazonRoute53.getHealthCheckStatus(Mockito.any())).thenReturn(healthCheckResult);

        HealthCheckStatus actualHealthCheckResponse = route53RaiderUnderTest.getSimpleHealthCheckerStatus(SW2_R53_HEALTH_CHECK_ID);

        Assert.assertEquals(HealthCheckStatus.SUCCESS, actualHealthCheckResponse);
    }

    /**
     * Test update health check to default regions.
     */
    @Test
    public void testUpdateHealthCheckToDefaultRegions() {
        UpdateHealthCheckResult updateHealthCheckResult = PowerMockito.mock(UpdateHealthCheckResult.class);
        Mockito.when(mockAmazonRoute53.updateHealthCheck(Mockito.any())).thenReturn(updateHealthCheckResult);

        route53RaiderUnderTest.updateHealthCheckToDefaultRegions(SW2_R53_HEALTH_CHECK_ID);
    }

    /**
     * Test update health check regions.
     */
    @Test
    public void testUpdateHealthCheckRegions() {
        UpdateHealthCheckResult updateHealthCheckResult = PowerMockito.mock(UpdateHealthCheckResult.class);
        Mockito.when(mockAmazonRoute53.updateHealthCheck(Mockito.any())).thenReturn(updateHealthCheckResult);

        route53RaiderUnderTest.updateHealthCheckRegions(SW2_R53_HEALTH_CHECK_ID, Arrays.asList("us-west-2","us-east-1"));
    }

    /**
     * Test get health check regions.
     */
    @Test
    public void testGetHealthCheckRegions() {
        GetHealthCheckResult getHealthCheckResult = PowerMockito.mock(GetHealthCheckResult.class);
        HealthCheck healthCheck = new HealthCheck();
        healthCheck.setHealthCheckConfig(new HealthCheckConfig().withRegions("us-west-2","us-east-1"));
        Mockito.when(mockAmazonRoute53.getHealthCheck(Mockito.any())).thenReturn(getHealthCheckResult);
        Mockito.when(getHealthCheckResult.getHealthCheck()).thenReturn(healthCheck);

        List<String> regions = route53RaiderUnderTest.getHealthCheckRegions(SW2_R53_HEALTH_CHECK_ID);
        Assert.assertNotNull(regions);
        Assert.assertEquals(regions.get(0),"us-west-2");

    }

    /**
     * Test get traffic policy version.
     */
    @Test
    public void testGetTrafficPolicyVersion() {
        ListTrafficPolicyInstancesResult listTrafficPolicyInstancesResult = PowerMockito.mock(ListTrafficPolicyInstancesResult.class);
        Mockito.when(mockAmazonRoute53.listTrafficPolicyInstances()).thenReturn(listTrafficPolicyInstancesResult);

        TrafficPolicyInstance trafficPolicyInstance = new TrafficPolicyInstance();
        trafficPolicyInstance.setName("testPolicy");
        trafficPolicyInstance.setTrafficPolicyVersion(1);
        trafficPolicyInstance.setTrafficPolicyId("abc");

        ListTrafficPoliciesResult listTrafficPoliciesResult = PowerMockito.mock(ListTrafficPoliciesResult.class);
        Mockito.when(mockAmazonRoute53.listTrafficPolicies()).thenReturn(listTrafficPoliciesResult);

        TrafficPolicySummary trafficPolicySummary = new TrafficPolicySummary();
        trafficPolicySummary.setName("testPolicy");
        trafficPolicySummary.setId("abc");
        Mockito.when(listTrafficPoliciesResult.getTrafficPolicySummaries()).thenReturn(Arrays.asList(trafficPolicySummary));
        Mockito.when(listTrafficPolicyInstancesResult.getTrafficPolicyInstances()).thenReturn(Arrays.asList(trafficPolicyInstance));

        int policyVersion = route53RaiderUnderTest.getTrafficPolicyVersion("testPolicy");
        Assert.assertEquals(policyVersion,1);

    }

    /**
     * Test update traffic policy version.
     */
    @Test
    public void testUpdateTrafficPolicyVersion() {

        UpdateTrafficPolicyInstanceResult updateTrafficPolicyInstanceResult = PowerMockito.mock(UpdateTrafficPolicyInstanceResult.class);
        Mockito.when(mockAmazonRoute53.updateTrafficPolicyInstance(Mockito.anyObject())).thenReturn(updateTrafficPolicyInstanceResult);

        ListTrafficPolicyInstancesResult listTrafficPolicyInstancesResult = PowerMockito.mock(ListTrafficPolicyInstancesResult.class);
        Mockito.when(mockAmazonRoute53.listTrafficPolicyInstances()).thenReturn(listTrafficPolicyInstancesResult);

        TrafficPolicyInstance trafficPolicyInstance = new TrafficPolicyInstance();
        trafficPolicyInstance.setName("testPolicy");
        trafficPolicyInstance.setTrafficPolicyVersion(1);
        trafficPolicyInstance.setTrafficPolicyId("abc");
        trafficPolicyInstance.setId("abc");

        ListTrafficPoliciesResult listTrafficPoliciesResult = PowerMockito.mock(ListTrafficPoliciesResult.class);
        Mockito.when(mockAmazonRoute53.listTrafficPolicies()).thenReturn(listTrafficPoliciesResult);

        TrafficPolicySummary trafficPolicySummary = new TrafficPolicySummary();
        trafficPolicySummary.setName("testPolicy");
        trafficPolicySummary.setId("abc");
        Mockito.when(listTrafficPoliciesResult.getTrafficPolicySummaries()).thenReturn(Arrays.asList(trafficPolicySummary));
        Mockito.when(listTrafficPolicyInstancesResult.getTrafficPolicyInstances()).thenReturn(Arrays.asList(trafficPolicyInstance));

        route53RaiderUnderTest.updateTrafficPolicy("testPolicy", 2);

    }

    /**
     * Test list health checkers.
     */
    @Test
    public void testListHealthCheckers() {

        ListHealthChecksResult listHealthChecksResult = PowerMockito.mock(ListHealthChecksResult.class);
        Mockito.when(mockAmazonRoute53.listHealthChecks()).thenReturn(listHealthChecksResult);

        HealthCheck healthCheck = new HealthCheck();
        healthCheck.setHealthCheckConfig(new HealthCheckConfig().withRegions("us-west-2","us-east-1"));

        Mockito.when(listHealthChecksResult.getHealthChecks()).thenReturn(Arrays.asList(healthCheck));
        List<HealthCheck> healthChecks = route53RaiderUnderTest.listHealthCheckers();
        Assert.assertNotNull(healthChecks);
        Assert.assertEquals(healthChecks.get(0).getHealthCheckConfig().getRegions(), healthCheck.getHealthCheckConfig().getRegions());


    }


    /**
     * Test get health checker status.
     */
    @Test
    public void testGetHealthCheckerStatus() {

        GetHealthCheckStatusResult getHealthCheckStatusResult = PowerMockito.mock(GetHealthCheckStatusResult.class);
        Mockito.when(mockAmazonRoute53.getHealthCheckStatus(Mockito.anyObject())).thenReturn(getHealthCheckStatusResult);

        Mockito.when(getHealthCheckStatusResult.getHealthCheckObservations()).thenReturn(createMultipleSuccessHealthObs());

        List<HealthCheckObservation> healthCheckObservations = route53RaiderUnderTest.getHealthCheckerStatus(SW1_R53_HEALTH_CHECK_ID);
        Assert.assertEquals(healthCheckObservations.size(), 2);
        Assert.assertEquals(healthCheckObservations.get(0).getStatusReport().getStatus(),"Success");

    }

    private List<HealthCheckObservation> createMultipleSuccessHealthObs() {
        List<HealthCheckObservation> observations = new ArrayList<>();
        observations.add(createSuccessHealthCheckOb());
        observations.add(createSuccessHealthCheckOb());
        return observations;
    }

    private HealthCheckObservation createFailureHealthCheckOb() {
        return new HealthCheckObservation().withStatusReport(new StatusReport().withStatus("Failure"));
    }

    private HealthCheckObservation createSuccessHealthCheckOb() {
        return new HealthCheckObservation().withStatusReport(new StatusReport().withStatus("Success"));
    }

    /**
     * The type R 53 raider impl test context configuration.
     */
    @Configuration
    protected static class R53RaiderImplTestContextConfiguration {

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
        public Route53Raider route53Raider() {
            return new Route53RaiderImpl();
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



    }

}
