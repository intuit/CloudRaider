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

import com.amazonaws.services.simplesystemsmanagement.model.GetCommandInvocationRequest;
import com.amazonaws.services.simplesystemsmanagement.model.GetCommandInvocationResult;
import com.amazonaws.services.simplesystemsmanagement.model.SendCommandRequest;
import com.amazonaws.services.simplesystemsmanagement.model.SendCommandResult;
import com.intuit.cloudraider.commons.SSMDelegator;
import com.intuit.cloudraider.core.interfaces.SSMRaider;
import com.intuit.cloudraider.exceptions.InvalidInputDataException;
import com.intuit.cloudraider.model.Command;
import com.intuit.cloudraider.utils.CommandUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * AWS SSM functionality.
 */
@Component (value = "ssmRaiderBean")
public class SSMRaiderImpl implements SSMRaider{

    /**
     * The Logger.
     */
    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SSMDelegator ssmDelegator;

    /**
     * Instantiates a new Ssm raider.
     */
    public SSMRaiderImpl() {

    }

    /**
     * Runs the commands to execute on each instance provided.
     *
     * @param instances instances to run commands on
     * @param commands list of commands to execute
     * @return sent command's id
     */
    @Override
    public String executeShellCommands(List<String> instances, List<String> commands) {

        SendCommandRequest sendCommandRequest = new SendCommandRequest()
                .withInstanceIds(instances)
                .withComment("Cloud-Raider FMEA Execution")
                .withDocumentName("AWS-RunShellScript")
                .addParametersEntry("commands",commands);

        logger.info("SSMRaider execCommands: " + commands);
        logger.info("SSMRaider Instances " + instances );

        SendCommandResult result = ssmDelegator.getAWSSimpleSystemsManagement().sendCommand(sendCommandRequest);

        return result.getCommand().getCommandId();
    }

    /**
     * Get status of the command execution.
     *
     * @param commandId command id
     * @param instanceId instance id
     * @return String status
     */
    @Override
    public String getCommandStatus(String commandId, String instanceId) {
        return getCommandInvocationResult(commandId, instanceId).getStatus();
    }

    /**
     * Get any output from the command execution.
     *
     * @param commandId command id
     * @param instanceId instance id
     * @return String output
     */
    @Override
    public String getCommandStandardOutput(String commandId, String instanceId) {
        return getCommandInvocationResult(commandId, instanceId).getStandardOutputContent();
    }

    /**
     * Get any errors from the command execution.
     *
     * @param commandId command id
     * @param instanceId instance id
     * @return String errors
     */
    @Override
    public String getCommandErrors(String commandId, String instanceId) {
        return getCommandInvocationResult(commandId, instanceId).getStandardErrorContent();
    }

    /**
     * Gets details of command execution given the instance and command.
     *
     * @param commandId command id
     * @param instanceId instance id
     * @return GetCommandInvocationResult
     */
    private GetCommandInvocationResult getCommandInvocationResult(String commandId, String instanceId)
    {
        GetCommandInvocationRequest getCommandInvocationRequest = new GetCommandInvocationRequest()
                .withCommandId(commandId)
                .withInstanceId(instanceId);

        return ssmDelegator.getAWSSimpleSystemsManagement().getCommandInvocation(getCommandInvocationRequest);
    }

    /**
     * Execute the shell command with the parameters on the given instances.
     *
     * @param instances instances to execute on
     * @param fileName file name
     * @param params parameters for command
     * @return command's id
     */
    @Override
    public String executeShellCommandsFromFile(List<String> instances, String fileName, String... params) {

        List<String> commands = CommandUtility.getCommandsFromFile(fileName, params);

        if (commands.isEmpty()) {
            throw new InvalidInputDataException("No commands available to execute");
        }

        return this.executeShellCommands(instances, commands);
    }

    /**
     * Execute the shell command with the parameters on the given instances.
     *
     * @param instances instances to execute on
     * @param command command to execute
     * @param params parameters for command
     * @return command's id
     */
    @Override
    public String executeShellCommand(List<String> instances, Command command, String... params) {

        return this.executeShellCommandsFromFile(instances, command.getCommandName()+".txt", params);
    }
}
