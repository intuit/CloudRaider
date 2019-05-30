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

import com.intuit.cloudraider.core.interfaces.SSMRaider;
import com.intuit.cloudraider.cucumber.interfaces.EC2StepFunctions;
import com.intuit.cloudraider.cucumber.model.ExecutionStateCache;
import com.intuit.cloudraider.model.Command;
import com.intuit.cloudraider.model.EC2InstanceTO;
import com.intuit.cloudraider.utils.CommandUtility;
import com.intuit.cloudraider.utils.Ec2Utils;
import cucumber.api.java.en.When;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Cucumber Step Definitions for AWS Systems Manager functionality.
 */
public class SSMStepDefinitions implements EC2StepFunctions {

    @Autowired
    @Qualifier("ssmRaiderBean")
    private SSMRaider ssmRaider;

    /**
     * The Logger.
     */
    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ExecutionStateCache executionStateCache;

    /**
     * Instantiates a new Ssm step definitions.
     */
    public SSMStepDefinitions() {
    }

    /**
     * Gets execution state cache.
     *
     * @return the execution state cache
     */
    public ExecutionStateCache getExecutionStateCache() {
        return executionStateCache;
    }

    public void setExecutionStateCache(ExecutionStateCache executionStateCache) {
        this.executionStateCache = executionStateCache;
    }

    /**
     * Executes the given command on the specified number of instances.
     *
     * @param command  command to execute
     * @param numHosts number of instances
     * @return the ssm step definitions
     */
    @When("^executeCommand  \"([^\"]*)\" on (\\d+) instance$")
    public SSMStepDefinitions executeCommandOnHealthyInstances(String command, int numHosts) {

        List<EC2InstanceTO> instanceList = getInstancesForExecution(numHosts);
        List<String> commands = Arrays.asList(command.split("\\s*,\\s*"));

        String commandId = ssmRaider.executeShellCommands(Ec2Utils.generateInstanceIdList(instanceList), commands);
        executionStateCache.setCommandId(commandId);
        executionStateCache.setSsmCommandInvocationInstances(instanceList);

        return this;
    }

    /**
     * Terminate the given process on the instance.
     *
     * @param processName process name
     * @param instanceID instance id
     * @param instanceIP instance private ip
     */
    @Override
    public void terminateProcessGivenInstance(String processName, String instanceID, String instanceIP) {
        List<String> list = new ArrayList<>();
        list.add(instanceID);

        executionStateCache.addProcessName(processName);
        String commandId = ssmRaider.executeShellCommand(list, Command.KILLPROCESS, processName);

        executionStateCache.setCommandId(commandId);
        executionStateCache.addSsmCommandInvocationInstances(executionStateCache.findEC2InstanceGivenID(instanceID));
        executionStateCache.setHealProcess(true);
    }

    /**
     * Terminate the given process on the specified number of instances.
     *
     * @param processName process name
     * @param numHosts    number of instances
     * @return the ssm step definitions
     */
    @When("^SSM terminate process  \"([^\"]*)\" on (\\d+) instance$")
    public SSMStepDefinitions terminateCommandOnHealthyInstances(String processName, int numHosts) {
        List<EC2InstanceTO> instanceList = getInstancesForExecution(numHosts);

        executionStateCache.addProcessName(processName);
        String commandId = ssmRaider.executeShellCommand(Ec2Utils.generateInstanceIdList(instanceList), Command.KILLPROCESS, processName);

        executionStateCache.setCommandId(commandId);
        executionStateCache.setSsmCommandInvocationInstances(instanceList);
        executionStateCache.setHealProcess(true);

        return this;
    }

    /**
     * Assert that current execution status matches what is expected
     *
     * @param expectedCommandStatus expected status
     */
    @When("^assertCommand execution status = \"([^\"]*)\"$")
    public void assertCommandExecutionStatus(final String expectedCommandStatus) {
        String commandId = executionStateCache.getCommandId();
        List<EC2InstanceTO> instances = executionStateCache.getSsmCommandInvocationInstances();
        if (commandId == null || commandId.isEmpty()) {
            throw new RuntimeException("Missing command-id information, unable to check status");
        }

        if (instances == null || instances.isEmpty()) {
            throw new RuntimeException("No instances are available to check command execution status");
        }

        instances.stream().forEach(
                instanceTO ->
                {
                    String actualCommandStatus = ssmRaider.getCommandStatus(commandId, instanceTO.getInstanceId());
                    org.testng.Assert.assertEquals(actualCommandStatus, expectedCommandStatus);
                }
        );

    }

    /**
     * Terminate the given process on the specified number of instances within the denoted availability zone.
     *
     * @param processName process to terminate
     * @param numHosts    number of hosts
     * @param zoneId      availability zone id
     * @return the ssm step definitions
     */
    @When("^SSM terminate process  \"([^\"]*)\" on (\\d+) instance in zone \"([^\"]*)\"$")
    public SSMStepDefinitions terminateProcessInAvailabilityZone(String processName, int numHosts, String zoneId) {
        this.executeCommand(numHosts, Command.KILLPROCESS, processName);
        executionStateCache.addProcessName(processName);
        executionStateCache.setHealProcess(true);

        return this;
    }

    /**
     * Spike the given number of cores on the instance.
     *
     * @param cores number of cores
     * @param instanceID instance id
     * @param instanceIP instance private ip
     */
    @Override
    public void spikeCPUGivenInstance(int cores, String instanceID, String instanceIP) {
        List<String> commands = new ArrayList<>();
        List<String> list = new ArrayList<>();
        list.add(instanceID);

        for (int i = 0; i < cores; i++) {
            commands.addAll(CommandUtility.getCommandsFromFile(Command.SPIKECPU.getCommandName() + ".txt"));
        }

        String commandId = ssmRaider.executeShellCommands(list, commands);

        executionStateCache.setCpuSpiked(true);
        executionStateCache.setCommandId(commandId);
        executionStateCache.addSsmCommandInvocationInstances(executionStateCache.findEC2InstanceGivenID(instanceID));
    }

    /**
     * Spikes the given number of cores on each instance for the specified number of instances.
     *
     * @param numHosts number of instances
     * @param cores    number of cores to spike
     * @return the ssm step definitions
     * @throws Exception the exception
     */
    @When("^SSM CPU spike on (\\d+) instances for (\\d+) cores$")
    public SSMStepDefinitions spikeCPUOnHealthyInstances(int numHosts, int cores) throws Exception {
        List<EC2InstanceTO> instances = executionStateCache.getInstances();
        if (instances == null || instances.isEmpty()) {
            throw new RuntimeException("No Instances are vailable");
        }

        if (cores <= 0) {
            throw new RuntimeException("Invalid number of cores provided");
        }

        if (numHosts > instances.size()) {
            numHosts = instances.size();
        }

        List<EC2InstanceTO> instanceList = instances;
        if (instanceList.size() != numHosts) {
            instanceList = instances.subList(0, numHosts);
        }

        List<String> commands = new ArrayList<>();
        for (int i = 0; i < cores; i++) {
            commands.addAll(CommandUtility.getCommandsFromFile(Command.SPIKECPU.getCommandName() + ".txt"));
        }

        String commandId = ssmRaider.executeShellCommands(Ec2Utils.generateInstanceIdList(instanceList), commands);


        executionStateCache.setCpuSpiked(true);
        executionStateCache.setCommandId(commandId);
        executionStateCache.setSsmCommandInvocationInstances(instanceList);

        return this;
    }

    /**
     * Terminates the given process on all available instances.
     *
     * @param processName process name
     * @return the ssm step definitions
     * @throws Throwable the throwable
     */
    @When("^SSM terminate process \"([^\"]*)\"$")
    public SSMStepDefinitions terminateProcessOnAllHealthyInstances(String processName) throws Throwable {
        this.executeCommand(executionStateCache.getInstances().size(), Command.KILLPROCESS, processName);
        executionStateCache.addProcessName(processName);
        executionStateCache.setHealProcess(true);

        return this;
    }

    /**
     * Add "size" GB to the specified volume.
     *
     * @param volumeType name of volume
     * @param size size of disk
     * @param instanceID instance id
     * @param instanceIP instance private ip
     */
    @Override
    public void diskFullGivenInstance(String volumeType, int size, String instanceID, String instanceIP) {
        List<String> list = new ArrayList<>();
        list.add(instanceID);

        String commandId = ssmRaider.executeShellCommand(list, Command.DISKFULL, volumeType, String.valueOf(size));
        executionStateCache.setCommandId(commandId);
        executionStateCache.addSsmCommandInvocationInstances(executionStateCache.findEC2InstanceGivenID(instanceID));
    }

    /**
     * Add "size" GB to the specified volume on each instance for the given number of instances.
     *
     * @param volumeType name of volume
     * @param size       size of disk
     * @param numHosts   number of instances
     * @return the ssm step definitions
     * @throws Throwable the throwable
     */
    @When("^SSM \"([^\"]*)\" disk full with (\\d+) GB on (\\d+) instance$")
    public SSMStepDefinitions diskFullOnInstance(String volumeType, int size, int numHosts) throws Throwable {

        this.executeCommand(numHosts, Command.DISKFULL, volumeType, String.valueOf(size));
        return this;

    }

    /**
     * Add "size" GB to the RAM on each instance for the given number of instances.
     *
     * @param size     size of disk
     * @param numHosts number of instances
     * @return the ssm step definitions
     * @throws Throwable the throwable
     */
    @When("^SSM RAM disk full with (\\d+) GB on (\\d+) instance$")
    public SSMStepDefinitions ramDiskFullOnInstance(int size, int numHosts) throws Throwable {

        this.executeCommand(numHosts, Command.RAMDISKFULL, String.valueOf(size));
        executionStateCache.setRamDiskFull(true);

        return this;

    }

    /**
     * Block the given domain name on the specified number of instances.
     *
     * @param domainName domain name
     * @param numHosts   number of instances
     * @return the ssm step definitions
     */
    @When("^SSM block domain \"([^\"]*)\" on (\\d+) instances$")
    public SSMStepDefinitions blockDomain(String domainName, int numHosts) {
        this.executeCommand(numHosts, Command.BLOCKDOMAIN, domainName);
        executionStateCache.getBlockedDomains().add(domainName);
        return this;
    }


    /**
     * Block dynamo db ssm step definitions.
     *
     * @param numHosts the num hosts
     * @return the ssm step definitions
     */
    @When("^SSM block DynamoDB on (\\d+) instances$")
    public SSMStepDefinitions blockDynamoDB( int numHosts)
    {
        this.executeCommand(numHosts, Command.BLOCKDYNAMODB,null);
        executionStateCache.setBlockDynamoDB(true);
        return this;
    }


    /**
     * Block s 3 ssm step definitions.
     *
     * @param numHosts the num hosts
     * @return the ssm step definitions
     */
    @When("^SSM block S3 on (\\d+) instances$")
    public SSMStepDefinitions blockS3(int numHosts)
    {
        this.executeCommand(numHosts, Command.BLOCKS3,null);
        executionStateCache.setBlockS3(true);
        return this;
    }

    /**
     * Stop the given process on the specified number of instances.
     *
     * @param serviceOrProcessType service or process (UNUSED)
     * @param processName          process name
     * @param numHosts             number of instances
     * @return the ssm step definitions
     */
    @When("^SSM stop (service|process) \"([^\"]*)\" on (\\d+) instance$")
    public SSMStepDefinitions stopProcessOnHealthyInstances(String serviceOrProcessType, String processName, int numHosts) {
        executionStateCache.addProcessName(processName);
        this.executeCommand(numHosts, Command.STOPSERVICE, processName);
        executionStateCache.setHealProcess(true);

        return this;
    }

    /**
     * Start the given process on the specified number of instances.
     *
     * @param serviceOrProcessType service or process (UNUSED)
     * @param processName          process name
     * @param numHosts             number of instances
     * @return the ssm step definitions
     */
    @When("^SSM start (service|process) \"([^\"]*)\" on (\\d+) instance$")
    public SSMStepDefinitions startProcessOnHealthyInstances(String serviceOrProcessType, String processName, int numHosts) {

        this.executeCommand(numHosts, Command.STARTSERVICE, processName);
        return this;
    }

    /**
     * Corrupt the given percentage of each instance's traffic for the specified number of instances.
     *
     * @param corruptPercent corruption percentage (0 to 100, inclusive)
     * @param numHosts       number of instances.
     * @return the ssm step definitions
     */
    @When("^SSM corrupt network (\\d+) percent on (\\d+) instances$")
    public SSMStepDefinitions corruptNetwork(int corruptPercent, int numHosts) {
        this.executeCommand(numHosts, Command.CORRUPTNETWORK, String.valueOf(corruptPercent));
        executionStateCache.setHealNetwork(true);
        return this;

    }

    /**
     * Injects network latency within the bounds on the default interface.
     *
     * @param lowerBound lower bound for latency
     * @param upperBound upper bound for latency
     * @param instanceID instance id
     * @param instanceIP instance private ip
     */
    @Override
    public void injectNetworkLatencyGivenInstance(int lowerBound, int upperBound, String instanceID, String instanceIP) {
        List<String> list = new ArrayList<>();
        list.add(instanceID);

        String commandId = ssmRaider.executeShellCommand(list, Command.DELAYNETWORK, String.valueOf(lowerBound), String.valueOf(upperBound));
        executionStateCache.setCommandId(commandId);
        executionStateCache.addSsmCommandInvocationInstances(executionStateCache.findEC2InstanceGivenID(instanceID));
        executionStateCache.setHealNetwork(true);
    }

    /**
     * Inject network latency within the bounds on the default interface for the given number of instances.
     *
     * @param lowerBound lower bound for latency
     * @param upperBound upper bound for latency
     * @param numHosts   number of instances
     * @return the ssm step definitions
     */
    @When("^SSM inject network latency (\\d+) ms to (\\d+) ms on (\\d+) instances$")
    public SSMStepDefinitions injectNetworkLatency(int lowerBound, int upperBound, int numHosts) {

        this.executeCommand(numHosts, Command.DELAYNETWORK, String.valueOf(lowerBound), String.valueOf(upperBound));
        executionStateCache.setHealNetwork(true);

        return this;

    }

    /**
     * Inject packet loss at the given percentage for the specified number of instances.
     *
     * @param percentLoss percentage of packet loss
     * @param numHosts    number of instances
     * @return the ssm step definitions
     */
    @When("^SSM inject network packet loss (\\d+) percent on (\\d+) instances$")
    public SSMStepDefinitions injectPacketLoss(int percentLoss, int numHosts) {
        this.executeCommand(numHosts, Command.PACKETLOSS, String.valueOf(percentLoss));
        executionStateCache.setHealNetwork(true);
        return this;
    }

    /**
     * Block traffic on the given port number.
     *
     * @param portNum port number to block
     * @param instanceID instance id
     * @param instanceIP instance private ip
     */
    @Override
    public void blockPortGivenInstance(int portNum, String instanceID, String instanceIP) {
        List<String> list = new ArrayList<>();
        list.add(instanceID);

        String commandId = ssmRaider.executeShellCommand(list, Command.BLOCKPORT, String.valueOf(portNum));
        executionStateCache.addSsmCommandInvocationInstances(executionStateCache.findEC2InstanceGivenID(instanceID));
        executionStateCache.setCommandId(commandId);
        executionStateCache.setBlockPort(true);
        executionStateCache.addPortNum(portNum);
    }

    /**
     * Block the given port number on the specified number of instances.
     *
     * @param portNum  port number
     * @param numHosts number of instances
     * @return the ssm step definitions
     */
    @When("^SSM block network port (\\d+) on (\\d+) instances$")
    public SSMStepDefinitions blockPort(int portNum, int numHosts) {
        this.executeCommand(numHosts, Command.BLOCKPORT, String.valueOf(portNum));
        executionStateCache.setBlockPort(true);
        executionStateCache.addPortNum(portNum);
        return this;
    }

    /**
     * Block outgoing traffic for the given port number on the specified number of instances
     *
     * @param portNum  port number
     * @param numHosts number of instances
     * @return the ssm step definitions
     */
    @When("^SSM block outbound network port (\\d+) on (\\d+) instances$")
    public SSMStepDefinitions blockOutboundPort(int portNum, int numHosts) {
        this.executeCommand(numHosts, Command.BLOCKOUTBOUNDPORT, String.valueOf(portNum));
        executionStateCache.setBlockPort(true);
        executionStateCache.addPortNum(portNum);
        return this;
    }

    /**
     * Executes the provided command with its parameters on the specified number of instances.
     *
     * @param numHosts number of instances
     * @param command command to execute
     * @param params parameters (multiple parameters allowed)
     */
    private void executeCommand(int numHosts, Command command, String... params) {
        List<EC2InstanceTO> instances = executionStateCache.getInstances();

        if (instances == null || instances.isEmpty()) {
            throw new RuntimeException("No Instances are vailable");
        }

        if (numHosts > instances.size()) {
            numHosts = instances.size();
        }

        List<EC2InstanceTO> instanceList = instances;
        if (instanceList.size() != numHosts) {
            instanceList = instances.subList(0, numHosts);
        }

        String commandId = ssmRaider.executeShellCommand(Ec2Utils.generateInstanceIdList(instanceList), command, params);

        executionStateCache.setCommandId(commandId);
        executionStateCache.setSsmCommandInvocationInstances(instanceList);
    }

    /**
     * Finds the provided number of instances to run commands on.
     *
     * @param numHosts number of instances to execute on
     * @return list of instances to execute on
     */
    private List<EC2InstanceTO> getInstancesForExecution(int numHosts) {

        List<EC2InstanceTO> instances = executionStateCache.getInstances();

        if (instances == null || instances.isEmpty()) {
            throw new RuntimeException("No Instances are available");
        }

        if (numHosts > instances.size()) {
            numHosts = instances.size();
        }

        List<EC2InstanceTO> instanceList = instances;
        if (instanceList.size() != numHosts) {
            instanceList = instances.subList(0, numHosts);
        }

        return instanceList;
    }
}
