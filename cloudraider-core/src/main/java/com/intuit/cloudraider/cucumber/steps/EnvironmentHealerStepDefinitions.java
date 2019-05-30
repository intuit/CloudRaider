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
import com.intuit.cloudraider.cucumber.model.ExecutionStateCache;
import com.intuit.cloudraider.cucumber.util.ScriptExecutor;
import com.intuit.cloudraider.model.Actions;
import com.intuit.cloudraider.model.Command;
import com.intuit.cloudraider.model.EC2InstanceTO;
import com.intuit.cloudraider.utils.CommandUtility;
import com.intuit.cloudraider.utils.Ec2Utils;
import cucumber.api.java.en.Then;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.ArrayList;
import java.util.List;

/**
 * Cucumber Step Definitions for restoring AWS infrastructure to previous state.
 */
public class EnvironmentHealerStepDefinitions {

    @Autowired
    @Qualifier("scriptExecutor")
    private ScriptExecutor scriptExecutor;

    /**
     * The Logger.
     */
    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    @Qualifier("ssmRaiderBean")
    private SSMRaider ssmRaider;

    @Autowired
    private ExecutionStateCache executionStateCache;

    public ExecutionStateCache getExecutionStateCache() {
        return executionStateCache;
    }

    public void setExecutionStateCache(ExecutionStateCache executionStateCache) {
        this.executionStateCache = executionStateCache;
    }

    public EnvironmentHealerStepDefinitions() {

    }

    /**
     * Recovers from instance-specific failures (other than termination and hard disk manipulation) during testing.
     */
    @Then("^recover$")
    public void recover() {
        boolean isSSM = !executionStateCache.getSsmCommandInvocationInstances().isEmpty();

        if (executionStateCache.isHealProcess()) {
            List<String> processes = executionStateCache.getProcessNames();
            for (String process : processes) {
                if (isSSM) {

                    healGivenSSMInstancesByProcess(process, executionStateCache.getSsmCommandInvocationInstances());
                } else {
                    healGivenInstancesByProcess(process, executionStateCache.getUnhealthyInstances());
                }
            }
        }

        if (executionStateCache.isHealNetwork()) {

            if (isSSM) {
                healSSMGivenInstancesFixingNetworkIssues(executionStateCache.getSsmCommandInvocationInstances());
            }
            else {
                healGivenInstancesFixingNetworkIssues(executionStateCache.getUnhealthyInstances());
            }
        }

        if (!executionStateCache.getBlockedDomains().isEmpty()) {
            if (isSSM) {

                executionStateCache.getBlockedDomains().parallelStream()
                        .forEach(domain -> healSSMGivenInstancesByDomainName(domain, executionStateCache.getSsmCommandInvocationInstances()));
            } else {
                executionStateCache.getBlockedDomains().parallelStream()
                        .forEach(domain -> healGivenInstancesByDomainName(domain, executionStateCache.getUnhealthyInstances()));
            }
        }

        if (executionStateCache.isCpuSpiked()) {
            if (isSSM) {

                healSSMGivenInstancesByFixingCPU(executionStateCache.getSsmCommandInvocationInstances());
            } else {
                healGivenInstancesByFixingCPU(executionStateCache.getUnhealthyInstances());
            }
        }

        if (executionStateCache.isBlockPort()) {
            List<Integer> portNums = executionStateCache.getPortNums();
            for (Integer port : portNums) {
                if (isSSM) {

                    healSSMGivenInstancesByFixingPort(port, executionStateCache.getSsmCommandInvocationInstances());
                } else {
                    healGivenInstancesByFixingPort(executionStateCache.getUnhealthyInstances());
                }
            }
        }

        if (executionStateCache.isRamDiskFull()) {
            if (isSSM) {
                healSSMGivenInstancesByFixingRamDisk(executionStateCache.getSsmCommandInvocationInstances());
            } else {
                healGivenInstancesByFixingRamDisk(executionStateCache.getUnhealthyInstances());
            }
        }


        if (executionStateCache.isBlockDynamoDB())
        {
            if (isSSM) {

                healSSMGivenInstancesByUnblockingDynamoDB(executionStateCache.getSsmCommandInvocationInstances());
            }
            else
            {
                healGivenInstancesByUnblockingDynamoDB(executionStateCache.getUnhealthyInstances());

            }
        }

        if (executionStateCache.isBlockS3())
        {
            if (isSSM) {

                healSSMGivenInstancesByUnblockingS3(executionStateCache.getSsmCommandInvocationInstances());
            }
            else
            {
                healGivenInstancesByUnblockingS3(executionStateCache.getUnhealthyInstances());

            }
        }


    }

    @Then("^clearCache$")
    public void clearCache() {

        executionStateCache.clear();
    }

        /**
         * Restarts the process on the given instances.
         *
         * @param processName process name
         * @param instances instances to heal
         */
    public EnvironmentHealerStepDefinitions healGivenInstancesByProcess(String processName, List<EC2InstanceTO> instances)
    {

        instances.parallelStream()
                .forEach(i ->
                {
                    String ip = i.getPrivateIpAddress();
                    scriptExecutor.executeScript("healInstance", ip, processName);
                });

        return this;
    }

    /**
     * Unblock the domain name for the given instances.
     *
     * @param domainName domain name
     * @param instances instances to heal
     */
    public EnvironmentHealerStepDefinitions healGivenInstancesByDomainName(String domainName, List<EC2InstanceTO> instances) {

        instances.parallelStream()
                .forEach(i ->
                {
                    String ip = i.getPrivateIpAddress();
                    scriptExecutor.executeScript(Actions.UNBLOCKDOMAIN, ip, domainName);
                });

        return this;
    }

    /**
     * Fix RAM Disk Full for the given instances.
     *
     * @param instanceList instances to heal
     */
    public EnvironmentHealerStepDefinitions healGivenInstancesByFixingRamDisk(List<EC2InstanceTO> instanceList) {

        instanceList.parallelStream()
                .forEach(i ->
                {
                    String ip = i.getPrivateIpAddress();
                    scriptExecutor.executeScript("clear-ramdiskfull.sh", ip);
                });

        return this;
    }

    /**
     * Clear network failures/latency for the given instances.
     *
     * @param instances instances to heal
     */
    public EnvironmentHealerStepDefinitions healGivenInstancesByFixingNetworkIssues(List<EC2InstanceTO> instances) {

        instances.parallelStream()
                .forEach(i ->
                {
                    String ip = i.getPrivateIpAddress();
                    scriptExecutor.executeScript("clear-networkfailures", ip);
                });

        return this;
    }

    /**
     * Stops CPU spike on the given instances.
     *
     * @param instances instances to heal
     */
    public EnvironmentHealerStepDefinitions healGivenInstancesByFixingCPU(List<EC2InstanceTO> instances) {

        instances.parallelStream()
                .forEach(i ->
                {
                    String ip = i.getPrivateIpAddress();
                    scriptExecutor.executeScript("killcpuspike", ip);
                });

        return this;
    }

    /**
     * Unblocks all ports for the given instances.
     *
     * @param instances instances to heal
     */
    public EnvironmentHealerStepDefinitions healGivenInstancesByFixingPort(List<EC2InstanceTO> instances) {

        instances.parallelStream()
                .forEach(i ->
                {
                    String ip = i.getPrivateIpAddress();
                    scriptExecutor.executeScript(Actions.UNBLOCKPORT, ip);
                });

        return this;
    }

    /**
     * [SSM] Stops CPU spike on the given instances.
     *
     * @param instanceList instances to heal
     */
    public EnvironmentHealerStepDefinitions healSSMGivenInstancesByFixingCPU(List<EC2InstanceTO> instanceList) {

        List<String> commands = CommandUtility.getCommandsFromFile("killcpuspikeCommand.txt");
        String commandId = ssmRaider.executeShellCommands(Ec2Utils.generateInstanceIdList(instanceList), commands);
        return this;
    }

    /**
     * [SSM] Clear network failures/latency for the given instances.
     *
     * @param instanceList instances to heal
     */
    public EnvironmentHealerStepDefinitions healSSMGivenInstancesFixingNetworkIssues(List<EC2InstanceTO> instanceList )
    {

        List<String> commands = CommandUtility.getCommandsFromFile("clear-networkfailuresCommand.txt");
        String commandId = ssmRaider.executeShellCommands(Ec2Utils.generateInstanceIdList(instanceList),commands);
        return this;
    }

    /**
     * Clear network failures/latency for the given instances.
     *
     * @param instances instances to heal
     */
    public EnvironmentHealerStepDefinitions healGivenInstancesFixingNetworkIssues(List<EC2InstanceTO> instances )
    {

        instances.parallelStream()
                .forEach( i ->
                {
                    String ip = i.getPrivateIpAddress();
                    scriptExecutor.executeScript("clear-networkfailures", ip);
                });

        return this;
    }


    public EnvironmentHealerStepDefinitions healSSMGivenInstancesByUnblockingDynamoDB(List<EC2InstanceTO> instanceList )
    {

        List<String> commands = CommandUtility.getCommandsFromFile(Command.UNBLOCKDYNAMODB.getCommandName()+".txt");
        String commandId = ssmRaider.executeShellCommands(Ec2Utils.generateInstanceIdList(instanceList),commands);
        executionStateCache.setBlockDynamoDB(false);
        return this;
    }

    public EnvironmentHealerStepDefinitions healSSMGivenInstancesByUnblockingS3(List<EC2InstanceTO> instanceList )
    {

        List<String> commands = CommandUtility.getCommandsFromFile(Command.UNBLOCKS3.getCommandName()+".txt");
        String commandId = ssmRaider.executeShellCommands(Ec2Utils.generateInstanceIdList(instanceList),commands);
        executionStateCache.setBlockS3(false);
        return this;
    }

    /**
     * [SSM] Unblocks the given port on the provided instances.
     *
     * @param portNum port number
     * @param instanceList instances to heal
     */
    public EnvironmentHealerStepDefinitions healSSMGivenInstancesByFixingPort(int portNum, List<EC2InstanceTO> instanceList) {

        List<String> commands = CommandUtility.getCommandsFromFile(Command.UNBLOCKPORT.getCommandName() + ".txt", String.valueOf(portNum));
        String commandId = ssmRaider.executeShellCommands(Ec2Utils.generateInstanceIdList(instanceList), commands);
        return this;
    }

    /**
     * [SSM] Restarts the process on the given instances.
     *
     * @param processName process to restart
     * @param instanceList instances to heal
     */
    public EnvironmentHealerStepDefinitions healGivenSSMInstancesByProcess(String processName, List<EC2InstanceTO> instanceList) {
        List<String> commands = CommandUtility.getCommandsFromFile("healCommand.txt", processName);

        String commandId = ssmRaider.executeShellCommands(Ec2Utils.generateInstanceIdList(instanceList), commands);
        return this;

    }

    /**
     * [SSM] Unblock the domain name for the given instances.
     *
     * @param domainName domain name
     * @param instanceList instances to heal
     */
    public EnvironmentHealerStepDefinitions healSSMGivenInstancesByDomainName(String domainName, List<EC2InstanceTO> instanceList) {

        List<String> commands = CommandUtility.getCommandsFromFile(Command.UNBLOCKDOMAIN.getCommandName() + ".txt", domainName);
        String commandId = ssmRaider.executeShellCommands(Ec2Utils.generateInstanceIdList(instanceList), commands);
        return this;
    }

    /**
     * [SSM] Fix RAM Disk Full for the given instances.
     *
     * @param instanceList instances to heal
     */
    public EnvironmentHealerStepDefinitions healSSMGivenInstancesByFixingRamDisk(List<EC2InstanceTO> instanceList) {

        List<String> commands = new ArrayList<>();
        commands.add("sudo rm /dev/shm/remove_me");
        String commandId = ssmRaider.executeShellCommands(Ec2Utils.generateInstanceIdList(instanceList), commands);
        return this;
    }

        /**
         * [SSM] Fix DynamoDB failure
         *
         * @param instances instances to heal
         */

    public EnvironmentHealerStepDefinitions healGivenInstancesByUnblockingDynamoDB(List<EC2InstanceTO> instances )
    {

        instances.parallelStream()
                .forEach( i ->
                {
                    String ip = i.getPrivateIpAddress();
                    scriptExecutor.executeScript(Actions.UNBLOCKDYNAMO, ip);
                });


        executionStateCache.setBlockDynamoDB(false);

        return this;
    }

        /**
         * [SSM] Fix S3 failure
         *
         * @param instances instances to heal
         */

    public EnvironmentHealerStepDefinitions healGivenInstancesByUnblockingS3(List<EC2InstanceTO> instances )
    {

        instances.parallelStream()
                .forEach( i ->
                {
                    String ip = i.getPrivateIpAddress();
                    scriptExecutor.executeScript(Actions.UNBLOCKS3, ip);
                });

        executionStateCache.setBlockS3(false);

        return this;
    }



}
