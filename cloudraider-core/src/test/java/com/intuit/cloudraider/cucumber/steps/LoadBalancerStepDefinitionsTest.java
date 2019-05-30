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

import com.intuit.cloudraider.commons.*;
import com.intuit.cloudraider.core.impl.ApplicationLoadBalancerRaiderImpl;
import com.intuit.cloudraider.core.impl.EC2RaiderImpl;
import com.intuit.cloudraider.core.impl.LoadBalancerRaiderImpl;
import com.intuit.cloudraider.core.interfaces.EC2Raider;
import com.intuit.cloudraider.cucumber.model.ExecutionStateCache;
import com.intuit.cloudraider.model.BasicCredentials;
import com.intuit.cloudraider.model.Credentials;
import com.intuit.cloudraider.model.EC2InstanceTO;
import org.junit.Assert;
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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@PowerMockIgnore({"javax.management.*","javax.net.ssl.*"})
@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@PrepareForTest({EC2RaiderImpl.class, LoadBalancerRaiderImpl.class, LoadBalancerStepDefinitions.class, EnvironmentHealerStepDefinitions.class})
public class LoadBalancerStepDefinitionsTest {

    @Autowired
    private LoadBalancerStepDefinitions loadBalancerStepDefinitions;

    @Autowired
    private EC2RaiderImpl ec2Raider;

    @Autowired
    private LoadBalancerRaiderImpl loadBalancerRaider;

    @Autowired
    private ApplicationLoadBalancerRaiderImpl applicationLoadBalancerRaider;

    @Autowired
    private ExecutionStateCache executionStateCache;

    /**
     * Sets method.
     *
     * @throws Exception the exception
     */
    @Before
    public void setupMethod() throws Exception {

        Map<String, String> map = new HashMap<>();
        map.put("elb", "ELB");

        PowerMockito.when(loadBalancerRaider.getInServiceInstances("elb")).thenReturn(Arrays.asList("id-123", "id-456"));
        PowerMockito.when(applicationLoadBalancerRaider.getInServiceInstances("alb")).thenReturn(Arrays.asList("id-123", "id-456"));
        PowerMockito.when(ec2Raider.getEC2InstancesByIds(Mockito.any())).thenReturn(createInstances());
        PowerMockito.when(loadBalancerRaider.getLoadBalancerSubnets("elb")).thenReturn(Arrays.asList("1-v0s", "1-v1s", "2-v0s"));
        PowerMockito.when(loadBalancerRaider.getSecurityGroups("elb")).thenReturn(Arrays.asList("sg-9", "sg-1", "sg-12"));
        PowerMockito.when(loadBalancerRaider.getLoadBalancerNames()).thenReturn(map);

        executionStateCache.clear();
    }

    /**
     * Test given load balancer name.
     *
     * @throws Throwable the throwable
     */
    @Test
    public void testGivenLoadBalancerName() throws Throwable {
        loadBalancerStepDefinitions.givenLoadBalancerName("ELB", "elb");
        Assert.assertEquals(executionStateCache.getLoadBalancerName(), "elb");
        Assert.assertTrue(executionStateCache.getInstances().size() > 0);
    }

    /**
     * Test given application load balancer name.
     *
     * @throws Throwable the throwable
     */
    @Test
    public void testGivenApplicationLoadBalancerName() throws Throwable {
        loadBalancerStepDefinitions.givenLoadBalancerName("ALB", "alb");
        Assert.assertEquals(executionStateCache.getLoadBalancerName(), "alb");
        Assert.assertTrue(executionStateCache.getInstances().size() > 0);
    }

    /**
     * Test detach security group.
     *
     * @throws Throwable the throwable
     */
    @Test
    public void testDetachSecurityGroup() throws Throwable {
        loadBalancerStepDefinitions.givenLoadBalancerName("ELB", "elb");
        loadBalancerStepDefinitions.detachSecurityGroup("sg-123");
    }

    /**
     * Test attach security group.
     *
     * @throws Throwable the throwable
     */
    @Test
    public void testAttachSecurityGroup() throws Throwable {
        loadBalancerStepDefinitions.givenLoadBalancerName("ELB", "elb");
        loadBalancerStepDefinitions.attachSecurityGroup("sg-123");
    }

    /**
     * Test detach subnet.
     *
     * @throws Throwable the throwable
     */
    @Test
    public void testDetachSubnet() throws Throwable {
        loadBalancerStepDefinitions.givenLoadBalancerName("ELB", "elb");
        loadBalancerStepDefinitions.detachSubnet("subnet-123");
    }

    /**
     * Test attach subnet.
     *
     * @throws Throwable the throwable
     */
    @Test
    public void testAttachSubnet() throws Throwable {
        loadBalancerStepDefinitions.givenLoadBalancerName("ELB", "elb");
        loadBalancerStepDefinitions.attachSubnet("subnet-123");
    }

    /**
     * Test assert healthy host count.
     *
     * @throws Throwable the throwable
     */
    @Test
    public void testAssertHealthyHostCount() throws Throwable {
        loadBalancerStepDefinitions.givenLoadBalancerName("ELB", "elb");
        loadBalancerStepDefinitions.assertHealthyHostCount(2);
    }

    /**
     * Test assert un healthy host count.
     *
     * @throws Throwable the throwable
     */
    @Test
    public void testAssertUnHealthyHostCount() throws Throwable {
        loadBalancerStepDefinitions.givenLoadBalancerName("ELB", "elb");
        loadBalancerStepDefinitions.assertUntHealthyHostCount(0);
    }

    /**
     * Test corrupt lb health check.
     *
     * @throws Throwable the throwable
     */
    @Test
    public void testCorruptLBHealthCheck() throws Throwable {
        loadBalancerStepDefinitions.givenLoadBalancerName("ELB", "elb");
        loadBalancerStepDefinitions.corruptLBHealthCheck();
    }

    /**
     * Test un corrupt lb health check.
     *
     * @throws Throwable the throwable
     */
    @Test
    public void testUnCorruptLBHealthCheck() throws Throwable {
        loadBalancerStepDefinitions.givenLoadBalancerName("ELB", "elb");
        loadBalancerStepDefinitions.unCorruptLBHealthCheck();
    }

    /**
     * Test de register instances.
     *
     * @throws Throwable the throwable
     */
    @Test
    public void testDeRegisterInstances() throws Throwable {
        PowerMockito.when(loadBalancerRaider.getInServiceInstances("elb")).thenReturn(Arrays.asList("id-123", "id-456"));
        PowerMockito.when(ec2Raider.getEC2InstancesByIds(Mockito.any())).thenReturn(createInstances());
        loadBalancerStepDefinitions.givenLoadBalancerName("ELB", "elb");
        loadBalancerStepDefinitions.deRegisterInstances(1);
        Assert.assertTrue(executionStateCache.getDeregistedInstance().size() == 1);
    }


    /**
     * Test de register percent of instances.
     *
     * @throws Throwable the throwable
     */
    @Test
    public void testDeRegisterPercentOfInstances() throws Throwable {
    	int percentage = 50;
        PowerMockito.when(loadBalancerRaider.getInServiceInstances("elb")).thenReturn(Arrays.asList("id-123", "id-456"));
        PowerMockito.when(ec2Raider.getEC2InstancesByIds(Mockito.any())).thenReturn(createInstances());
        loadBalancerStepDefinitions.givenLoadBalancerName("ELB", "elb");
        loadBalancerStepDefinitions.deRegisterPercentOfInstances(percentage);
        Assert.assertTrue(executionStateCache.getDeregistedInstance().size() == Math.round((percentage * executionStateCache.getInstances().size())/100));
    }


    /**
     * Test register instances.
     *
     * @throws Throwable the throwable
     */
    @Test
    public void testRegisterInstances() throws Throwable {
        loadBalancerStepDefinitions.givenLoadBalancerName("ELB", "elb");
        executionStateCache.addDeregisteredInstance(createInstances());
        loadBalancerStepDefinitions.registerInstances();
        Assert.assertEquals(0, executionStateCache.getDeregistedInstance().size());
    }


    /**
     * Test confirm un healthy host count.
     *
     * @throws Throwable the throwable
     */
    @Test
    public void testConfirmUnHealthyHostCount() throws Throwable {
        loadBalancerStepDefinitions.givenLoadBalancerName("ELB", "elb");
        loadBalancerStepDefinitions.confirmUnHealthyHostCount(0);
    }

    /**
     * Testconfirm healthy host count.
     *
     * @throws Throwable the throwable
     */
    @Test
    public void testconfirmHealthyHostCount() throws Throwable {
        loadBalancerStepDefinitions.givenLoadBalancerName("ELB", "elb");
        loadBalancerStepDefinitions.confirmHealthyHostCount(2);
    }

    /**
     * Test subnet manipulation.
     *
     * @throws Throwable the throwable
     */
    @Test
    public void testSubnetManipulation() throws Throwable {
        loadBalancerStepDefinitions.givenLoadBalancerName("ELB", "elb");
        Assert.assertEquals(0, executionStateCache.getDetachedSubnets().size());
        loadBalancerStepDefinitions.detachRandomSubnets(0);
        Assert.assertEquals(0, executionStateCache.getDetachedSubnets().size());
        loadBalancerStepDefinitions.attachRandomRemovedSubnets();
        Assert.assertEquals(0, executionStateCache.getDetachedSubnets().size());
        loadBalancerStepDefinitions.detachRandomSubnets(2);
        Assert.assertEquals(2, executionStateCache.getDetachedSubnets().size());
        loadBalancerStepDefinitions.attachRandomRemovedSubnets();
        Assert.assertEquals(0, executionStateCache.getDetachedSubnets().size());
    }

    /**
     * Test security group manipulation.
     *
     * @throws Throwable the throwable
     */
    @Test
    public void testSecurityGroupManipulation() throws Throwable {
        loadBalancerStepDefinitions.givenLoadBalancerName("ELB", "elb");
        Assert.assertEquals(0, executionStateCache.getDetachedSecurityGroups().size());
        loadBalancerStepDefinitions.detachRandomSecurityGroups(1);
        Assert.assertEquals(1, executionStateCache.getDetachedSecurityGroups().size());
        loadBalancerStepDefinitions.attachRandomRemovedSecurityGroups();
        Assert.assertEquals(0, executionStateCache.getDetachedSecurityGroups().size());
        loadBalancerStepDefinitions.detachRandomSecurityGroups(2);
        Assert.assertEquals(2, executionStateCache.getDetachedSecurityGroups().size());
        loadBalancerStepDefinitions.attachRandomRemovedSecurityGroups();
        Assert.assertEquals(0, executionStateCache.getDetachedSecurityGroups().size());
    }

    /**
     * Test get load balancer names.
     *
     * @throws Throwable the throwable
     */
    @Test
    public void testGetLoadBalancerNames() throws Throwable {
        loadBalancerStepDefinitions.givenLoadBalancerName("ELB", "elb");
        Map<String, String> names = loadBalancerStepDefinitions.getLoadBalancerNames();
        Assert.assertEquals(1, names.size());
        Assert.assertEquals("ELB", names.get("elb"));
    }

    /**
     * Test get subnets.
     *
     * @throws Throwable the throwable
     */
    @Test
    public void testGetSubnets() throws Throwable {
        loadBalancerStepDefinitions.givenLoadBalancerName("ELB", "elb");
        List<String> subnets = loadBalancerStepDefinitions.getSubnets();
        Assert.assertEquals(3, subnets.size());
        Assert.assertTrue(subnets.contains("2-v0s"));
        Assert.assertTrue(subnets.contains("1-v1s"));
        Assert.assertTrue(subnets.contains("1-v0s"));
    }

    /**
     * Test get security groups.
     *
     * @throws Throwable the throwable
     */
    @Test
    public void testGetSecurityGroups() throws Throwable {
        loadBalancerStepDefinitions.givenLoadBalancerName("ELB", "elb");
        List<String> groups = loadBalancerStepDefinitions.getSecurityGroups();
        Assert.assertEquals(3, groups.size());
        Assert.assertTrue(groups.contains("sg-9"));
        Assert.assertTrue(groups.contains("sg-1"));
        Assert.assertTrue(groups.contains("sg-12"));
    }

    private List<EC2InstanceTO> createInstances() {
        EC2InstanceTO ec2InstanceTO = new EC2InstanceTO();
        ec2InstanceTO.setAvailabilityZone("us-west-2a");
        ec2InstanceTO.setInstanceId("id-123");
        ec2InstanceTO.setPrivateIpAddress("10.1.1.1");

        EC2InstanceTO ec2InstanceTO2 = new EC2InstanceTO();
        ec2InstanceTO2.setAvailabilityZone("us-west-2a");
        ec2InstanceTO2.setInstanceId("id-456");
        ec2InstanceTO2.setPrivateIpAddress("10.1.1.2");
        return Arrays.asList(ec2InstanceTO, ec2InstanceTO2);
    }

    /**
     * The type Load balancer step definition test context configuration.
     */
    @Configuration
    protected static class LoadBalancerStepDefinitionTestContextConfiguration {


        /**
         * Ec 2 delegator ec 2 delegator.
         *
         * @return the ec 2 delegator
         */
        @Bean
        public EC2Delegator ec2Delegator() {
            return Mockito.mock(EC2Delegator.class);
        }

        /**
         * Ec 2 raider ec 2 raider.
         *
         * @return the ec 2 raider
         */
        @Bean (name={"ec2raiderBean"})
        public EC2Raider ec2Raider() {
            return  Mockito.mock(EC2RaiderImpl.class);
        }

        /**
         * Application load balancer raider application load balancer raider.
         *
         * @return the application load balancer raider
         */
        @Bean (name={"albRaiderBean"})
        public ApplicationLoadBalancerRaiderImpl applicationLoadBalancerRaider() {
            return  Mockito.mock(ApplicationLoadBalancerRaiderImpl.class);
        }

        /**
         * Load balancer raider load balancer raider.
         *
         * @return the load balancer raider
         */
        @Bean (name={"elbRaiderBean"})
        public LoadBalancerRaiderImpl loadBalancerRaider() {
            return  Mockito.mock(LoadBalancerRaiderImpl.class);
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
         * Load balancer step definitions load balancer step definitions.
         *
         * @return the load balancer step definitions
         */
        @Bean
        public LoadBalancerStepDefinitions loadBalancerStepDefinitions() {
            return new LoadBalancerStepDefinitions();
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

        /**
         * Application load balancer delegator application load balancer delegator.
         *
         * @return the application load balancer delegator
         */
        @Bean
        public ApplicationLoadBalancerDelegator applicationLoadBalancerDelegator() {
            return Mockito.mock(ApplicationLoadBalancerDelegator.class);
        }

        /**
         * Load balancer delegator load balancer delegator.
         *
         * @return the load balancer delegator
         */
        @Bean
        public LoadBalancerDelegator loadBalancerDelegator() {
            return Mockito.mock(LoadBalancerDelegator.class);
        }

    }
}
