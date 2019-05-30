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


import com.amazonaws.services.ec2.model.Tag;
import com.intuit.cloudraider.core.interfaces.EC2Raider;
import com.intuit.cloudraider.cucumber.interfaces.EC2StepFunctions;
import com.intuit.cloudraider.cucumber.model.ExecutionStateCache;
import com.intuit.cloudraider.cucumber.util.CucumberHelperFunctions;
import com.intuit.cloudraider.cucumber.util.ScriptExecutor;
import com.intuit.cloudraider.model.EC2InstanceTO;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Cucumber Step Definitions for AWS EC2 functionality.
 */
public class InstanceFailureStepDefinitions implements EC2StepFunctions {

    @Autowired
    @Qualifier("scriptExecutor")
    private ScriptExecutor scriptExecutor;

    @Autowired
    @Qualifier("ec2raiderBean")
    private EC2Raider ec2Raider;

    /**
     * The Logger.
     */
    Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final String MULTIPLE_DELIMITER = ",";

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

    @Autowired
    private ExecutionStateCache executionStateCache;

    /**
     * Instantiates a new Instance failure step definitions.
     */
    public InstanceFailureStepDefinitions() {
    }

    /**
     * Finds EC2 instances with the given name and adds to execution cache.
     *
     * @param name name of instance
     * @return the instance failure step definitions
     */
    @Given("^EC2 \"([^\"]*)\"$")
    public InstanceFailureStepDefinitions givenEC2InstanceName(String name) {
        executionStateCache.addInstances(findInstancesByName(name));
        executionStateCache.setEc2Tag(name);
        return this;
    }

    /**
     * Finds EC2 instances with the given tag and adds to execution cache.
     *
     * @param tag tag in the form "key:value"
     * @return the instance failure step definitions
     */
    @Given("^EC2 with a tag \"([^\"]*)\"$")
    public InstanceFailureStepDefinitions givenEC2InstanceTags(String tag) {
        executionStateCache.addInstances(findAllInstancesByTag(tag));
        executionStateCache.setEc2Tag(tag);
        return this;
    }

    /**
     * Finds EC2 instances with names that do not include the provided keywords and adds to execution cache.
     *
     * @param filters keywords to ignore in the name, separated by "," (comma)
     * @return the instance failure step definitions
     */
    @Given("^EC2 instances filtered by \"([^\"]*)\"$")
    public InstanceFailureStepDefinitions givenFilteredEC2(String filters) {
        List<String> filtersList = Arrays.asList(filters.split("\\s*" + MULTIPLE_DELIMITER + "\\s*"));
        executionStateCache.setInstances(ec2Raider.getFilteredActiveInstances(filtersList));
        return this;
    }

    /**
     * FindsEC2 instances with any of the given tags and adds to execution cache.
     *
     * @param tags tags in the form "key:value","key:value"
     * @return the instance failure step definitions
     */
    @Given("^EC2 with a tags \"([^\"]*)\"$")
    public InstanceFailureStepDefinitions givenEC2InstanceTags(String... tags) {
        List<String> tagsList = Arrays.asList(tags);
        tagsList.parallelStream().forEach(
                t -> executionStateCache.addInstances(findAllInstancesByTag(t))
        );
        return this;
    }

    /**
     * Terminate the given process on the instance.
     *
     * @param processName process name
     * @param instanceID instance id
     * @param instanceIP instance private ip
     */
    public void terminateProcessGivenInstance(String processName, String instanceID, String instanceIP) {
        terminateProcessGivenInstanceCucumber(processName, instanceID, instanceIP);
    }

    /**
     * Terminate the given process on all available instances.
     *
     * @param processName process name
     * @return the instance failure step definitions
     * @throws Throwable the throwable
     */
    @When("^terminate process \"([^\"]*)\"$")
    public InstanceFailureStepDefinitions terminateProcessOnAllHealthyInstances(String processName) throws Throwable {
        List<EC2InstanceTO> instances = executionStateCache.getInstances();
        if (instances == null || instances.isEmpty()) {
            throw new RuntimeException("No Instances are available");
        }
        executionStateCache.addProcessName(processName);

        terminateProcess(processName, instances.size(), instances);

        return this;
    }

    /**
     * Terminate the given process on the instance.
     *
     * @param processName process name
     * @param instanceID  instance id
     * @param instanceIP  instance private ip
     * @return the instance failure step definitions
     */
    @When("^terminate process \"([^\"]*)\" on instance with id \"([^\"]*)\" and ip \"([^\"]*)\"$")
    public InstanceFailureStepDefinitions terminateProcessGivenInstanceCucumber(String processName, String instanceID, String instanceIP) {
        List<EC2InstanceTO> instances = new ArrayList<>();
        instances.add(executionStateCache.findEC2InstanceGivenID(instanceID));

        executionStateCache.addProcessName(processName);

        terminateProcess(processName, 1, instances);

        return this;
    }

    /**
     * Terminate the given process on the provided number of instances.
     *
     * @param processName process name
     * @param numHosts    number of instances
     * @return the instance failure step definitions
     */
    @When("^terminate process  \"([^\"]*)\" on (\\d+) instance$")
    public InstanceFailureStepDefinitions terminateProcessOnHealthyInstances(String processName, int numHosts) {
        List<EC2InstanceTO> instances = executionStateCache.getInstances();

        if (instances == null || instances.isEmpty()) {
            throw new RuntimeException("No Instances are available");
        }

        executionStateCache.addProcessName(processName);

        if (numHosts > instances.size()) {
            numHosts = instances.size();
        }

        terminateProcess(processName, numHosts, instances);

        return this;
    }

    /**
     * Terminate the given process on the provided number of instances for the specified availability zone.
     *
     * @param processName process name
     * @param numHosts    number of instances
     * @param zoneId      availability zone id
     * @return the instance failure step definitions
     */
    @When("^terminate process  \"([^\"]*)\" on (\\d+) instance in zone \"([^\"]*)\"$")
    public InstanceFailureStepDefinitions terminateProcessInAvailabilityZone(String processName, int numHosts, String zoneId) {
        List<EC2InstanceTO> instancesTO = ec2Raider.getEc2InstancesForAvailabilityZone(zoneId, new ArrayList<>());

        if (instancesTO == null || instancesTO.isEmpty()) {
            throw new RuntimeException("No Instances are available");
        }

        executionStateCache.addProcessName(processName);

        if (numHosts > instancesTO.size()) {
            numHosts = instancesTO.size();
        }

        terminateProcess(processName, numHosts, instancesTO);

        return this;
    }

    /**
     * Spike the given number of cores on the instance.
     *
     * @param cores number of cores
     * @param instanceID instance id
     * @param instanceIP instance private ip
     */
    public void spikeCPUGivenInstance(int cores, String instanceID, String instanceIP) {
        spikeCPUGivenInstanceCucumber(cores, instanceID, instanceIP);
    }

    /**
     * Spike the given number of cores on the instance.
     *
     * @param cores      number of cores
     * @param instanceID instance id
     * @param instanceIP instance private ip
     * @return the instance failure step definitions
     */
    @When("^CPU spike for (\\d+) cores on instance with id \"([^\"]*)\" and ip \"([^\"]*)\"$")
    public InstanceFailureStepDefinitions spikeCPUGivenInstanceCucumber(int cores, String instanceID, String instanceIP) {
        spikeCPUHelper(cores, instanceID, instanceIP);
        executionStateCache.setCpuSpiked(true);
        executionStateCache.addUnHealthyInstance(executionStateCache.findEC2InstanceGivenID(instanceID));
        return this;
    }

    /**
     * Helper function to spike the cores on the instance.
     *
     * @param cores number of cores
     * @param instanceID instance id
     * @param instanceIP instance private ip
     */
    private void spikeCPUHelper(int cores, String instanceID, String instanceIP) {
        logger.info("starting cpu spike on: " + instanceID + " @ " + instanceIP);
        scriptExecutor.executeCPUSpike(instanceIP, cores);
    }

    /**
     * Spike the given number of cores on the provided number of instances.
     *
     * @param numHosts number of instances
     * @param cores    number of cores
     * @return the instance failure step definitions
     */
    @When("^CPU spike on (\\d+) instances for (\\d+) cores$")
    public InstanceFailureStepDefinitions spikeCPUOnHealthyInstances(int numHosts, int cores) {
        List<EC2InstanceTO> instances = executionStateCache.getInstances();
        if (instances == null || instances.isEmpty()) {
            throw new RuntimeException("No Instances are available");
        }

        if (numHosts > instances.size()) {
            numHosts = instances.size();
        }

        IntStream.range(0, numHosts)
                .parallel()
                .forEach(
                        i ->
                        {
                            spikeCPUHelper(cores, instances.get(i).getInstanceId(), instances.get(i).getPrivateIpAddress());
                            executionStateCache.addUnHealthyInstance(instances.get(i));
                            executionStateCache.setCpuSpiked(true);
                        });

        return this;
    }

    /**
     * Terminate all available EC2 instances.
     *
     * @return the instance failure step definitions
     */
    @When("^terminate all instances$")
    public InstanceFailureStepDefinitions terminateAllInstances() {
        List<EC2InstanceTO> instances = executionStateCache.getInstances();
        if (instances == null || instances.isEmpty()) {
            throw new RuntimeException("No Instances are available");
        }

        instances.parallelStream()
                .forEach(i ->  {
                    terminationHelper(i.getInstanceId(), i.getPrivateIpAddress());
                });

        return this;
    }

    /**
     * Terminates the provided instance.
     *
     * @param instanceID instance id
     * @param instanceIP instance private ip
     */
    public void terminateGivenInstance(String instanceID, String instanceIP) {
        terminateGivenInstanceCucumber(instanceID, instanceIP);
    }

    /**
     * Helper function for terminating provided instance.
     *
     * @param instanceID instance id
     * @param instanceIP instance private ip
     */
    private void terminationHelper(String instanceID, String instanceIP) {
        logger.info("Terminating instance: " + instanceID + " @ " + instanceIP);
        ec2Raider.terminateEc2InstancesById(instanceID);
    }

    /**
     * Terminates the provided instance.
     *
     * @param instanceID instance id
     * @param instanceIP instance private ip
     * @return the instance failure step definitions
     */
    @When("^terminate instance with id \"([^\"]*)\" and ip \"([^\"]*)\"$")
    public InstanceFailureStepDefinitions terminateGivenInstanceCucumber(String instanceID, String instanceIP) {
        terminationHelper(instanceID, instanceIP);
        return this;
    }

    /**
     * Terminates the given number of instances.
     *
     * @param numInstances number of instances
     * @return the instance failure step definitions
     */
    @When("^terminate (\\d+) instance$")
    public InstanceFailureStepDefinitions terminateInstanceOnNumInstances(int numInstances) {
        List<EC2InstanceTO> instances = executionStateCache.getInstances();
        if (instances == null || instances.isEmpty()) {
            throw new RuntimeException("Unable to terminate process, no instances available");
        }

        if (numInstances > instances.size()) {
            numInstances = instances.size();
        }

        IntStream.range(0, numInstances)
                .parallel()
                .forEach(
                        i ->
                        {
                            terminationHelper(instances.get(i).getInstanceId(), instances.get(i).getPrivateIpAddress());
                        });

        return this;
    }

    /**
     * Terminates the given number of instances with matching tags. The instance must have all tags in order to be considered.
     *
     * @param numInstances         number of instances
     * @param compulsoryTagsString tags in the form of "key:value,key:value"
     * @return the instance failure step definitions
     */
    @When("^terminate (\\d+) instance with tags \"([^\"]*)\"$")
    public InstanceFailureStepDefinitions terminateInstanceOnNumInstancesWithTags(int numInstances, String compulsoryTagsString) {
        List<Tag> compulsoryTags = CucumberHelperFunctions.tagStringToList(compulsoryTagsString);

        List<EC2InstanceTO> instances = executionStateCache.getInstances();

        if (instances == null || instances.isEmpty()) {
            throw new RuntimeException("Unable to terminate process, no instances available in AZ");
        }

        if (numInstances > instances.size()) {
            numInstances = instances.size();
        }

        int terminatedInstances = 0;
        for (EC2InstanceTO instance : instances) {
            boolean flag = CucumberHelperFunctions.containsAllCompulsoryTags(compulsoryTags, instance.getTags());

            if (flag) {
                ec2Raider.terminateEc2InstancesById(instance.getInstanceId());
                terminatedInstances++;
            }

            if (terminatedInstances == numInstances) {
                break;
            }

        }
        return this;
    }

    /**
     * Stops the given number of available instances.
     *
     * @param numInstances number of instances
     * @return the instance failure step definitions
     */
    @When("^stop (\\d+) instance$")
    public InstanceFailureStepDefinitions stopInstanceOnNumInstances(int numInstances ) {
        List<EC2InstanceTO> stoppedInstances = executionStateCache.getStoppedInstances();

        List<EC2InstanceTO> instances = executionStateCache.getInstances();
        if (instances == null  || instances.isEmpty()) {
            throw new RuntimeException("Unable to stop, no instances available");
        }

        if (numInstances > instances.size()) {
            numInstances = instances.size();
        }

        IntStream.range(0, numInstances)
                .parallel()
                .forEach(
                        i ->
                        {
                            ec2Raider.stopEc2Instances(instances.get(i).getInstanceId());
                            stoppedInstances.add(instances.get(i));
                        } );

        executionStateCache.setStoppedInstances(stoppedInstances);

        return this;
    }

    /**
     * Restarts the given number of stopped instances.
     *
     * @param numInstances number of instances
     * @return the instance failure step definitions
     */
    @Then("^start (\\d+) instance$")
    public InstanceFailureStepDefinitions startInstanceOnNumInstances(int numInstances ) {
        List<EC2InstanceTO> stoppedInstances = executionStateCache.getStoppedInstances();
        List<EC2InstanceTO> restartedInstances = new ArrayList<>();

        if (stoppedInstances == null  || stoppedInstances.isEmpty()) {
            logger.debug("EC2RaiderStepDefinitions - All instances are active");
            return this;
        }

        if (numInstances > stoppedInstances.size()) {
            numInstances = stoppedInstances.size();
        }

        IntStream.range(0, numInstances)
                .parallel()
                .forEach(
                        i ->
                        {
                            restartedInstances.add(stoppedInstances.get(i));
                            ec2Raider.restartEc2Instances(stoppedInstances.get(i).getInstanceId());
                        } );

        stoppedInstances.removeAll(restartedInstances);
        executionStateCache.setStoppedInstances(stoppedInstances);

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
    public void diskFullGivenInstance(String volumeType, int size, String instanceID, String instanceIP) {
        diskFullGivenInstanceCucumber(volumeType, size, instanceID, instanceIP);
    }

    /**
     * Add "size" GB to the specified volume.
     *
     * @param volumeType name of volume
     * @param size       size of disk
     * @param instanceID instance id
     * @param instanceIP instance private ip
     * @return the instance failure step definitions
     */
    @When("^\"([^\"]*)\" disk full with (\\d+) GB on instance with id \"([^\"]*)\" and ip \"([^\"]*)\"$")
    public InstanceFailureStepDefinitions diskFullGivenInstanceCucumber(String volumeType, int size, String instanceID, String instanceIP) {
        diskFullHelper(volumeType, size, instanceID, instanceIP);
        executionStateCache.setHealProcess(true);
        executionStateCache.addUnHealthyInstance(executionStateCache.findEC2InstanceGivenID(instanceID));
        return this;
    }

    /**
     * Helper function to add data to the disk.
     *
     * @param volumeType name of volume
     * @param size size of disk
     * @param instanceID instance id
     * @param instanceIP instance private ip
     */
    private void diskFullHelper(String volumeType, int size, String instanceID, String instanceIP) {
        logger.info("starting disk full on: " + instanceID + " @ " + instanceIP);
        scriptExecutor.executeDiskFull(instanceIP, volumeType, size);
    }

    /**
     * Add "size" GB to the specified volume on the given number of instances.
     *
     * @param volumeType    name of volume
     * @param size          size of disk
     * @param instanceCount number of instances
     * @return the instance failure step definitions
     * @throws Throwable the throwable
     */
    @When("^\"([^\"]*)\" disk full with (\\d+) GB on (\\d+) instance$")
    public InstanceFailureStepDefinitions diskFullOnInstance(String volumeType, int size, int instanceCount) throws Throwable {
        List<EC2InstanceTO> instances = executionStateCache.getInstances();
        if (instances == null || instances.isEmpty()) {
            throw new RuntimeException("No Instances are available");
        }

        if (instanceCount > instances.size()) {
            instanceCount = instances.size();
        }


        IntStream.range(0, instanceCount)
                .parallel()
                .forEach(
                        i ->
                        {

                            diskFullHelper(volumeType, size, instances.get(i).getInstanceId(), instances.get(i).getPrivateIpAddress());
                            executionStateCache.setHealProcess(true);
                            executionStateCache.addUnHealthyInstance(instances.get(i));
                        });

        return this;
    }

    /**
     * Block the domain from access on the given number of instances.
     *
     * @param domainName   domain name
     * @param numInstances number of instances
     * @return the instance failure step definitions
     */
    @When("^block domain \"([^\"]*)\" on (\\d+) instances$")
    public InstanceFailureStepDefinitions blockDomain(String domainName, int numInstances) {
        List<EC2InstanceTO> instances = executionStateCache.getInstances();
        if (instances == null || instances.isEmpty()) {
            throw new RuntimeException("No Instances are available");
        }

        if (numInstances > instances.size()) {
            numInstances = instances.size();
        }

        IntStream.range(0, numInstances )
                .parallel()
                .forEach(
                        i ->
                        {
                            EC2InstanceTO ec2Instance = ec2Raider.getEC2InstanceById(instances.get(i).getInstanceId());
                            String ip = ec2Instance.getPrivateIpAddress();
                            String id = ec2Instance.getInstanceId();
                            logger.info("blocking domain " + domainName + " on: " + id + " @ " + ip);
                            scriptExecutor.executeBlockDomain(ip, domainName);
                            executionStateCache.getBlockedDomains().add(domainName);
                            executionStateCache.addUnHealthyInstance(ec2Instance);
                        });

        return this;
    }

    /**
     * Block traffic on the given port number.
     *
     * @param portNum port number to block
     * @param instanceID instance id
     * @param instanceIP instance private ip
     */
    public void blockPortGivenInstance(int portNum, String instanceID, String instanceIP) {
        blockPortGivenInstanceCucumber(portNum, instanceID, instanceIP);
    }

    /**
     * Block traffic on the given port number.
     *
     * @param portNum    port number to block
     * @param instanceID instance id
     * @param instanceIP instance private ip
     * @return the instance failure step definitions
     */
    @When("^block port (\\d+) on instance with id \"([^\"]*)\" and ip \"([^\"]*)\"$")
    public InstanceFailureStepDefinitions blockPortGivenInstanceCucumber(int portNum, String instanceID, String instanceIP) {
        blockPortHelper(portNum, instanceID, instanceIP);
        executionStateCache.setBlockPort(true);
        executionStateCache.addPortNum(portNum);
        executionStateCache.addUnHealthyInstance(executionStateCache.findEC2InstanceGivenID(instanceID));
        return this;
    }

    /**
     * Helper function to block traffic port.
     *
     * @param portNum port number to block
     * @param instanceID instance id
     * @param instanceIP instance private ip
     */
    private void blockPortHelper(int portNum, String instanceID, String instanceIP) {
        logger.info("blocking port " + portNum + " on: " + instanceID + " @ " + instanceIP);
        scriptExecutor.executeBlockDomain(instanceIP, String.valueOf(portNum));
    }

    /**
     * Block traffic on the given port number on each of the provided number of instances.
     *
     * @param portNum      port number to block
     * @param numInstances number of instances
     * @return the instance failure step definitions
     */
    @When("^block port (\\d+) on (\\d+) instances$")
    public InstanceFailureStepDefinitions blockPort(int portNum, int numInstances) {
        List<EC2InstanceTO> instances = executionStateCache.getInstances();
        if (instances == null || instances.isEmpty()) {
            throw new RuntimeException("No Instances are available");
        }

        if (numInstances > instances.size()) {
            numInstances = instances.size();
        }

        IntStream.range(0, numInstances)
                .parallel()
                .forEach(
                        i ->
                        {
                            blockPortHelper(portNum, instances.get(i).getInstanceId(), instances.get(i).getPrivateIpAddress());
                            executionStateCache.setBlockPort(true);
                            executionStateCache.addPortNum(portNum);
                            executionStateCache.addUnHealthyInstance(instances.get(i));
                        });

        return this;
    }

    /**
     * Block dynamo db instance failure step definitions.
     *
     * @param numInstances the num instances
     * @return the instance failure step definitions
     */
    @When("^block DynamoDB on (\\d+) instances$")
    public InstanceFailureStepDefinitions blockDynamoDb(int numInstances) {
        List<EC2InstanceTO> instances = executionStateCache.getInstances();
        if (instances == null || instances.isEmpty()) {
            throw new RuntimeException("No Instances are vailable");
        }

        if (numInstances > instances.size()) {
            numInstances = instances.size();
        }

        IntStream.range(0, numInstances)
                .parallel()
                .forEach(
                        i ->
                        {
                            EC2InstanceTO ec2SInstance = ec2Raider.getEC2InstanceById(instances.get(i).getInstanceId());
                            String ip = ec2SInstance.getPrivateIpAddress();
                            scriptExecutor.executeBlockDynamoDB(ip);
                            executionStateCache.setBlockDynamoDB(true);
                            executionStateCache.addUnHealthyInstance(ec2SInstance);
                        });

        return this;
    }


    /**
     * Block s 3 instance failure step definitions.
     *
     * @param numInstances the num instances
     * @return the instance failure step definitions
     */
    @When("^block S3 on (\\d+) instances$")
    public InstanceFailureStepDefinitions blockS3(int numInstances) {
        List<EC2InstanceTO> instances = executionStateCache.getInstances();
        if (instances == null || instances.isEmpty()) {
            throw new RuntimeException("No Instances are vailable");
        }

        if (numInstances > instances.size()) {
            numInstances = instances.size();
        }

        IntStream.range(0, numInstances)
                .parallel()
                .forEach(
                        i ->
                        {
                            EC2InstanceTO ec2SInstance = ec2Raider.getEC2InstanceById(instances.get(i).getInstanceId());
                            String ip = ec2SInstance.getPrivateIpAddress();
                            scriptExecutor.executeBlockS3(ip);
                            executionStateCache.setBlockS3(true);
                            executionStateCache.addUnHealthyInstance(ec2SInstance);
                        });

        return this;
    }

    /**
     * Stop the given process on the specified number of instances.
     *
     * @param serviceOrProcessType service or process (UNUSED)
     * @param processName          process name
     * @param numHosts             number of instances
     * @return the instance failure step definitions
     */
    @When("^stop (service|process) \"([^\"]*)\" on (\\d+) instance$")
    public InstanceFailureStepDefinitions stopProcessOnHealthyInstances(String serviceOrProcessType, String processName, int numHosts) {
        List<EC2InstanceTO> instances = executionStateCache.getInstances();

        List<String> processList = new ArrayList<String>(Arrays.asList(processName.split(",")));


        if (instances == null || instances.isEmpty())
        {
            throw new RuntimeException("No Instances are vailable");
        }

        executionStateCache.addProcessName(processName);

        if (numHosts > instances.size()) {
            numHosts = instances.size();
        }


        for(String process : processList)
        {
        	logger.info("stopping process: "+ process + "on " + numHosts + " instances ");
        	stopProcess(process.trim(),numHosts,instances);

        }


        return this;
    }

    /**
     * Start the given process on the specified number of instances.
     *
     * @param serviceOrProcessType service or process (UNUSED)
     * @param processName          process name
     * @param numHosts             number of instances
     * @return the instance failure step definitions
     */
    @When("^start (service|process) \"([^\"]*)\" on (\\d+) instance$")
    public InstanceFailureStepDefinitions startProcessOnHealthyInstances(String serviceOrProcessType, String processName, int numHosts) {
        List<EC2InstanceTO> instances = executionStateCache.getInstances();

        if (instances == null || instances.isEmpty()) {
            throw new RuntimeException("No Instances are available");
        }

        executionStateCache.addProcessName(processName);

        if (numHosts > instances.size()) {
            numHosts = instances.size();
        }

        IntStream.range(0, numHosts)
                .parallel()
                .forEach(
                        i ->
                        {
                            String ip = instances.get(i).getPrivateIpAddress();
                            String id = instances.get(i).getInstanceId();
                            logger.info("starting process on: " + id + " @ " + ip);
                            scriptExecutor.executeStartProcess(ip, processName);
                        });
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
    public void injectNetworkLatencyGivenInstance(int lowerBound, int upperBound, String instanceID, String instanceIP) {
        injectNetworkLatencyGivenInstanceCucumber(lowerBound, upperBound, instanceID, instanceIP);
    }

    /**
     * Injects network latency within the bounds on the default interface.
     *
     * @param lowerBound lower bound for latency
     * @param upperBound upper bound for latency
     * @param instanceID instance id
     * @param instanceIP instance private ip
     * @return the instance failure step definitions
     */
    @When("^inject network latency (\\d+) ms to (\\d+) ms on instance with id \"([^\"]*)\" and ip \"([^\"]*)\"$")
    public InstanceFailureStepDefinitions injectNetworkLatencyGivenInstanceCucumber(int lowerBound, int upperBound, String instanceID, String instanceIP) {
        injectNetworkLatencyHelper(lowerBound, upperBound, instanceID, instanceIP);
        executionStateCache.setHealNetwork(true);
        executionStateCache.addUnHealthyInstance(executionStateCache.findEC2InstanceGivenID(instanceID));
        return this;
    }

    /**
     * Helper function for injecting network latency.
     *
     * @param lowerBound lower bound for latency
     * @param upperBound upper bound for latency
     * @param instanceID instance id
     * @param instanceIP instance private ip
     */
    private void injectNetworkLatencyHelper(int lowerBound, int upperBound, String instanceID, String instanceIP) {
        logger.info("injecting latency on: " + instanceID + " @ " + instanceIP);
        scriptExecutor.executeRandomNetworkLatency(instanceIP, String.valueOf(upperBound), String.valueOf(lowerBound));
    }

    /**
     * Injects network latency within the bounds on the default interface on each of the provided number of instances.
     *
     * @param lowerBound   lower bound for latency
     * @param upperBound   upper bound for latency
     * @param numInstances number of instances
     * @return the instance failure step definitions
     */
    @When("^inject network latency (\\d+) ms to (\\d+) ms on (\\d+) instances$")
    public InstanceFailureStepDefinitions injectNetworkLatency(int lowerBound, int upperBound, int numInstances) {

        List<EC2InstanceTO> instances = executionStateCache.getInstances();
        if (instances == null || instances.isEmpty()) {
            throw new RuntimeException("No Instances are available");
        }

        if (numInstances > instances.size()) {
            numInstances = instances.size();
        }

        IntStream.range(0, numInstances)
                .parallel()
                .forEach(
                        i ->
                        {
                            injectNetworkLatencyHelper(lowerBound, upperBound, instances.get(i).getInstanceId(), instances.get(i).getPrivateIpAddress());
                            executionStateCache.setHealNetwork(true);
                            executionStateCache.addUnHealthyInstance(instances.get(i));
                        });

        return this;
    }

    /**
     * Injects domain latency within the bounds on the default interface on each of the provided number of instances.
     *
     * @param domainName   domain name
     * @param lowerBound   lower bound for latency
     * @param upperBound   upper bound for latency
     * @param numInstances number of instances
     * @return the instance failure step definitions
     */
    @When("^inject domain network latency \"([^\"]*)\" for (\\d+) ms to (\\d+) ms on (\\d+) instances$")
    public InstanceFailureStepDefinitions injectNetworkLatency(String domainName, int lowerBound, int upperBound, int numInstances) {

        List<EC2InstanceTO> instances = executionStateCache.getInstances();
        if (instances == null || instances.isEmpty()) {
            throw new RuntimeException("No Instances are available");
        }

        if (numInstances > instances.size()) {
            numInstances = instances.size();
        }

        IntStream.range(0, numInstances)
                .parallel()
                .forEach(
                        i ->
                        {
                            String ip = instances.get(i).getPrivateIpAddress();
                            logger.info("injecting domain latency on ip: " + ip + " for domain " + domainName);
                            scriptExecutor.executeRandomDomainNetworkLatency(ip, String.valueOf(upperBound), String.valueOf(lowerBound), domainName);
                            executionStateCache.setHealNetwork(true);
                            executionStateCache.addUnHealthyInstance(instances.get(i));
                        } );

        return this;
    }

    /**
     * Terminate the process on the given number of provided instances.
     *
     * @param processName process name
     * @param numHosts number of instances
     * @param instances instances to perform action on
     */
    private void terminateProcess(String processName, int numHosts, List<EC2InstanceTO> instances) {
        IntStream.range(0, numHosts )
                .parallel()
                .forEach(
                        i ->
                        {
                            String ip = instances.get(i).getPrivateIpAddress();
                            String id = instances.get(i).getInstanceId();
                            logger.info("starting process termination on: " + id + " @ " + ip);
                            scriptExecutor.executeProcessTermination(ip, processName);
                            executionStateCache.setHealProcess(true);
                            executionStateCache.addUnHealthyInstance(instances.get(i));
                        });
    }

    


    /**
     * Stop the process on the given number of provided instances.
     *
     * @param processName process name
     * @param numHosts number of instances
     * @param instances instances to perform action on
     */
    private void stopProcess(String processName, int numHosts, List<EC2InstanceTO> instances) {
        IntStream.range(0, numHosts)
                .parallel()
                .forEach(
                        i ->
                        {
                            String ip = instances.get(i).getPrivateIpAddress();
                            String id = instances.get(i).getInstanceId();
                            logger.info("starting process termination on: " + id + " @ " + ip);
                            scriptExecutor.executeStopProcess(ip, processName);
                            executionStateCache.setHealProcess(true);
                            executionStateCache.addUnHealthyInstance(instances.get(i));
                        });
    }

    /**
     * Finds instances that match the name provided.
     *
     * @param name name to match
     * @return List of EC2InstanceTO representing instances with matching name
     */
    public List<EC2InstanceTO> findInstancesByName(String name) {
        if (name == null) {
            return null;
        }
        return ec2Raider.getInstancesByName(name);
    }

    /**
     * Finds instances that match the tag provided.
     *
     * @param tagString tag to match in the form of key:value
     * @return List of EC2InstanceTO representing instances with matching tag
     */
    public List<EC2InstanceTO> findAllInstancesByTag(String tagString) {
        if (tagString == null) {
            return null;
        }

        String[] arr = tagString.split(":");
        List<Tag> tags = new ArrayList<>();
        Tag tag = new Tag();
        tag.setKey(arr[0]);
        tag.setValue(arr[1]);
        tags.add(tag);

        return ec2Raider.getInstancesFromAnyTags(tags);
    }

    /**
     * Returns a EC2InstanceTO representing the instance with the provided id
     *
     * @param id instance id
     * @return EC2InstanceTO representing the instance with the provided id
     */
    public EC2InstanceTO getInstanceFromID(String id) {
        return ec2Raider.getEC2InstanceById(id);
    }
}