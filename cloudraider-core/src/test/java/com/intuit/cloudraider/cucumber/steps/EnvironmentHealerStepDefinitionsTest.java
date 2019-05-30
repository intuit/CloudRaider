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

import com.intuit.cloudraider.commons.SSMDelegator;
import com.intuit.cloudraider.commons.SystemDelegator;
import com.intuit.cloudraider.core.impl.SSMRaiderImpl;
import com.intuit.cloudraider.core.impl.SystemRaiderImpl;
import com.intuit.cloudraider.core.interfaces.SSMRaider;
import com.intuit.cloudraider.core.interfaces.SystemRaider;
import com.intuit.cloudraider.cucumber.model.ExecutionStateCache;
import com.intuit.cloudraider.cucumber.util.ScriptExecutor;
import com.intuit.cloudraider.model.BasicCredentials;
import com.intuit.cloudraider.model.Credentials;
import com.intuit.cloudraider.model.EC2InstanceTO;
import com.intuit.cloudraider.utils.CommandUtility;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


@PowerMockIgnore({"javax.management.*","javax.net.ssl.*"})
@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@PrepareForTest({ScriptExecutor.class, SSMRaiderImpl.class,
        EnvironmentHealerStepDefinitions.class,CommandUtility.class})

public class EnvironmentHealerStepDefinitionsTest {


    @Autowired
    private EnvironmentHealerStepDefinitions environmentHealerStepDefinitions;

    @Autowired
    private   SSMRaiderImpl ssmRaider;

    @Autowired
    private   ScriptExecutor scriptExecutor;


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

        PowerMockito.when(ssmRaider.executeShellCommands(Mockito.anyObject(), Mockito.anyObject())).thenReturn(commandId);


        PowerMockito.when(ssmRaider.executeShellCommand(Mockito.anyObject(),
                Mockito.anyObject(), Mockito.anyObject())).thenReturn(commandId);

        PowerMockito.when(ssmRaider.executeShellCommand(Mockito.anyObject(),
                Mockito.anyObject(), Mockito.anyObject(), Mockito.anyObject())).thenReturn(commandId);


        PowerMockito.when(ssmRaider.getCommandStatus(Mockito.anyObject(), Mockito.anyObject())).thenReturn("Success");


        PowerMockito.mockStatic(CommandUtility.class);
        PowerMockito.when(CommandUtility.getCommandsFromFile(Mockito.anyObject(), Mockito.anyObject())).thenReturn(createCommandsList());

    }

    /**
     * Test recover ssm failures.
     *
     * @throws Exception the exception
     */
    @Test
    public void testRecoverSSMFailures() throws Exception
    {
         executionStateCache.setSsmCommandInvocationInstances(createInstances());
         executionStateCache.setHealProcess(true);
         executionStateCache.addProcessName("nginx");
         executionStateCache.setRamDiskFull(true);
         executionStateCache.addPortNum(1234);
         executionStateCache.setHealNetwork(true);
         executionStateCache.setBlockPort(true);
         executionStateCache.setBlockedDomains(Arrays.asList("yahoo.com"));
        executionStateCache.setCpuSpiked(true);

         environmentHealerStepDefinitions.recover();


    }

    /**
     * Test recover system failures.
     *
     * @throws Exception the exception
     */
    @Test
    public void testRecoverSystemFailures() throws Exception
    {

        executionStateCache.setUnhealthyInstances(this.createInstances());
        executionStateCache.setHealProcess(true);
        executionStateCache.addProcessName("nginx");
        executionStateCache.setRamDiskFull(true);
        executionStateCache.addPortNum(1234);
        executionStateCache.setHealNetwork(true);
        executionStateCache.setBlockPort(true);
        executionStateCache.setBlockedDomains(Arrays.asList("yahoo.com"));
        executionStateCache.setCpuSpiked(true);

        environmentHealerStepDefinitions.recover();

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
        return new ArrayList<EC2InstanceTO>(Arrays.asList(ec2InstanceTO, ec2IntanceTO2));
    }

    private List<String> createCommandsList()
    {
        return Arrays.asList("#Comment Line","ps -aef |grep java");
    }


    /**
     * The type Environment healer step definition test context configuration.
     */
    @Configuration
    protected static class EnvironmentHealerStepDefinitionTestContextConfiguration {

        /**
         * Script executor script executor.
         *
         * @return the script executor
         */
        @Bean (name={"scriptExecutor"})
        public ScriptExecutor scriptExecutor() {
            return Mockito.mock(ScriptExecutor.class);
        }

        /**
         * Ssm raider ssm raider.
         *
         * @return the ssm raider
         */
        @Bean (name={"ssmRaiderBean"})
        public SSMRaider ssmRaider() {
            return  Mockito.mock(SSMRaiderImpl.class);
        }

        /**
         * System raider system raider.
         *
         * @return the system raider
         */
        @Bean (name={"systemRaiderBean"})
        public SystemRaider systemRaider() {
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
         * Environment healer step definitions environment healer step definitions.
         *
         * @return the environment healer step definitions
         */
        @Bean
        public EnvironmentHealerStepDefinitions environmentHealerStepDefinitions() {
            return new EnvironmentHealerStepDefinitions();
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
         * System delegator system delegator.
         *
         * @return the system delegator
         */
        @Bean
        public SystemDelegator systemDelegator() {
            return Mockito.mock(SystemDelegator.class);
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
