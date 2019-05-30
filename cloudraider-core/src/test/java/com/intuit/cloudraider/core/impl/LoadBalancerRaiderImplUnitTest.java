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


import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancing;
import com.amazonaws.services.elasticloadbalancing.model.ConfigureHealthCheckRequest;
import com.amazonaws.services.elasticloadbalancing.model.DescribeLoadBalancersResult;
import com.amazonaws.services.elasticloadbalancing.model.HealthCheck;
import com.amazonaws.services.elasticloadbalancing.model.LoadBalancerDescription;
import com.intuit.cloudraider.commons.LoadBalancerDelegator;
import com.intuit.cloudraider.model.BasicCredentials;
import com.intuit.cloudraider.model.Credentials;
import com.intuit.cloudraider.model.HealthCheckTarget;
import com.intuit.cloudraider.utils.HealthCheckUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
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


@PowerMockIgnore({"javax.management.*","javax.net.ssl.*"})
@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@PrepareForTest({LoadBalancerRaiderImpl.class, Credentials.class, LoadBalancerDelegator.class, AmazonElasticLoadBalancing.class })
public class LoadBalancerRaiderImplUnitTest {


    /**
     * The Mockito rule.
     */
    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Captor
    private ArgumentCaptor<ConfigureHealthCheckRequest> argumentCaptorHealthCheckRequest;
    @Mock
    private AmazonElasticLoadBalancing mockAmazonElasticLoadBalancing;

    @Autowired
    private  LoadBalancerRaiderImpl loadBalancerRaiderUnderTest;

    @Autowired
    private LoadBalancerDelegator loadBalancerDelegator;


    @Autowired
    private Credentials credentials;


    /**
     * Sets test.
     *
     * @throws Exception the exception
     */
    @Before
    public void setupTest() throws Exception {

        PowerMockito.whenNew(LoadBalancerDelegator.class).withNoArguments().thenReturn(loadBalancerDelegator);
        PowerMockito.when(loadBalancerDelegator.getAmazonElasticLoadBalancing()).thenReturn(mockAmazonElasticLoadBalancing);

    }


    private String aLoadBalancerName = "elb-fido-prf";

    /**
     * Test update load balancer health check.
     */
    @Test
    public void testUpdateLoadBalancerHealthCheck() {
        HealthCheckTarget.PingProtocol expectedPingProtocol = HealthCheckTarget.PingProtocol.TCP;
        Integer expectedPingPort = 4430;
        String expectedPingPath = "/some/path";
        String originalPingPort = ":443";

        HealthCheck originalHealthCheck = setupMock(expectedPingProtocol, expectedPingPath, originalPingPort);
        HealthCheckTarget healthCheckTargetDisable = new HealthCheckTarget();
        healthCheckTargetDisable.setPingPort(expectedPingPort);
        healthCheckTargetDisable.setPingProtocol(expectedPingProtocol);
        healthCheckTargetDisable.setPingPath(expectedPingPath);

        loadBalancerRaiderUnderTest.updateLoadbalancerHealthCheck(aLoadBalancerName, healthCheckTargetDisable);

        Mockito.verify(mockAmazonElasticLoadBalancing).configureHealthCheck(argumentCaptorHealthCheckRequest.capture());
        Assert.assertEquals(originalHealthCheck.getTarget(), argumentCaptorHealthCheckRequest.getValue().getHealthCheck().getTarget());
    }

    /**
     * Test update load balancer health check when one target param provided.
     */
    @Test
    public void testUpdateLoadBalancerHealthCheckWhenOneTargetParamProvided() {
        HealthCheckTarget.PingProtocol expectedPingProtocol = HealthCheckTarget.PingProtocol.TCP;
        Integer expectedPingPort = 4430;
        String expectedPingPath = "/some/path";
        String originalPingPort = ":443";

        setupMock(expectedPingProtocol, expectedPingPath, originalPingPort);
        HealthCheckTarget healthCheckTargetDisable = new HealthCheckTarget();
        healthCheckTargetDisable.setPingPort(expectedPingPort);

        loadBalancerRaiderUnderTest.updateLoadbalancerHealthCheck(aLoadBalancerName, healthCheckTargetDisable);

        Mockito.verify(mockAmazonElasticLoadBalancing).configureHealthCheck(argumentCaptorHealthCheckRequest.capture());
        Assert.assertEquals(expectedPingProtocol + ":" + expectedPingPort + expectedPingPath, argumentCaptorHealthCheckRequest.getValue().getHealthCheck().getTarget());
    }

    /**
     * Test force fail health check.
     */
    @Test
    public void testForceFailHealthCheck() {
        HealthCheckTarget.PingProtocol expectedPingProtocol = HealthCheckTarget.PingProtocol.TCP;
        Integer expectedPingPort = HealthCheckUtils.scramblePingPort(443);
        String expectedPingPath = "/some/path";
        String originalHealthCheckPingPort = ":443";

        setupMock(expectedPingProtocol, expectedPingPath, originalHealthCheckPingPort);
        HealthCheckTarget healthCheckTargetDisable = new HealthCheckTarget();
        healthCheckTargetDisable.setPingPort(expectedPingPort);

        loadBalancerRaiderUnderTest.forceFailLoadbalancerHealthCheck(aLoadBalancerName);

        Mockito.verify(mockAmazonElasticLoadBalancing).configureHealthCheck(argumentCaptorHealthCheckRequest.capture());
        Assert.assertEquals(expectedPingProtocol + ":" + expectedPingPort + expectedPingPath, argumentCaptorHealthCheckRequest.getValue().getHealthCheck().getTarget());
    }

    /**
     * Test force fail health check when already force failed.
     */
    @Test(expected = IllegalStateException.class)
    public void testForceFailHealthCheckWhenAlreadyForceFailed() {
        HealthCheckTarget.PingProtocol expectedPingProtocol = HealthCheckTarget.PingProtocol.TCP;
        Integer expectedPingPort = HealthCheckUtils.scramblePingPort(443);
        String expectedPingPath = "/some/path";
        String originalHealthCheckPingPort = ":3101";

        setupMock(expectedPingProtocol, expectedPingPath, originalHealthCheckPingPort);
        HealthCheckTarget healthCheckTargetDisable = new HealthCheckTarget();
        healthCheckTargetDisable.setPingPort(expectedPingPort);

        loadBalancerRaiderUnderTest.forceFailLoadbalancerHealthCheck(aLoadBalancerName);

        Mockito.verify(mockAmazonElasticLoadBalancing).configureHealthCheck(argumentCaptorHealthCheckRequest.capture());
        Assert.assertEquals(expectedPingProtocol + ":" + expectedPingPort + expectedPingPath, argumentCaptorHealthCheckRequest.getValue().getHealthCheck().getTarget());
    }

    /**
     * Test undo force fail health check.
     */
    @Test
    public void testUndoForceFailHealthCheck() {
        HealthCheckTarget.PingProtocol expectedPingProtocol = HealthCheckTarget.PingProtocol.TCP;
        Integer expectedPingPort = 443;
        String expectedPingPath = "/some/path";
        String originalHealthCheckPingPort = ":3101";

        setupMock(expectedPingProtocol, expectedPingPath, originalHealthCheckPingPort);
        HealthCheckTarget healthCheckTargetDisable = new HealthCheckTarget();
        healthCheckTargetDisable.setPingPort(expectedPingPort);

        loadBalancerRaiderUnderTest.undoForceFailLoadbalancerHealthCheck(aLoadBalancerName);

        Mockito.verify(mockAmazonElasticLoadBalancing).configureHealthCheck(argumentCaptorHealthCheckRequest.capture());
        Assert.assertEquals(expectedPingProtocol + ":" + expectedPingPort + expectedPingPath, argumentCaptorHealthCheckRequest.getValue().getHealthCheck().getTarget());
    }

    /**
     * Test undo force fail health check when no force failing has occurred.
     */
    @Test(expected = IllegalStateException.class)
    public void testUndoForceFailHealthCheckWhenNoForceFailingHasOccurred() {
        HealthCheckTarget.PingProtocol expectedPingProtocol = HealthCheckTarget.PingProtocol.TCP;
        Integer expectedPingPort = 443;
        String expectedPingPath = "/some/path";
        String originalHealthCheckPingPort = ":443";

        setupMock(expectedPingProtocol, expectedPingPath, originalHealthCheckPingPort);
        HealthCheckTarget healthCheckTargetDisable = new HealthCheckTarget();
        healthCheckTargetDisable.setPingPort(expectedPingPort);

        loadBalancerRaiderUnderTest.undoForceFailLoadbalancerHealthCheck(aLoadBalancerName);

        Mockito.verify(mockAmazonElasticLoadBalancing).configureHealthCheck(argumentCaptorHealthCheckRequest.capture());
        Assert.assertEquals(expectedPingProtocol + ":" + expectedPingPort + expectedPingPath, argumentCaptorHealthCheckRequest.getValue().getHealthCheck().getTarget());
    }

    /**
     * Test undo force fail health check when scrambled ping port is greater than acceptable.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testUndoForceFailHealthCheckWhenScrambledPingPortIsGreaterThanAcceptable() {
        HealthCheckTarget.PingProtocol expectedPingProtocol = HealthCheckTarget.PingProtocol.TCP;
        Integer expectedPingPort = 443;
        String expectedPingPath = "/some/path";
        String originalHealthCheckPingPort = ":65000";

        setupMock(expectedPingProtocol, expectedPingPath, originalHealthCheckPingPort);
        HealthCheckTarget healthCheckTargetDisable = new HealthCheckTarget();
        healthCheckTargetDisable.setPingPort(expectedPingPort);

        loadBalancerRaiderUnderTest.forceFailLoadbalancerHealthCheck(aLoadBalancerName);

        Mockito.verify(mockAmazonElasticLoadBalancing).configureHealthCheck(argumentCaptorHealthCheckRequest.capture());
        Assert.assertEquals(expectedPingProtocol + ":" + expectedPingPort + expectedPingPath, argumentCaptorHealthCheckRequest.getValue().getHealthCheck().getTarget());
    }

    private HealthCheck setupMock(HealthCheckTarget.PingProtocol expectedPingProtocol, String expectedPingPath, String originalPingPort) {
        HealthCheck healthCheck = new HealthCheck().withTarget(expectedPingProtocol + originalPingPort + expectedPingPath);
        LoadBalancerDescription loadBalancerDescription = new LoadBalancerDescription().withHealthCheck(healthCheck);
        DescribeLoadBalancersResult describeLoadBalancersResult = new DescribeLoadBalancersResult().withLoadBalancerDescriptions(loadBalancerDescription);
        Mockito.when(mockAmazonElasticLoadBalancing.describeLoadBalancers(Mockito.any())).thenReturn(describeLoadBalancersResult);
        return healthCheck;
    }


    /**
     * The type Elb raider impl test context configuration.
     */
    @Configuration
    protected static class ELBRaiderImplTestContextConfiguration {

        /**
         * Load balancer delegator load balancer delegator.
         *
         * @return the load balancer delegator
         */
        @Bean
        public LoadBalancerDelegator loadBalancerDelegator() {
            return Mockito.mock(LoadBalancerDelegator.class);
        }

        /**
         * Load balancer raider under test load balancer raider.
         *
         * @return the load balancer raider
         */
        @Bean
        public LoadBalancerRaiderImpl loadBalancerRaiderUnderTest() {
            return new LoadBalancerRaiderImpl();
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
