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
import com.intuit.cloudraider.core.impl.EC2RaiderImpl;
import com.intuit.cloudraider.core.impl.SSMRaiderImpl;
import com.intuit.cloudraider.core.interfaces.EC2Raider;
import com.intuit.cloudraider.cucumber.model.ExecutionStateCache;
import com.intuit.cloudraider.model.BasicCredentials;
import com.intuit.cloudraider.model.Credentials;
import com.intuit.cloudraider.model.EC2InstanceTO;
import com.intuit.cloudraider.utils.CommandUtility;
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


@PowerMockIgnore({"javax.management.*","javax.net.ssl.*"})
@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@PrepareForTest({EC2RaiderImpl.class, SSMRaiderImpl.class,
        SSMStepDefinitions.class, EnvironmentHealerStepDefinitions.class, CommandUtility.class})
public class SSMStepDefinitionsTest {


    @Autowired
    private SSMStepDefinitions ssmStepDefinitions;

    @Autowired
    private   EC2RaiderImpl ec2Raider;

    @Autowired
    private   SSMRaiderImpl ssmRaider;

    @Autowired
    private ExecutionStateCache executionStateCache;

    /**
     * Sets method.
     *
     * @throws Exception the exception
     */
    @Before
    public void setupMethod() throws Exception{


        String commandId = "1234";

        PowerMockito.mockStatic(CommandUtility.class);
        PowerMockito.when(CommandUtility.getCommandsFromFile(Mockito.anyObject(), Mockito.anyObject())).thenReturn(createCommandsList());



        PowerMockito.when(ssmRaider.executeShellCommands(Mockito.anyObject(), Mockito.anyObject())).thenReturn(commandId);


        PowerMockito.when(ssmRaider.executeShellCommand(Mockito.anyObject(),
                Mockito.anyObject(), Mockito.anyObject())).thenReturn(commandId);

        PowerMockito.when(ssmRaider.executeShellCommand(Mockito.anyObject(),
                Mockito.anyObject(), Mockito.anyObject(), Mockito.anyObject())).thenReturn(commandId);


        PowerMockito.when(ssmRaider.getCommandStatus(Mockito.anyObject(), Mockito.anyObject())).thenReturn("Success");


        executionStateCache.clear();



    }

    /**
     * Test execute command on healthy instances.
     *
     * @throws Exception the exception
     */
    @Test
    public void testExecuteCommandOnHealthyInstances() throws Exception
    {
        executionStateCache.setInstances(createInstances());
        ssmStepDefinitions.executeCommandOnHealthyInstances(createCommands(),1);
        Assert.assertEquals(executionStateCache.getCommandId(), "1234");
        Assert.assertTrue(executionStateCache.getInstances().size()>0);

    }

    /**
     * Test terminate command on healthy instances.
     *
     * @throws Exception the exception
     */
    @Test
    public void testTerminateCommandOnHealthyInstances() throws Exception
    {
        executionStateCache.setInstances(createInstances());
        ssmStepDefinitions.terminateCommandOnHealthyInstances("nginx", 1);
        Assert.assertEquals(executionStateCache.getCommandId(), "1234");
        Assert.assertTrue(executionStateCache.getInstances().size()>0);
        Assert.assertTrue(executionStateCache.getProcessNames().contains("nginx"));

    }

    /**
     * Test terminate process in availability zone.
     *
     * @throws Exception the exception
     */
    @Test
    public void testTerminateProcessInAvailabilityZone() throws Exception
    {
        executionStateCache.setInstances(createInstances());
        ssmStepDefinitions.terminateProcessInAvailabilityZone("nginx", 1,"us-west-2c");
        Assert.assertEquals(executionStateCache.getCommandId(), "1234");
        Assert.assertTrue(executionStateCache.getInstances().size()>0);
        Assert.assertTrue(executionStateCache.getProcessNames().contains("nginx"));
        Assert.assertTrue(executionStateCache.getSsmCommandInvocationInstances().size() > 0);
        Assert.assertTrue(executionStateCache.isHealProcess());

    }

    /**
     * Test terminate process on all healthy instances.
     *
     * @throws Exception the exception
     */
    @Test
    public void testTerminateProcessOnAllHealthyInstances() throws Exception
    {
        executionStateCache.setInstances(createInstances());
        try {
            ssmStepDefinitions.terminateProcessOnAllHealthyInstances("nginx");
        }
        catch (Throwable t)
        {
          //  t.printStackTrace();
        }
        Assert.assertEquals(executionStateCache.getCommandId(), "1234");
        Assert.assertTrue(executionStateCache.getInstances().size()>0);
        Assert.assertTrue(executionStateCache.getProcessNames().contains("nginx"));
        Assert.assertTrue(executionStateCache.getSsmCommandInvocationInstances().size() > 0);
        Assert.assertTrue(executionStateCache.isHealProcess());

    }

    /**
     * Test stop process on healthy instances.
     *
     * @throws Exception the exception
     */
    @Test
    public void testStopProcessOnHealthyInstances() throws Exception
    {
        executionStateCache.setInstances(createInstances());
        try {
            ssmStepDefinitions.stopProcessOnHealthyInstances("process","nginx",1);
        }
        catch (Throwable t)
        {
          //  t.printStackTrace();
        }
        Assert.assertEquals(executionStateCache.getCommandId(), "1234");
        Assert.assertTrue(executionStateCache.getInstances().size()>0);
        Assert.assertTrue(executionStateCache.getProcessNames().contains("nginx"));
        Assert.assertTrue(executionStateCache.getSsmCommandInvocationInstances().size() > 0);
        Assert.assertTrue(executionStateCache.isHealProcess());

    }

    /**
     * Test start process on healthy instances.
     *
     * @throws Exception the exception
     */
    @Test
    public void testStartProcessOnHealthyInstances() throws Exception
    {
        executionStateCache.setInstances(createInstances());
        try {
            ssmStepDefinitions.startProcessOnHealthyInstances("process","nginx",1);
        }
        catch (Throwable t)
        {
         //   t.printStackTrace();
        }
        Assert.assertEquals(executionStateCache.getCommandId(), "1234");
        Assert.assertTrue(executionStateCache.getInstances().size()>0);
        Assert.assertTrue(executionStateCache.getSsmCommandInvocationInstances().size() > 0);

    }


    /**
     * Test disk full on instance.
     *
     * @throws Exception the exception
     */
    @Test
    public void testDiskFullOnInstance() throws Exception
    {
        executionStateCache.setInstances(createInstances());
        try {
            ssmStepDefinitions.diskFullOnInstance("root", 95, 1);
        }
        catch (Throwable t)
        {
         //   t.printStackTrace();
        }
        Assert.assertEquals(executionStateCache.getCommandId(), "1234");
        Assert.assertTrue(executionStateCache.getInstances().size()>0);
        Assert.assertTrue(executionStateCache.getSsmCommandInvocationInstances().size() > 0);

    }

    /**
     * Test ram disk full on instance.
     *
     * @throws Exception the exception
     */
    @Test
    public void testRamDiskFullOnInstance() throws Exception
    {
        executionStateCache.setInstances(createInstances());
        try {
            ssmStepDefinitions.ramDiskFullOnInstance(10, 1);
        }
        catch (Throwable t)
        {
           // t.printStackTrace();
        }
        Assert.assertEquals(executionStateCache.getCommandId(), "1234");
        Assert.assertTrue(executionStateCache.getInstances().size()>0);
        Assert.assertTrue(executionStateCache.isRamDiskFull());
        Assert.assertTrue(executionStateCache.getSsmCommandInvocationInstances().size() > 0);

    }

    /**
     * Test block domain.
     *
     * @throws Exception the exception
     */
    @Test
    public void testBlockDomain() throws Exception
    {
        executionStateCache.setInstances(createInstances());
        try {
            ssmStepDefinitions.blockDomain("www.test.com", 1);
        }
        catch (Throwable t)
        {
         //   t.printStackTrace();
        }
        Assert.assertEquals(executionStateCache.getCommandId(), "1234");
        Assert.assertTrue(executionStateCache.getInstances().size()>0);
        Assert.assertEquals(executionStateCache.getBlockedDomains().get(0), "www.test.com");
        Assert.assertTrue(executionStateCache.getSsmCommandInvocationInstances().size() > 0);

    }

    /**
     * Test block port.
     *
     * @throws Exception the exception
     */
    @Test
    public void testBlockPort() throws Exception
    {
        executionStateCache.setInstances(createInstances());
        try {
            ssmStepDefinitions.blockPort(8080, 1);
        }
        catch (Throwable t)
        {
         //   t.printStackTrace();
        }
        Assert.assertEquals(executionStateCache.getCommandId(), "1234");
        Assert.assertTrue(executionStateCache.getInstances().size()>0);
        Assert.assertTrue(executionStateCache.getPortNums().contains(8080));
        Assert.assertTrue(executionStateCache.isBlockPort());
        Assert.assertTrue(executionStateCache.getSsmCommandInvocationInstances().size() > 0);

    }

    /**
     * Test block dynamo db.
     *
     * @throws Exception the exception
     */
    @Test
    public void testBlockDynamoDB() throws Exception
    {
        executionStateCache.setInstances(createInstances());
        try {
            ssmStepDefinitions.blockDynamoDB(1);
        }
        catch (Throwable t)
        {
          //  t.printStackTrace();
        }
        Assert.assertEquals(executionStateCache.getCommandId(), "1234");
        Assert.assertTrue(executionStateCache.getInstances().size()>0);
        Assert.assertTrue(executionStateCache.isBlockDynamoDB());
        Assert.assertTrue(executionStateCache.getSsmCommandInvocationInstances().size() > 0);

    }

    /**
     * Test block s 3.
     *
     * @throws Exception the exception
     */
    @Test
    public void testBlockS3() throws Exception
    {
        executionStateCache.setInstances(createInstances());
        try {
            ssmStepDefinitions.blockS3(1);
        }
        catch (Throwable t)
        {
         //   t.printStackTrace();
        }
        Assert.assertEquals(executionStateCache.getCommandId(), "1234");
        Assert.assertTrue(executionStateCache.getInstances().size()>0);
        Assert.assertTrue(executionStateCache.isBlockS3());
        Assert.assertTrue(executionStateCache.getSsmCommandInvocationInstances().size() > 0);

    }

    /**
     * Test block outbound port.
     *
     * @throws Exception the exception
     */
    @Test
    public void testBlockOutboundPort() throws Exception
    {
        executionStateCache.setInstances(createInstances());
        try {
            ssmStepDefinitions.blockOutboundPort(8080, 1);
        }
        catch (Throwable t)
        {
           // t.printStackTrace();
        }
        Assert.assertEquals(executionStateCache.getCommandId(), "1234");
        Assert.assertTrue(executionStateCache.getInstances().size()>0);
        Assert.assertTrue(executionStateCache.getPortNums().contains(8080));
        Assert.assertTrue(executionStateCache.isBlockPort());
        Assert.assertTrue(executionStateCache.getSsmCommandInvocationInstances().size() > 0);

    }

    /**
     * Test inject network latency.
     *
     * @throws Exception the exception
     */
    @Test
    public void testInjectNetworkLatency() throws Exception
    {
        executionStateCache.setInstances(createInstances());
        try {
            ssmStepDefinitions. injectNetworkLatency(80, 100, 1);
        }
        catch (Throwable t)
        {
          //  t.printStackTrace();
        }
        Assert.assertEquals(executionStateCache.getCommandId(), "1234");
        Assert.assertTrue(executionStateCache.getInstances().size()>0);
        Assert.assertTrue(executionStateCache.isHealNetwork());
        Assert.assertTrue(executionStateCache.getSsmCommandInvocationInstances().size() > 0);

    }

    /**
     * Test corrupt network.
     *
     * @throws Exception the exception
     */
    @Test
    public void testCorruptNetwork() throws Exception
    {
        executionStateCache.setInstances(createInstances());
        try {
            ssmStepDefinitions.corruptNetwork(50,  1);
        }
        catch (Throwable t)
        {
           // t.printStackTrace();
        }
        Assert.assertEquals(executionStateCache.getCommandId(), "1234");
        Assert.assertTrue(executionStateCache.getInstances().size()>0);
        Assert.assertTrue(executionStateCache.isHealNetwork());
        Assert.assertTrue(executionStateCache.getSsmCommandInvocationInstances().size() > 0);

    }

    /**
     * Test inject packet loss.
     *
     * @throws Exception the exception
     */
    @Test
    public void testInjectPacketLoss() throws Exception
    {
        executionStateCache.setInstances(createInstances());
        try {
            ssmStepDefinitions.injectPacketLoss(50,  1);
        }
        catch (Throwable t)
        {
           // t.printStackTrace();
        }
        Assert.assertEquals(executionStateCache.getCommandId(), "1234");
        Assert.assertTrue(executionStateCache.getInstances().size()>0);
        Assert.assertTrue(executionStateCache.isHealNetwork());
        Assert.assertTrue(executionStateCache.getSsmCommandInvocationInstances().size() > 0);

    }

    /**
     * Test spike cpu on healthy instances.
     *
     * @throws Exception the exception
     */
    @Test
    public void testSpikeCPUOnHealthyInstances() throws Exception
    {
        executionStateCache.setInstances(createInstances());
        ssmStepDefinitions.spikeCPUOnHealthyInstances(1,8);
        Assert.assertEquals(executionStateCache.getCommandId(), "1234");
        Assert.assertTrue(executionStateCache.getInstances().size()>0);
        Assert.assertTrue(executionStateCache.isCpuSpiked());
        Assert.assertTrue(executionStateCache.getSsmCommandInvocationInstances().size() > 0);

    }


    /**
     * Assert command execution status.
     *
     * @throws Exception the exception
     */
    @Test
    public void assertCommandExecutionStatus() throws Exception
    {
        executionStateCache.setSsmCommandInvocationInstances(createInstances());
        executionStateCache.setCommandId("1234");
        ssmStepDefinitions.assertCommandExecutionStatus("Success");
        Assert.assertEquals(executionStateCache.getCommandId(), "1234");
        Assert.assertTrue(executionStateCache.getSsmCommandInvocationInstances().size()>0);

    }

    /**
     * Assert command execution status exception.
     *
     * @throws Exception the exception
     */
    @Test (expected = AssertionError.class)
    public void assertCommandExecutionStatusException() throws Exception
    {
        executionStateCache.setSsmCommandInvocationInstances(createInstances());
        executionStateCache.setCommandId("1234");
        ssmStepDefinitions.assertCommandExecutionStatus("fail");

    }

    /**
     * Assert command execution status no instances.
     *
     * @throws Exception the exception
     */
    @Test (expected = RuntimeException.class)
    public void assertCommandExecutionStatusNoInstances() throws Exception
    {
        ssmStepDefinitions.assertCommandExecutionStatus("fail");
    }




    private List<EC2InstanceTO> createInstances()
    {
        EC2InstanceTO ec2InstanceTO = new EC2InstanceTO();
        ec2InstanceTO.setAvailabilityZone("us-west-2a");
        ec2InstanceTO.setInstanceId("i-1234");
        ec2InstanceTO.setPrivateIpAddress("10.1.1.1");


        EC2InstanceTO ec2IntanceTO2 = new EC2InstanceTO();
        ec2IntanceTO2.setAvailabilityZone("us-west-2a");
        ec2IntanceTO2.setInstanceId("i-4567");
        ec2IntanceTO2.setPrivateIpAddress("10.1.1.2");
        return Arrays.asList(ec2InstanceTO, ec2IntanceTO2);
    }

    private List<String> createCommandsList()
    {
        return Arrays.asList("#Comment Line","ps -aef |grep java");
    }

    private String createCommands()
    {
        return "#Comment Line,ps -aef |grep java";
    }

    /**
     * The type Ssm step definition test context configuration.
     */
    @Configuration
    protected static class SSMStepDefinitionTestContextConfiguration {


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
         * Credentials credentials.
         *
         * @return the credentials
         */
        @Bean
        public Credentials credentials() {
            return new BasicCredentials();
        }

        /**
         * Ssm step definitions ssm step definitions.
         *
         * @return the ssm step definitions
         */
        @Bean
        public SSMStepDefinitions ssmStepDefinitions() {
            return new SSMStepDefinitions();
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
         * Ssm delegator ssm delegator.
         *
         * @return the ssm delegator
         */
        @Bean
        public SSMDelegator ssmDelegator() {
            return Mockito.mock(SSMDelegator.class);
        }

    }


}
