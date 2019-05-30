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

import com.intuit.cloudraider.core.impl.ApplicationLoadBalancerRaiderImpl;
import com.intuit.cloudraider.core.impl.LoadBalancerRaiderImpl;
import com.intuit.cloudraider.core.interfaces.EC2Raider;
import com.intuit.cloudraider.core.interfaces.LoadBalancerRaider;
import com.intuit.cloudraider.cucumber.model.ExecutionStateCache;
import com.intuit.cloudraider.model.Credentials;
import com.intuit.cloudraider.model.EC2InstanceTO;
import com.intuit.cloudraider.utils.Ec2Utils;
import com.intuit.cloudraider.utils.Randomizer;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Cucumber Step Definitions for ALB and ELB functionality.
 */
public class LoadBalancerStepDefinitions {

    private LoadBalancerRaider loadBalancerRaider;

    @Autowired
    @Qualifier("ec2raiderBean")
    private EC2Raider ec2Raider;

    @Autowired
    private ExecutionStateCache executionStateCache;

    /**
     * The Logger.
     */
    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
   // @Qualifier("credentials")
    private Credentials credentials;


    @Autowired
    private ApplicationLoadBalancerRaiderImpl applicationLoadBalancerRaider;

    @Autowired
    private LoadBalancerRaiderImpl elasticLoadBalancerRaider;

    public LoadBalancerStepDefinitions() {

    }

    public ExecutionStateCache getExecutionStateCache() {
        return executionStateCache;
    }

    public void setExecutionStateCache(ExecutionStateCache executionStateCache) {
        this.executionStateCache = executionStateCache;
    }

    /**
     * Sets the load balancer to use along with its attached instances.
     *
     * @param loadbalancerType type of the load balancer (ELB, ALB, NLB)
     * @param loadBalancerName name of the load balancer
     */
    @Given("^(ELB|ALB|NLB) \"([^\"]*)\"$")
    public LoadBalancerStepDefinitions givenLoadBalancerName(String loadbalancerType, String loadBalancerName) throws Throwable {

        if (loadbalancerType.equalsIgnoreCase("ELB")) {
            loadBalancerRaider = elasticLoadBalancerRaider;
        } else {
            loadBalancerRaider = applicationLoadBalancerRaider;
        }

        executionStateCache.setLoadBalancerName(loadBalancerName);
        executionStateCache.clearInstances();
        executionStateCache.addInstances(ec2Raider.getEC2InstancesByIds(findAllInServiceInstances()));
        return this;
    }

    public void givenLoadBalancerNameIgnoreExecCache(String loadbalancerType, String loadBalancerName) {
        if (loadbalancerType.equalsIgnoreCase("ELB")) {
            loadBalancerRaider = elasticLoadBalancerRaider;
        } else {
            loadBalancerRaider = applicationLoadBalancerRaider;
        }

        executionStateCache.setLoadBalancerName(loadBalancerName);
        executionStateCache.clearInstances();
    }

    /**
     * Gets all load balancers, of any type, associated with the provided account.
     *
     * @return Map of (Load Balancer Name, Load Balancer Type)
     */
    public Map<String, String> getLoadBalancerNames() {
        Map<String, String> map = applicationLoadBalancerRaider.getLoadBalancerNames();
        map.putAll(loadBalancerRaider.getLoadBalancerNames());
        return map;
    }

    /**
     * Detaches the given security group from the load balancer.
     *
     * @param securityGroupId security group id
     */
    @Given("^detach security-group \"([^\"]*)\"$")
    public LoadBalancerStepDefinitions detachSecurityGroup(String securityGroupId) {
        executionStateCache.addDetachedSecurityGroup(securityGroupId);
        logger.info("Detaching security group " + securityGroupId + " from " + executionStateCache.getLoadBalancerName());
        loadBalancerRaider.deleteSecurityGroups(executionStateCache.getLoadBalancerName(), securityGroupId);
        return this;
    }

    /**
     * Attaches the given security group to the load balancer.
     *
     * @param securityGroupId security group id
     */
    @Given("^attach security-group \"([^\"]*)\"$")
    public LoadBalancerStepDefinitions attachSecurityGroup(String securityGroupId) {
        loadBalancerRaider.addSecurityGroups(executionStateCache.getLoadBalancerName(), securityGroupId);
        return this;
    }

    /**
     * Randomly detaches the given number of security groups from the load balancer. If the number is larger than the
     * number of attached security groups, all available security groups are detached. NOTE that the load balancer
     * requires at least one security group to function, so one group will always be left over.
     *
     * @param num number of security groups to detach
     */
    @Given("^detach (\\d+) security-groups$")
    public LoadBalancerStepDefinitions detachRandomSecurityGroups(int num) {
        List<String> groups = loadBalancerRaider.getSecurityGroups(executionStateCache.getLoadBalancerName());
        if (groups == null || groups.isEmpty()) {
            throw new RuntimeException("No groups are available");
        }
        if (num > groups.size()) {
            num = groups.size();
        }
        for (int i = 0; i < num; i++) {
            groups = loadBalancerRaider.getSecurityGroups(executionStateCache.getLoadBalancerName());
            String group = groups.get(Randomizer.generateInt(0, groups.size()));
            if (executionStateCache.getDetachedSecurityGroups().contains(group)) {
                i--;
                continue;
            }
            executionStateCache.addDetachedSecurityGroup(group);
            logger.info("Detaching security group " + group + " from " + executionStateCache.getLoadBalancerName());
            loadBalancerRaider.deleteSecurityGroups(executionStateCache.getLoadBalancerName(), group);
        }
        return this;
    }

    /**
     * Attach removed security groups to the load balancer.
     */
    @Given("^attach removed security-groups$")
    public LoadBalancerStepDefinitions attachRandomRemovedSecurityGroups() {
        for (String group : executionStateCache.getDetachedSecurityGroups()) {
            loadBalancerRaider.addSecurityGroups(executionStateCache.getLoadBalancerName(), group);
        }
        executionStateCache.removeDetachedSecurityGroups(executionStateCache.getDetachedSecurityGroups());
        return this;
    }

    /**
     * Detaches the given subnet from the load balancer.
     *
     * @param subnetId subnet id
     */
    @Given("^detach subnet \"([^\"]*)\"$")
    public LoadBalancerStepDefinitions detachSubnet(String subnetId) {
        executionStateCache.addDetachedSubnet(subnetId);
        logger.info("Detaching subnet " + subnetId + " from " + executionStateCache.getLoadBalancerName());
        loadBalancerRaider.detachLoadBalancerFromSubnets(executionStateCache.getLoadBalancerName(), subnetId);
        return this;
    }

    /**
     * Randomly detaches the given number of subnets from the load balancer. If the number is larger than the
     * number of attached subnets, all available subnets are detached. NOTE that the load balancer requires at least
     * two subnets to function, so two subnets will always be left over.
     *
     * @param num number of subnets to detach
     */
    @Given("^detach (\\d+) subnets$")
    public LoadBalancerStepDefinitions detachRandomSubnets(int num) {
        List<String> subnets = loadBalancerRaider.getLoadBalancerSubnets(executionStateCache.getLoadBalancerName());
        if (subnets == null || subnets.isEmpty()) {
            throw new RuntimeException("No subnets are available");
        }
        if (num > subnets.size()) {
            num = subnets.size();
        }
        for (int i = 0; i < num; i++) {
            subnets = loadBalancerRaider.getLoadBalancerSubnets(executionStateCache.getLoadBalancerName());
            String subnet = subnets.get(Randomizer.generateInt(0, subnets.size()));
            if (executionStateCache.getDetachedSubnets().contains(subnet)) {
                i--;
                continue;
            }
            executionStateCache.addDetachedSubnet(subnet);
            logger.info("Detaching subnet " + subnet + " from " + executionStateCache.getLoadBalancerName());
            loadBalancerRaider.detachLoadBalancerFromSubnets(executionStateCache.getLoadBalancerName(), subnet);
        }
        return this;
    }

    /**
     * Attach removed subnets to the load balancer.
     */
    @Given("^attach removed subnets$")
    public LoadBalancerStepDefinitions attachRandomRemovedSubnets() {
        for (String subnet : executionStateCache.getDetachedSubnets()) {
            loadBalancerRaider.attachLoadBalancerToSubnets(executionStateCache.getLoadBalancerName(), subnet);
        }
        executionStateCache.removeDetachedSubnets(executionStateCache.getDetachedSubnets());
        return this;
    }

    /**
     * Attach the provided subnet to the load balancer.
     *
     * @param subnetId subnet id
     */
    @Given("^attach subnet \"([^\"]*)\"$")
    public LoadBalancerStepDefinitions attachSubnet(String subnetId) {
        executionStateCache.removeDetachedSubnet(subnetId);
        loadBalancerRaider.attachLoadBalancerToSubnets(executionStateCache.getLoadBalancerName(), subnetId);
        return this;
    }

    /**
     * Assert that the number of healthy hosts attached to the load balancer matches what is expected.
     *
     * @param expected expected number of healthy hosts
     */
    @Then("^assertEC2 healthy host count = (\\d+)$")
    public void assertHealthyHostCount(int expected) throws Throwable {
        logger.info("healthy host count " + findAllInServiceInstances().size());
        Assert.assertTrue("Healthy host count mismatched ", this.confirmHealthyHostCount(expected));
    }

    /**
     * Assert that the number of unhealthy hosts attached to the load balancer matches what is expected.
     *
     * @param expected expected number of unhealthy hosts
     */
    @Then("^assertEC2 unhealthy host count = (\\d+)$")
    public void assertUntHealthyHostCount(int expected) throws Throwable {
        Assert.assertFalse("Unhealthy host count mismatched ", this.confirmHealthyHostCount(expected));
    }

    /**
     * Corrupt the load balancer's health check process so that it no longer works properly,
     */
    @When("^LB corrupt HealthChecks$")
    public void corruptLBHealthCheck() throws Throwable {
        loadBalancerRaider.forceFailLoadbalancerHealthCheck(executionStateCache.getLoadBalancerName());
    }

    /**
     * Return the load balancer's health check process back to working condition.
     */
    @Then("^LB unCorrupt HealthChecks$")
    public void unCorruptLBHealthCheck() throws Throwable {
        loadBalancerRaider.undoForceFailLoadbalancerHealthCheck(executionStateCache.getLoadBalancerName());
    }

    /**
     * De-register the given instance from the load balancer.
     *
     * @param instanceID instance id
     */
    @When("^detach instanceId \"([^\"]*)\" from loadbalancer$")
    public void deRegisterGivenInstance(String instanceID) {
        List<String> list = new ArrayList<>();
        list.add(instanceID);
        logger.info("Detaching instance  " + instanceID + " from " + executionStateCache.getLoadBalancerName());
        executionStateCache.addDeregisteredInstance(executionStateCache.findEC2InstanceGivenID(instanceID));
        loadBalancerRaider.deregisterInstancesFromLoadBalancer(executionStateCache.getLoadBalancerName(), list);
    }

    /**
     * Detach the given percentage of instances attached to the load balancer, choosing randomly once the number of
     * instances to detach is determined.
     *
     * @param percentage percentage of instances (0 to 100 inclusive)
     */
//    @When("^detach (\\d+)% instances from loadbalancer$")
    public LoadBalancerStepDefinitions deRegisterPercentOfInstances(double percentage) {
        List<EC2InstanceTO> instances = executionStateCache.getInstances();
        int numInstances = (int) Math.round((percentage * instances.size())/100);

        if (instances.isEmpty()) {
            throw new RuntimeException("No Instances are available");
        }

        List<EC2InstanceTO> instanceList = instances;
        if (instanceList.size() != numInstances) {
            instanceList = instances.subList(0,numInstances);
        }

        loadBalancerRaider.deregisterInstancesFromLoadBalancer(executionStateCache.getLoadBalancerName(), Ec2Utils.generateInstanceIdList(instanceList));
        executionStateCache.addDeregisteredInstance(instanceList);

        return this;
    }

    /**
     * Detach the provided number of instances that are attached to the load balancer.
     *
     * @param numInstances number of instances
     */
    @When("^detach (\\d+) instances from loadbalancer$")
    public LoadBalancerStepDefinitions deRegisterInstances(int numInstances) {
        List<EC2InstanceTO> instances = executionStateCache.getInstances();

        if (instances == null || instances.isEmpty())
        {
            throw new RuntimeException("No Instances are vailable");
        }

        logger.info("number of available instances: " + instances.size());
        if (numInstances > instances.size()) {
            numInstances = instances.size();
        }

        List<EC2InstanceTO> instanceList = instances;
        if (instanceList.size() != numInstances) {
            instanceList = instances.subList(0, numInstances);
        }

        logger.info("Detaching instances " + instanceList.stream().map(x -> x.getInstanceId()).collect(Collectors.toList())
                + " from " + executionStateCache.getLoadBalancerName());

        loadBalancerRaider.deregisterInstancesFromLoadBalancer(executionStateCache.getLoadBalancerName(), Ec2Utils.generateInstanceIdList(instanceList));
        executionStateCache.addDeregisteredInstance(instanceList);

        return this;
    }

    /**
     * Detach the percentage of instances that are attached to the load balancer.
     *
     * @param percentage number of instances
     */
    @When("^detach (\\d+)% instances from loadbalancer$")
    public LoadBalancerStepDefinitions deRegisterPercentOfInstances(int percentage)
    {
        List<EC2InstanceTO> instances = executionStateCache.getInstances();
        int numInstances = Math.round((percentage * instances.size())/100);

        if (instances == null || instances.isEmpty())
        {
            throw new RuntimeException("No Instances are vailable");
        }

        List<EC2InstanceTO> instanceList = instances;
        if (instanceList.size() != numInstances)
        {
            instanceList = instances.subList(0,numInstances);
        }


        loadBalancerRaider.deregisterInstancesFromLoadBalancer(executionStateCache.getLoadBalancerName(),Ec2Utils.generateInstanceIdList(instanceList));
        executionStateCache.addDeregisteredInstance(instanceList);

        return this;

    }


    @When("^attach unregistered instances$")
    public LoadBalancerStepDefinitions registerInstances() {
        List<EC2InstanceTO> instances = executionStateCache.getDeregistedInstance();
        if (instances == null || instances.isEmpty()) {
            throw new RuntimeException("No Instances are available");
        }

        loadBalancerRaider.registerInstancesFromLoadBalancer(executionStateCache.getLoadBalancerName(), Ec2Utils.generateInstanceIdList(instances));
        executionStateCache.clearDeregisteredInstance();

        return this;
    }

    /**
     * Checks if the number of unhealthy hosts matches what is expected.
     *
     * @param expected expected number
     * @return true if the expected number and the actual number match
     */
    public boolean confirmUnHealthyHostCount(int expected) {
        return (findAllOutOfServiceInstances().size() == expected);
    }

    /**
     * Checks if the number of healthy hosts matches what is expected.
     *
     * @param expected expected number
     * @return true if the expected number and the actual number match
     */
    public boolean confirmHealthyHostCount(int expected) throws Throwable {
        return (findAllInServiceInstances().size() == expected);
    }

    /**
     * Returns list of security groups attached to the load balancer.
     *
     * @return list of security group ids
     */
    public List<String> getSecurityGroups() {
        return loadBalancerRaider.getSecurityGroups(executionStateCache.getLoadBalancerName());
    }

    /**
     * Returns list of subnets attached to the load balancer.
     *
     * @return list of subnet ids
     */
    public List<String> getSubnets() {
        return loadBalancerRaider.getLoadBalancerSubnets(executionStateCache.getLoadBalancerName());
    }

    /**
     * Returns list of in service instances attached to the load balancer.
     *
     * @return list of in service instances
     */
    public List<String> findAllInServiceInstances() {
        List<String> instances = null;

        if (executionStateCache.getLoadBalancerName() == null) {
            throw new RuntimeException("LoadBalancer Name is not provided");
        }

        instances = loadBalancerRaider.getInServiceInstances(executionStateCache.getLoadBalancerName());
        return instances;
    }

    /**
     * Returns list of in service instances attached to the load balancer provided.
     *
     * @param loadBalancerName load balancer name
     * @return list of in service instances
     */
    public List<String> findAllInServiceInstances(String loadBalancerName) {
        return loadBalancerRaider.getInServiceInstances(loadBalancerName);
    }

    /**
     * Returns list of out of service instances attached to the load balancer.
     *
     * @return list of out of service instances
     */
    public List<String> findAllOutOfServiceInstances() {
        List<String> instances = null;

        if (executionStateCache.getLoadBalancerName() == null) {
            throw new RuntimeException("LoadBalancer Name is not provided");
        }

        instances = loadBalancerRaider.getOutOfServiceInstances(executionStateCache.getLoadBalancerName());
        return instances;
    }
}
