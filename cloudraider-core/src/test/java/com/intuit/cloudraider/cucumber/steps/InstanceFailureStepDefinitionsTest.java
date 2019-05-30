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

import com.intuit.cloudraider.commons.EC2Delegator;
import com.intuit.cloudraider.commons.SSMDelegator;
import com.intuit.cloudraider.commons.SystemDelegator;
import com.intuit.cloudraider.core.impl.EC2RaiderImpl;
import com.intuit.cloudraider.core.impl.SSMRaiderImpl;
import com.intuit.cloudraider.core.impl.SystemRaiderImpl;
import com.intuit.cloudraider.core.interfaces.EC2Raider;
import com.intuit.cloudraider.cucumber.model.ExecutionStateCache;
import com.intuit.cloudraider.cucumber.util.ScriptExecutor;
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
import java.util.List;
import java.util.stream.Collectors;


@PowerMockIgnore({"javax.management.*","javax.net.ssl.*"})
@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@PrepareForTest({EC2RaiderImpl.class,ScriptExecutor.class, SSMRaiderImpl.class,
        InstanceFailureStepDefinitions.class, EnvironmentHealerStepDefinitions.class})
public class InstanceFailureStepDefinitionsTest {

    @Autowired
    private InstanceFailureStepDefinitions instanceFailureStepDefinitions;

    @Autowired
    private EC2RaiderImpl ec2Raider;

    @Autowired
    private ExecutionStateCache executionStateCache;

    /**
     * Sets method.
     *
     * @throws Exception the exception
     */
    @Before
    public void setupMethod() throws Exception {
       executionStateCache.clear();
    }

    /**
     * Test given ec 2 with tag.
     *
     * @throws Exception the exception
     */
    @Test
    public void testGivenEC2WithTag() throws Exception
    {
        PowerMockito.when(ec2Raider.getInstancesByName("test")).thenReturn(createInstances());
        instanceFailureStepDefinitions.givenEC2InstanceName("test");
        Assert.assertEquals(executionStateCache.getEc2Tag(), "test");
        Assert.assertTrue(executionStateCache.getInstances().size()>0);
    }

    /**
     * Test all ec 2.
     *
     * @throws Exception the exception
     */
    @Test
    public void testAllEC2() throws Exception
    {
        PowerMockito.when(ec2Raider.getFilteredActiveInstances(Mockito.anyListOf(String.class))).thenReturn(createInstances());
        instanceFailureStepDefinitions.givenFilteredEC2("");
        Assert.assertEquals(2, executionStateCache.getInstances().size());
        List<String> ids = executionStateCache.getInstances().stream().map(EC2InstanceTO::getInstanceId).collect(Collectors.toList());
        Assert.assertTrue(ids.contains("i-1234"));
        Assert.assertTrue(ids.contains("i-4567"));
    }

    /**
     * Test terminate process on healthy instances.
     *
     * @throws Exception the exception
     */
    @Test
    public void testTerminateProcessOnHealthyInstances() throws Exception
    {
        PowerMockito.when(ec2Raider.getInstancesByName("test")).thenReturn(createInstances());
        instanceFailureStepDefinitions.givenEC2InstanceName("test");
        Assert.assertEquals(executionStateCache.getEc2Tag(), "test");
        Assert.assertTrue(executionStateCache.getInstances().size()>0);

        instanceFailureStepDefinitions.terminateProcessOnHealthyInstances("nginx",1);
        Assert.assertTrue(executionStateCache.getProcessNames().contains("nginx"));


    }

    /**
     * Test terminate process on all healthy instances.
     *
     * @throws Exception the exception
     */
    @Test
    public void testTerminateProcessOnAllHealthyInstances() throws Exception
    {
        PowerMockito.when(ec2Raider.getInstancesByName("test")).thenReturn(createInstances());
        instanceFailureStepDefinitions.givenEC2InstanceName("test");
        Assert.assertEquals(executionStateCache.getEc2Tag(), "test");
        Assert.assertTrue(executionStateCache.getInstances().size()>0);
        try {
            instanceFailureStepDefinitions.terminateProcessOnAllHealthyInstances("nginx");
        }
        catch (Throwable t)
        {
            t.printStackTrace();
        }
        Assert.assertTrue(executionStateCache.getProcessNames().contains("nginx"));
        Assert.assertFalse(executionStateCache.getUnhealthyInstances().isEmpty());

    }

    /**
     * Terminate process in availability zone.
     *
     * @throws Exception the exception
     */
    @Test
    public void terminateProcessInAvailabilityZone() throws Exception
    {
        PowerMockito.when(ec2Raider.getInstancesByName(Mockito.anyString())).thenReturn(createInstances());
        PowerMockito.when(ec2Raider.getEc2InstancesForAvailabilityZone(Mockito.anyString(), Mockito.anyList())).thenReturn(createInstances());
        instanceFailureStepDefinitions.givenEC2InstanceName("test");
        Assert.assertEquals(executionStateCache.getEc2Tag(), "test");

        instanceFailureStepDefinitions.terminateProcessInAvailabilityZone("nginx",1, "us-west-2a");
        Assert.assertTrue(executionStateCache.getProcessNames().contains("nginx"));

    }

    /**
     * Test terminate all in service instances.
     *
     * @throws Exception the exception
     */
    @Test
    public void testTerminateAllInServiceInstances() throws Exception
    {
        PowerMockito.when(ec2Raider.getInstancesByName("test")).thenReturn(createInstances());
        instanceFailureStepDefinitions.givenEC2InstanceName("test");
        Assert.assertEquals(executionStateCache.getEc2Tag(), "test");
        Assert.assertTrue(executionStateCache.getInstances().size()>0);

        instanceFailureStepDefinitions.terminateAllInstances();
    }

    /**
     * Test terminate instance on num instances.
     *
     * @throws Exception the exception
     */
    @Test
    public void testTerminateInstanceOnNumInstances() throws Exception
    {
        PowerMockito.when(ec2Raider.getInstancesByName("test")).thenReturn(createInstances());
        instanceFailureStepDefinitions.givenEC2InstanceName("test");
        Assert.assertEquals(executionStateCache.getEc2Tag(), "test");
        Assert.assertTrue(executionStateCache.getInstances().size()>0);

        PowerMockito.when(ec2Raider.getEC2InstanceById("i-1234")).thenReturn(createInstances().get(0));
        PowerMockito.when(ec2Raider.getEC2InstanceById("i-4567")).thenReturn(createInstances().get(1));

        instanceFailureStepDefinitions.terminateInstanceOnNumInstances(1);
    }

    /**
     * Test stop process on healthy instances.
     *
     * @throws Exception the exception
     */
    @Test
    public void testStopProcessOnHealthyInstances() throws Exception
    {
        PowerMockito.when(ec2Raider.getInstancesByName("test")).thenReturn(createInstances());
        instanceFailureStepDefinitions.givenEC2InstanceName("test");
        Assert.assertEquals(executionStateCache.getEc2Tag(), "test");
        Assert.assertTrue(executionStateCache.getInstances().size()>0);

        instanceFailureStepDefinitions.stopProcessOnHealthyInstances("process","nginx",1);
        Assert.assertEquals(executionStateCache.getUnhealthyInstances().size(), 1);
        Assert.assertTrue(executionStateCache.getProcessNames().contains("nginx"));
    }

    /**
     * Test start process on healthy instances.
     *
     * @throws Exception the exception
     */
    @Test
    public void testStartProcessOnHealthyInstances() throws Exception
    {
        PowerMockito.when(ec2Raider.getInstancesByName("test")).thenReturn(createInstances());
        instanceFailureStepDefinitions.givenEC2InstanceName("test");
        Assert.assertEquals(executionStateCache.getEc2Tag(), "test");
        Assert.assertTrue(executionStateCache.getInstances().size()>0);

        instanceFailureStepDefinitions.startProcessOnHealthyInstances("process","nginx",1);
        Assert.assertEquals(executionStateCache.getUnhealthyInstances().size(), 0);
        Assert.assertTrue(executionStateCache.getProcessNames().contains("nginx"));
    }

    /**
     * Test block domain.
     *
     * @throws Exception the exception
     */
    @Test
    public void testBlockDomain() throws Exception
    {
        PowerMockito.when(ec2Raider.getInstancesByName("test")).thenReturn(createInstances());
        instanceFailureStepDefinitions.givenEC2InstanceName("test");
        Assert.assertEquals(executionStateCache.getEc2Tag(), "test");
        Assert.assertTrue(executionStateCache.getInstances().size()>0);


        PowerMockito.when(ec2Raider.getEC2InstanceById("i-1234")).thenReturn(createInstances().get(0));
        PowerMockito.when(ec2Raider.getEC2InstanceById("i-4567")).thenReturn(createInstances().get(1));
        instanceFailureStepDefinitions.blockDomain("www.yahoo.com",1);
        Assert.assertEquals(executionStateCache.getUnhealthyInstances().size(), 1);
        Assert.assertEquals(executionStateCache.getBlockedDomains().get(0),"www.yahoo.com");
    }

    /**
     * Test block port.
     *
     * @throws Exception the exception
     */
    @Test
    public void testBlockPort() throws Exception
    {
        PowerMockito.when(ec2Raider.getInstancesByName("test")).thenReturn(createInstances());
        instanceFailureStepDefinitions.givenEC2InstanceName("test");
        Assert.assertEquals(executionStateCache.getEc2Tag(), "test");
        Assert.assertTrue(executionStateCache.getInstances().size()>0);


        PowerMockito.when(ec2Raider.getEC2InstanceById("i-1234")).thenReturn(createInstances().get(0));
        PowerMockito.when(ec2Raider.getEC2InstanceById("i-4567")).thenReturn(createInstances().get(1));
        instanceFailureStepDefinitions.blockPort(8080,1);
        Assert.assertEquals(executionStateCache.getUnhealthyInstances().size(), 1);
        Assert.assertTrue(executionStateCache.getPortNums().contains(8080));
    }

    /**
     * Test block dynamo db.
     *
     * @throws Exception the exception
     */
    @Test
    public void testBlockDynamoDB() throws Exception
    {
        PowerMockito.when(ec2Raider.getInstancesByName("test")).thenReturn(createInstances());
        instanceFailureStepDefinitions.givenEC2InstanceName("test");
        Assert.assertEquals(executionStateCache.getEc2Tag(), "test");
        Assert.assertTrue(executionStateCache.getInstances().size()>0);


        PowerMockito.when(ec2Raider.getEC2InstanceById("i-1234")).thenReturn(createInstances().get(0));
        PowerMockito.when(ec2Raider.getEC2InstanceById("i-4567")).thenReturn(createInstances().get(1));
        instanceFailureStepDefinitions.blockDynamoDb(1);
        Assert.assertEquals(executionStateCache.getUnhealthyInstances().size(), 1);
        Assert.assertTrue(executionStateCache.isBlockDynamoDB());
    }

    /**
     * Test block s 3.
     *
     * @throws Exception the exception
     */
    @Test
    public void testBlockS3() throws Exception
    {
        PowerMockito.when(ec2Raider.getInstancesByName("test")).thenReturn(createInstances());
        instanceFailureStepDefinitions.givenEC2InstanceName("test");
        Assert.assertEquals(executionStateCache.getEc2Tag(), "test");
        Assert.assertTrue(executionStateCache.getInstances().size()>0);


        PowerMockito.when(ec2Raider.getEC2InstanceById("i-1234")).thenReturn(createInstances().get(0));
        PowerMockito.when(ec2Raider.getEC2InstanceById("i-4567")).thenReturn(createInstances().get(1));
        instanceFailureStepDefinitions.blockS3(1);
        Assert.assertEquals(executionStateCache.getUnhealthyInstances().size(), 1);
        Assert.assertTrue(executionStateCache.isBlockS3());
    }


    /**
     * Test disk full on instance.
     *
     * @throws Exception the exception
     */
    @Test
    public void testDiskFullOnInstance() throws Exception
    {
        PowerMockito.when(ec2Raider.getInstancesByName("test")).thenReturn(createInstances());
        instanceFailureStepDefinitions.givenEC2InstanceName("test");
        Assert.assertEquals(executionStateCache.getEc2Tag(), "test");
        Assert.assertTrue(executionStateCache.getInstances().size()>0);


        PowerMockito.when(ec2Raider.getEC2InstanceById("i-1234")).thenReturn(createInstances().get(0));
        PowerMockito.when(ec2Raider.getEC2InstanceById("i-4567")).thenReturn(createInstances().get(1));

        PowerMockito.when(ec2Raider.getInstanceStatusById("i-1234")).thenReturn("running");
        try {
            instanceFailureStepDefinitions.diskFullOnInstance("root", 50, 1);
        }
        catch (Throwable t)
        {
            t.printStackTrace();
        }
        Assert.assertEquals(executionStateCache.getUnhealthyInstances().size(), 1);

    }

    /**
     * Test spike cpu on healthy instances.
     *
     * @throws Exception the exception
     */
    @Test
    public void testSpikeCPUOnHealthyInstances() throws Exception
    {
        PowerMockito.when(ec2Raider.getInstancesByName("test")).thenReturn(createInstances());
        instanceFailureStepDefinitions.givenEC2InstanceName("test");
        Assert.assertEquals(executionStateCache.getEc2Tag(), "test");
        Assert.assertTrue(executionStateCache.getInstances().size()>0);


        PowerMockito.when(ec2Raider.getEC2InstanceById("i-1234")).thenReturn(createInstances().get(0));
        PowerMockito.when(ec2Raider.getEC2InstanceById("i-4567")).thenReturn(createInstances().get(1));
        try {
            instanceFailureStepDefinitions.spikeCPUOnHealthyInstances(1,8);
        }
        catch (Throwable t)
        {
            t.printStackTrace();
        }
        Assert.assertTrue(executionStateCache.isCpuSpiked());
    }

    /**
     * Test inject network latency.
     *
     * @throws Exception the exception
     */
    @Test
    public void testInjectNetworkLatency() throws Exception
    {
        PowerMockito.when(ec2Raider.getInstancesByName("test")).thenReturn(createInstances());
        instanceFailureStepDefinitions.givenEC2InstanceName("test");
        Assert.assertEquals(executionStateCache.getEc2Tag(), "test");
        Assert.assertTrue(executionStateCache.getInstances().size()>0);


        PowerMockito.when(ec2Raider.getEC2InstanceById("i-1234")).thenReturn(createInstances().get(0));
        PowerMockito.when(ec2Raider.getEC2InstanceById("i-4567")).thenReturn(createInstances().get(1));
        try {
            instanceFailureStepDefinitions.injectNetworkLatency(20,80, 1);
        }
        catch (Throwable t)
        {
            t.printStackTrace();
        }
        Assert.assertTrue(executionStateCache.isHealNetwork());
        Assert.assertFalse(executionStateCache.getUnhealthyInstances().isEmpty());
    }

    private List<EC2InstanceTO> createInstances() {
        EC2InstanceTO ec2InstanceTO = new EC2InstanceTO();
        ec2InstanceTO.setAvailabilityZone("us-west-2a");
        ec2InstanceTO.setInstanceId("i-1234");
        ec2InstanceTO.setPrivateIpAddress("10.1.1.1");

        EC2InstanceTO ec2InstanceTO2 = new EC2InstanceTO();
        ec2InstanceTO2.setAvailabilityZone("us-west-2a");
        ec2InstanceTO2.setInstanceId("i-4567");
        ec2InstanceTO2.setPrivateIpAddress("10.1.1.2");

        return Arrays.asList(ec2InstanceTO, ec2InstanceTO2);
    }

    /**
     * The type Instance step definition test context configuration.
     */
    @Configuration
    protected static class InstanceStepDefinitionTestContextConfiguration {


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
         * Ssm raider ssm raider.
         *
         * @return the ssm raider
         */
        @Bean (name={"ssmRaiderBean"})
        public SSMRaiderImpl ssmRaider() {
            return  Mockito.mock(SSMRaiderImpl.class);
        }

        /**
         * System raider system raider.
         *
         * @return the system raider
         */
        @Bean (name={"systemRaiderBean"})
        public SystemRaiderImpl systemRaider() {
            return  Mockito.mock(SystemRaiderImpl.class);
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
         * Instance failure step definitions instance failure step definitions.
         *
         * @return the instance failure step definitions
         */
        @Bean
        public InstanceFailureStepDefinitions instanceFailureStepDefinitions() {
            return new InstanceFailureStepDefinitions();
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
         * Script executor script executor.
         *
         * @return the script executor
         */
        @Bean
        public ScriptExecutor scriptExecutor() {
            return Mockito.mock(ScriptExecutor.class);
        }

        /**
         * Ssm delegator ssm delegator.
         *
         * @return the ssm delegator
         */
        @Bean
        public SSMDelegator ssmDelegator() {
            return Mockito.mock(SSMDelegator.class);
        }

        /**
         * System delegator system delegator.
         *
         * @return the system delegator
         */
        @Bean
        public SystemDelegator systemDelegator() {
            return Mockito.mock(SystemDelegator.class);
        }

    }
}
