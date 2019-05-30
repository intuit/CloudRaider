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

import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagement;
import com.amazonaws.services.simplesystemsmanagement.model.Command;
import com.amazonaws.services.simplesystemsmanagement.model.GetCommandInvocationResult;
import com.amazonaws.services.simplesystemsmanagement.model.SendCommandResult;
import com.intuit.cloudraider.commons.SSMDelegator;
import com.intuit.cloudraider.model.BasicCredentials;
import com.intuit.cloudraider.model.Credentials;
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
@PrepareForTest({SSMRaiderImpl.class, Credentials.class, SSMDelegator.class,CommandUtility.class})
public class SSMRaiderImplTest {


    @Autowired
    private  SSMRaiderImpl ssmRaiderUnderTest;

    @Autowired
    private  SSMDelegator ssmDelegator;

    private  AWSSimpleSystemsManagement amazonSSM;
    private  GetCommandInvocationResult getCommandInvocationResult;

    /**
     * Sets method.
     *
     * @throws Exception the exception
     */
    @Before
    public void setupMethod() throws Exception{

        amazonSSM = PowerMockito.mock(AWSSimpleSystemsManagement.class);
        PowerMockito.when(ssmDelegator.getAWSSimpleSystemsManagement()).thenReturn(amazonSSM);


        Command awsCommand = new Command().withCommandId("777");
        SendCommandResult sendCommandResult = PowerMockito.mock(SendCommandResult.class);
        PowerMockito.when(amazonSSM.sendCommand(Mockito.anyObject())).thenReturn(sendCommandResult);
        PowerMockito.when(sendCommandResult.getCommand()).thenReturn(awsCommand);

        PowerMockito.mockStatic(CommandUtility.class);
        PowerMockito.when(CommandUtility.getCommandsFromFile(Mockito.anyObject(), Mockito.anyObject())).thenReturn(createCommandsList());

        getCommandInvocationResult = PowerMockito.mock(GetCommandInvocationResult.class);
        PowerMockito.when(amazonSSM.getCommandInvocation(Mockito.anyObject())).thenReturn(getCommandInvocationResult);

    }

    /**
     * Test execute shell commands.
     *
     * @throws Exception the exception
     */
    @Test
    public void testExecuteShellCommands() throws Exception{

        String commandId = ssmRaiderUnderTest.executeShellCommands(createInstancesList(), createCommandsList());
        Assert.assertEquals(commandId, "777");
    }

    /**
     * Test execute shell command.
     *
     * @throws Exception the exception
     */
    @Test
    public void testExecuteShellCommand() throws Exception{

        String commandId = ssmRaiderUnderTest.executeShellCommand(createInstancesList(),com.intuit.cloudraider.model.Command.KILLPROCESS,"java" );
        Assert.assertEquals(commandId, "777");
    }


    /**
     * Test execute shell commands from file.
     *
     * @throws Exception the exception
     */
    @Test
    public void testExecuteShellCommandsFromFile() throws Exception{

        String commandId = ssmRaiderUnderTest.executeShellCommandsFromFile(createInstancesList(),"test.sh","java" );
        Assert.assertEquals(commandId, "777");
    }

    /**
     * Test get command status.
     *
     * @throws Exception the exception
     */
    @Test
    public void testGetCommandStatus() throws Exception{

        PowerMockito.when(getCommandInvocationResult.getStatus()).thenReturn("Success");
        String status = ssmRaiderUnderTest.getCommandStatus("777" ,"i-1234");
        Assert.assertEquals(status, "Success");
    }

    /**
     * Test get command standard output.
     *
     * @throws Exception the exception
     */
    @Test
    public void testGetCommandStandardOutput() throws Exception{

        PowerMockito.when(getCommandInvocationResult.getStandardOutputContent()).thenReturn("success execution");
        String standardOutput = ssmRaiderUnderTest.getCommandStandardOutput("777" ,"i-1234");
        Assert.assertEquals(standardOutput, "success execution");
    }


    /**
     * Test get command errors.
     *
     * @throws Exception the exception
     */
    @Test
    public void testGetCommandErrors() throws Exception{

        PowerMockito.when(getCommandInvocationResult.getStandardErrorContent()).thenReturn("");
        String errors = ssmRaiderUnderTest.getCommandErrors("777" ,"i-1234");
        Assert.assertTrue(errors.isEmpty());
    }

    private List<String> createCommandsList()
    {
        return Arrays.asList("#Comment Line","ps -aef |grep java");
    }


    private List<String> createInstancesList()
    {
        return Arrays.asList("i-1234","i-4567");
    }

    /**
     * The type Ssm raider impl test context configuration.
     */
    @Configuration
    protected static class SSMRaiderImplTestContextConfiguration {

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
         * Ssm raider ssm raider.
         *
         * @return the ssm raider
         */
        @Bean
        public SSMRaiderImpl ssmRaider() {
            return new SSMRaiderImpl();
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
