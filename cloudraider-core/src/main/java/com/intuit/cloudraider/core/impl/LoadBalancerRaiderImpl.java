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

import com.amazonaws.services.elasticloadbalancing.model.*;
import com.amazonaws.util.StringUtils;
import com.intuit.cloudraider.commons.LoadBalancerDelegator;
import com.intuit.cloudraider.core.interfaces.LoadBalancerRaider;
import com.intuit.cloudraider.exceptions.ResourceNotFoundException;
import com.intuit.cloudraider.exceptions.UnSupportedFeatureException;
import com.intuit.cloudraider.model.HealthCheckTarget;
import com.intuit.cloudraider.utils.HealthCheckUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * AWS Elastic Load Balancer functionality.
 * <p>
  */
@Component(value="elbRaiderBean")
public class LoadBalancerRaiderImpl implements LoadBalancerRaider {

    /**
     * The Logger.
     */
    Logger logger = LoggerFactory.getLogger(this.getClass());
    
    @Autowired
    private LoadBalancerDelegator loadBalancerDelegator;

    /**
     * Instantiates a new Load balancer raider.
     */
    public LoadBalancerRaiderImpl() {
    }


    /**
     * Simulates a health check failure.
     *
     * Note (11/2017): does not work if the health check ping port is divisible by 7,
     * will throw illegal state exception.
     *
     * @param loadBalancerName load balancer name
     */
    @Override
    public void forceFailLoadbalancerHealthCheck(final String loadBalancerName) {
        HealthCheck originalHealthCheck = validateAndGetExistingHealthCheck(loadBalancerName);
        int pingPort = HealthCheckUtils.toPingPort(originalHealthCheck);
        validatePingPort(pingPort);
        int wrongPingPort = HealthCheckUtils.scramblePingPort(pingPort);

        HealthCheckTarget healthCheckTarget = new HealthCheckTarget();
        healthCheckTarget.setPingPort(wrongPingPort);
        updateLoadbalancerHealthCheck(loadBalancerName, healthCheckTarget);
    }

    /**
     * Makes sure that the port selected is valid and unscrambled.
     *
     * @param pingPort port
     */
    private void validatePingPort(int pingPort) {
        if (HealthCheckUtils.isPingPortScrambled(pingPort))
            throw new IllegalStateException("The health check may have already been forced to fail.");
    }

    /**
     * Returns health check to original state.
     *
     * Note (11/2017): does not work if force fail method has not yet been called (health check ping port is not divisible by 7),
     * will throw illegal state exception.
     *
     * @param loadBalancerName load balancer name
     */
    @Override
    public void undoForceFailLoadbalancerHealthCheck(final String loadBalancerName) {
        HealthCheck originalHealthCheck = validateAndGetExistingHealthCheck(loadBalancerName);
        int pingPort = HealthCheckUtils.toPingPort(originalHealthCheck);
        validateScrambledPingPort(pingPort);
        int correctPingPort = HealthCheckUtils.unscramblePingPort(pingPort);

        HealthCheckTarget healthCheckTarget = new HealthCheckTarget();
        healthCheckTarget.setPingPort(correctPingPort);
        updateLoadbalancerHealthCheck(loadBalancerName, healthCheckTarget);
    }

    /**
     * Makes sure that the port selected is valid and already scrambled.
     *
     * @param pingPort port
     */
    public void validateScrambledPingPort(int pingPort) {
        if (!HealthCheckUtils.isPingPortScrambled(pingPort))
            throw new IllegalStateException("The health check may have not been forced to fail.");
    }

    /**
     * Update the health check settings for the given load balancer.
     *
     * @param loadBalancerName load balancer name
     * @param healthCheckTarget health check target
     */
    @Override
    public void updateLoadbalancerHealthCheck(final String loadBalancerName, final HealthCheckTarget healthCheckTarget) {
        HealthCheckTarget copyHealthCheckTarget = HealthCheckUtils.copy(healthCheckTarget);
        HealthCheck originalHealthCheck = validateAndGetExistingHealthCheck(loadBalancerName);// remove this if this method becomes private

        if (StringUtils.isNullOrEmpty(copyHealthCheckTarget.getPingPath()))
            copyHealthCheckTarget.setPingPath(HealthCheckUtils.toPingPath(originalHealthCheck));
        if (copyHealthCheckTarget.getPingProtocol() == null)
            copyHealthCheckTarget.setPingProtocol(HealthCheckUtils.toPingProtocol(originalHealthCheck));
        if (copyHealthCheckTarget.getPingPort() == null)
            copyHealthCheckTarget.setPingPort(HealthCheckUtils.toPingPort(originalHealthCheck));

        originalHealthCheck.withTarget(copyHealthCheckTarget.getTarget());

        ConfigureHealthCheckRequest configureHealthCheckRequest = new ConfigureHealthCheckRequest()
                .withLoadBalancerName(loadBalancerName)
                .withHealthCheck(originalHealthCheck);

        loadBalancerDelegator.getAmazonElasticLoadBalancing().configureHealthCheck(configureHealthCheckRequest);
    }

    /**
     * Confirms that a load balancer with the given name exists and gets its corresponding health check information.
     *
     * @param loadBalancerName load balancer name
     * @return AWS HealthCheck
     */
    public HealthCheck validateAndGetExistingHealthCheck(String loadBalancerName) {
        DescribeLoadBalancersResult describeLoadBalancersResult = this.describeLoadBalancers(loadBalancerName);
        if (describeLoadBalancersResult.getLoadBalancerDescriptions().isEmpty())
            throw new IllegalStateException("No load balancers exists for " + loadBalancerName);
        HealthCheck originalHealthCheck = describeLoadBalancersResult.getLoadBalancerDescriptions().get(0).getHealthCheck();
        if (originalHealthCheck == null)
            throw new IllegalStateException("No health check exists for " + loadBalancerName);
        return originalHealthCheck;
    }

    /**
     * Deletes the load balancer with the given name.
     *
     * @param loadBalancerName load balancer name
     * @throws ResourceNotFoundException if the load balancer does not exist.
     */
    @Override
    public void deleteLoadBalancer(String loadBalancerName) throws ResourceNotFoundException {
        DeleteLoadBalancerRequest deleteLoadBalancerRequest = new DeleteLoadBalancerRequest(loadBalancerName);
        if (isLoadBalancerExist(loadBalancerName)) {
            loadBalancerDelegator.getAmazonElasticLoadBalancing().deleteLoadBalancer(deleteLoadBalancerRequest);

        } else {
            throw new ResourceNotFoundException();
        }
    }

    /**
     * Checks if the load balancer with the given name exists.
     *
     * @param loadBalancerName load balancer name
     * @return true if the load balancer exists; false otherwise
     */
    private boolean isLoadBalancerExist(String loadBalancerName) {
        DescribeLoadBalancersResult describeLoadBalancersResult = this.describeLoadBalancers(loadBalancerName);
        return !(describeLoadBalancersResult.getLoadBalancerDescriptions().contains(loadBalancerName));
    }

//    public DescribeLoadBalancersResult getLoadBalancerDescription(String loadBalancerName)
//    {
//        if (isLoadBalancerExist(loadBalancerName)) {
//            DescribeLoadBalancersResult describeLoadBalancersResult = this.describeLoadBalancersResult(loadBalancerName);
//            return describeLoadBalancersResult;
//        }else{
//            throw new ResourceNotFoundException("LoadBalancer "+loadBalancerName +" is Not Exist");
//        }
//
//    }

    /**
     * Removes the specified load balancers from the given load balancer.
     *
     * @param loadBalancerName load balancer name
     * @param loadBalancerPorts client port numbers of the listeners
     */
    @Override
    public void deleteLoadBalancerListeners(String loadBalancerName, Integer... loadBalancerPorts) {
        if (isLoadBalancerExist(loadBalancerName)) {
            DeleteLoadBalancerListenersRequest deleteLoadBalancerListenersRequest = new DeleteLoadBalancerListenersRequest(loadBalancerName, Arrays.asList(loadBalancerPorts));
            loadBalancerDelegator.getAmazonElasticLoadBalancing().deleteLoadBalancerListeners(deleteLoadBalancerListenersRequest);
        } else {
            throw new ResourceNotFoundException();
        }
    }

    /**
     * Update the port number for one of the given load balancer's listeners.
     *
     * @param loadBalancerName load balancer name
     * @param oldPort old client port for listener
     * @param newPort new client port for listener
     */
    @Override
    public void updateLoadbalancerListner(String loadBalancerName, int oldPort, int newPort) {
        throw new UnSupportedFeatureException("We Dont Support this Feature currently");
    }

    /**
     * De-registers the number of instances from the given load balancer.
     *
     * @param loadBalancerName load balancer name
     * @param numberOfInstances number of instances
     * @throws ResourceNotFoundException if no instances are registered to the load balancer
     */
    @Override
    public void deregisterInstancesFromLoadBalancer(String loadBalancerName, int numberOfInstances) throws ResourceNotFoundException {
        List<String> loadBalancers = new ArrayList<String>();
        loadBalancers.add(loadBalancerName);
        DescribeLoadBalancersRequest describeLoadBalancersRequest = new DescribeLoadBalancersRequest(loadBalancers);
        DescribeLoadBalancersResult describeLoadBalancersResult = loadBalancerDelegator.getAmazonElasticLoadBalancing().describeLoadBalancers(describeLoadBalancersRequest);

        if (describeLoadBalancersResult.getLoadBalancerDescriptions().get(0).getInstances().size() > 0) {
            DeregisterInstancesFromLoadBalancerRequest deregisterInstancesFromLoadBalancerRequest = new DeregisterInstancesFromLoadBalancerRequest(loadBalancerName, describeLoadBalancersResult.getLoadBalancerDescriptions().get(0).getInstances().subList(0, numberOfInstances));
            loadBalancerDelegator.getAmazonElasticLoadBalancing().deregisterInstancesFromLoadBalancer(deregisterInstancesFromLoadBalancerRequest);
        } else {
            throw new ResourceNotFoundException("No instances in LoadBalancer " + loadBalancerName);
        }
    }

    /**
     * De-registers all instances from the given load balancer.
     *
     * @param loadBalancerName load balancer name
     * @throws ResourceNotFoundException if no instances are registered to the load balancer
     */
    @Override
    public void deregisterInstancesFromLoadBalancer(String loadBalancerName) throws ResourceNotFoundException {
        DescribeLoadBalancersResult describeLoadBalancersResult = describeLoadBalancers(loadBalancerName);
        if (describeLoadBalancersResult.getLoadBalancerDescriptions().get(0).getInstances().size() > 0) {
            DeregisterInstancesFromLoadBalancerRequest deregisterInstancesFromLoadBalancerRequest = new DeregisterInstancesFromLoadBalancerRequest(loadBalancerName, describeLoadBalancersResult.getLoadBalancerDescriptions().get(0).getInstances());
            loadBalancerDelegator.getAmazonElasticLoadBalancing().deregisterInstancesFromLoadBalancer(deregisterInstancesFromLoadBalancerRequest);
        } else {
            throw new ResourceNotFoundException("No instances in LoadBalancer " + loadBalancerName);
        }
    }

    /**
     * De-register the given instances from the provided load balancer.
     *
     * @param loadBalancerName load balancer name
     * @param instances list of instance ids
     * @throws ResourceNotFoundException if the load balancer does not exist
     */
    @Override
    public void deregisterInstancesFromLoadBalancer(String loadBalancerName, List<String> instances) throws ResourceNotFoundException {
        if (isLoadBalancerExist(loadBalancerName)) {
            DeregisterInstancesFromLoadBalancerRequest deregisterInstancesFromLoadBalancerRequest = new DeregisterInstancesFromLoadBalancerRequest()
                    .withLoadBalancerName(loadBalancerName)
                    .withInstances(instances.stream()
                            .map(com.amazonaws.services.elasticloadbalancing.model.Instance::new)
                            .collect(Collectors.toList()));

            loadBalancerDelegator.getAmazonElasticLoadBalancing().deregisterInstancesFromLoadBalancer(deregisterInstancesFromLoadBalancerRequest);
        } else {
            throw new ResourceNotFoundException();
        }
    }

    /**
     * Register the given instances to the specified load balancer.
     *
     * @param loadBalancerName load balancer name
     * @param instances list of instance ids
     * @throws ResourceNotFoundException if the load balancer does not exist
     */
    @Override
    public void registerInstancesFromLoadBalancer(String loadBalancerName, List<String> instances) throws ResourceNotFoundException {
        if (isLoadBalancerExist(loadBalancerName)) {
            RegisterInstancesWithLoadBalancerRequest registerInstancesWithLoadBalancerRequest = new RegisterInstancesWithLoadBalancerRequest()
                    .withLoadBalancerName(loadBalancerName)
                    .withInstances(instances.stream()
                            .map(com.amazonaws.services.elasticloadbalancing.model.Instance::new)
                            .collect(Collectors.toList())
                    );
            loadBalancerDelegator.getAmazonElasticLoadBalancing().registerInstancesWithLoadBalancer(registerInstancesWithLoadBalancerRequest);
        } else {
            throw new ResourceNotFoundException();
        }
    }

    /**
     * Deletes the given policy from the specified load balancer.
     *
     * @param loadBalancerName load balancer name
     * @param policyName policy name
     * @throws ResourceNotFoundException if the load balancer does not exist
     */
    @Override
    public void deleteLoadBalancerPolicy(String loadBalancerName, String policyName) throws ResourceNotFoundException {
        if (isLoadBalancerExist(loadBalancerName)) {
            DeleteLoadBalancerPolicyRequest deleteLoadBalancerPolicyRequest = new DeleteLoadBalancerPolicyRequest(loadBalancerName, policyName);
            loadBalancerDelegator.getAmazonElasticLoadBalancing().deleteLoadBalancerPolicy(deleteLoadBalancerPolicyRequest);
        } else {
            throw new ResourceNotFoundException();
        }
    }

    /**
     * Attaches the given subnets to the specified load balancer.
     *
     * @param loadBalancerName load balancer name
     * @param subnets subnet ids to add
     * @throws ResourceNotFoundException if the load balancer does not exist
     */
    @Override
    public void attachLoadBalancerToSubnets(String loadBalancerName, String... subnets) throws ResourceNotFoundException {
        if (isLoadBalancerExist(loadBalancerName)) {
            AttachLoadBalancerToSubnetsRequest attachLoadBalancerToSubnetsRequest = new AttachLoadBalancerToSubnetsRequest().withLoadBalancerName(loadBalancerName).withSubnets(subnets);
            loadBalancerDelegator.getAmazonElasticLoadBalancing().attachLoadBalancerToSubnets(attachLoadBalancerToSubnetsRequest);
        } else {
            throw new ResourceNotFoundException("No LoadBalancer with Name " + loadBalancerName);
        }
    }

    /**
     * Detaches the given subnets to the specified load balancer.
     *
     * @param loadBalancerName load balancer name
     * @param subnets subnet ids to add
     * @throws ResourceNotFoundException if the load balancer does not exist
     */
    @Override
    public void detachLoadBalancerFromSubnets(String loadBalancerName, String... subnets) throws ResourceNotFoundException {
        if (isLoadBalancerExist(loadBalancerName)) {
            DetachLoadBalancerFromSubnetsRequest detachLoadBalancerFromSubnetsRequest = new DetachLoadBalancerFromSubnetsRequest().withLoadBalancerName(loadBalancerName).withSubnets(subnets);
            loadBalancerDelegator.getAmazonElasticLoadBalancing().detachLoadBalancerFromSubnets(detachLoadBalancerFromSubnetsRequest);
        } else {
            throw new ResourceNotFoundException();
        }
    }

    /**
     * Disables the given availability zones on the specified load balancer.
     *
     * @param loadBalancerName load balancer name
     * @param availabilityZones availability zone ids
     * @throws ResourceNotFoundException if the load balancer does not exist
     */
    @Override
    public void disableAvailabilityZonesForLoadBalancer(String loadBalancerName, String... availabilityZones) throws ResourceNotFoundException {
        if (isLoadBalancerExist(loadBalancerName)) {
            DisableAvailabilityZonesForLoadBalancerRequest disableAvailabilityZonesForLoadBalancerRequest = new DisableAvailabilityZonesForLoadBalancerRequest().withLoadBalancerName(loadBalancerName).withAvailabilityZones(availabilityZones);
            loadBalancerDelegator.getAmazonElasticLoadBalancing().disableAvailabilityZonesForLoadBalancer(disableAvailabilityZonesForLoadBalancerRequest);
        } else {
            throw new ResourceNotFoundException();
        }
    }

    /**
     * Enables the given availability zones on the specified load balancer.
     *
     * @param loadBalancerName load balancer name
     * @param availabilityZones availability zone ids
     * @throws ResourceNotFoundException if the load balancer does not exist
     */
    @Override
    public void enableAvailabilityZonesForLoadBalancer(String loadBalancerName, String... availabilityZones) throws ResourceNotFoundException {
        EnableAvailabilityZonesForLoadBalancerRequest enableAvailabilityZonesForLoadBalancerRequest = new EnableAvailabilityZonesForLoadBalancerRequest().withLoadBalancerName(loadBalancerName).withAvailabilityZones(availabilityZones);
        loadBalancerDelegator.getAmazonElasticLoadBalancing().enableAvailabilityZonesForLoadBalancer(enableAvailabilityZonesForLoadBalancerRequest);
    }

    /**
     * Checks if the given load balancer exists.
     *
     * @param loadBalancerName load balancer name
     * @return true if the load balancer no longer exists
     */
    @Override
    public Boolean isDeleted(String loadBalancerName) throws ResourceNotFoundException {
        return (describeLoadBalancers(loadBalancerName).getLoadBalancerDescriptions().get(0) == null);
    }

    /**
     * Gets the instances attached to the current load balancer
     *
     * @param loadBalancerName load balancer name
     * @return list of AWS instances
     */
    @Override
    public List<Instance> getLoadBalancerInstances(String loadBalancerName) throws ResourceNotFoundException {
        return describeLoadBalancers(loadBalancerName).getLoadBalancerDescriptions().get(0).getInstances();
    }

    /**
     * Feature is currently disabled.
     *
     * Update the load balancer name.
     *
     * @param loadBalancerName old name
     * @param newLoadBalancerName new name
     * @throws UnSupportedFeatureException currently feature is disabled
     */
    @Override
    public void changeLoadBalancerName(String loadBalancerName, String newLoadBalancerName) throws UnSupportedFeatureException {
//        ModifyLoadBalancerAttributesRequest modifyLoadBalancerAttributesRequest = new ModifyLoadBalancerAttributesRequest().withLoadBalancerName(loadBalancerName);
//        loadBalancerDelegator.getAmazonElasticLoadBalancing().modifyLoadBalancerAttributes(modifyLoadBalancerAttributesRequest).setLoadBalancerName(newLoadBalancerName);
        throw new UnSupportedFeatureException("We have disabled this feature");
    }

    /**
     * Deprecated.
     * Replaced with describeLoadBalancers().
     *
     * @param loadBalancerName the load balancer name
     * @return the describe load balancers result
     */
    @Deprecated
    public DescribeLoadBalancersResult describeLoadBalancersResult(String loadBalancerName) {
        return describeLoadBalancers(loadBalancerName);
    }

    /**
     * Get all load balancer information for the load balancer with the matching name.
     *
     * @param loadBalancerName load balancer name
     * @return DescribeLoadBalancerResult describe load balancers result
     */
    public DescribeLoadBalancersResult describeLoadBalancers(String loadBalancerName) {
        List<String> loadBalancers = new ArrayList<String>();
        loadBalancers.add(loadBalancerName);
        DescribeLoadBalancersRequest describeLoadBalancersRequest = new DescribeLoadBalancersRequest(loadBalancers);
        return loadBalancerDelegator.getAmazonElasticLoadBalancing().describeLoadBalancers(describeLoadBalancersRequest);
    }

    /**
     * Gets all in service instances attached to the provided load balancer.
     *
     * @param elbName load balancer name
     * @return list of instance ids
     */
    @Override
    public List<String> getInServiceInstances(String elbName) {
        return getServiceInstances(elbName, "InService");
    }

    /**
     * Gets all out of service instances attached to the provided load balancer.
     *
     * @param elbName load balancer name
     * @return list of instance ids
     */
    @Override
    public List<String> getOutOfServiceInstances(String elbName) {
        return getServiceInstances(elbName, "OutOfService");
    }

    /**
     * Helper function for getting In or Out Service instances.
     *
     * @param elbName load balancer name.
     * @param state state to match
     * @return list of instance ids
     */
    private List<String> getServiceInstances(String elbName, String state) {
        DescribeInstanceHealthRequest healthRequest = new DescribeInstanceHealthRequest();
        healthRequest.setLoadBalancerName(elbName);

        List<String> instances = loadBalancerDelegator.getAmazonElasticLoadBalancing().describeInstanceHealth(healthRequest)
                .getInstanceStates()
                .stream()
                .filter(x -> x.getState().equalsIgnoreCase(state))
                .map(x -> x.getInstanceId())
                .collect(Collectors.toList());

        Collections.shuffle(instances);

        return instances;
    }

    /**
     * Duplicate function of deleteSecurityGroups().
     */
    @Deprecated
    public void deleteSecurityGroup(String elbName, String securityGroup) throws ResourceNotFoundException {
        List<String> securityGroups = this.getSecurityGroups(elbName);
        if (securityGroups != null && securityGroup.contains(securityGroup)) {
            securityGroups.remove(securityGroup);
            loadBalancerDelegator.getAmazonElasticLoadBalancing().applySecurityGroupsToLoadBalancer(
                    new ApplySecurityGroupsToLoadBalancerRequest()
                            .withLoadBalancerName(elbName)
                            .withSecurityGroups(securityGroups));
        }
    }

    /**
     * Duplicate function of addSecurityGroups().
     */
    @Deprecated
    public void addSecurityGroup(String elbName, String securityGroup) throws ResourceNotFoundException {
        List<String> securityGroups = this.getSecurityGroups(elbName);
        if (securityGroups != null && !securityGroups.contains(securityGroup)) {
            securityGroups.add(securityGroup);
            loadBalancerDelegator.getAmazonElasticLoadBalancing().applySecurityGroupsToLoadBalancer(
                    new ApplySecurityGroupsToLoadBalancerRequest()
                            .withLoadBalancerName(elbName)
                            .withSecurityGroups(securityGroups));
        }
    }

    /**
     * Gets all attached security groups to the given load balancer.
     *
     * @param elbName load balancer name
     * @return list of security group ids
     */
    @Override
    public List<String> getSecurityGroups(String elbName) throws ResourceNotFoundException {
        return loadBalancerDelegator.getAmazonElasticLoadBalancing().describeLoadBalancers(new DescribeLoadBalancersRequest().withLoadBalancerNames(elbName))
                .getLoadBalancerDescriptions()
                .get(0)
                .getSecurityGroups();
    }

    /**
     * Detach the given security groups from the specified load balancer.
     *
     * @param elbName load balancer name
     * @param securityGroups security group ids
     */
    @Override
    public void deleteSecurityGroups(String elbName, String... securityGroups) throws ResourceNotFoundException {
        List<String> existingSecurityGroups = this.getSecurityGroups(elbName);
        if (existingSecurityGroups != null && existingSecurityGroups.containsAll(Arrays.asList(securityGroups))) {
            existingSecurityGroups.removeAll(Arrays.asList(securityGroups));
            loadBalancerDelegator.getAmazonElasticLoadBalancing().applySecurityGroupsToLoadBalancer(
                    new ApplySecurityGroupsToLoadBalancerRequest()
                            .withLoadBalancerName(elbName)
                            .withSecurityGroups(existingSecurityGroups));
        }
    }

    /**
     * Attach the given security groups to the specified load balancer.
     *
     * @param elbName load balancer name
     * @param securityGroups security group ids
     */
    @Override
    public void addSecurityGroups(String elbName, String... securityGroups) throws ResourceNotFoundException {
        List<String> existingSecurityGroups = this.getSecurityGroups(elbName);
        if (existingSecurityGroups != null && !existingSecurityGroups.containsAll(Arrays.asList(securityGroups))) {
            existingSecurityGroups.addAll(Arrays.asList(securityGroups));
            loadBalancerDelegator.getAmazonElasticLoadBalancing().applySecurityGroupsToLoadBalancer(
                    new ApplySecurityGroupsToLoadBalancerRequest()
                            .withLoadBalancerName(elbName)
                            .withSecurityGroups(existingSecurityGroups));
        }
    }

    /**
     * Gets the subnets attached to the specified load balancer.
     *
     * @param loadBalancerName load balancer name
     * @return list of subnet ids
     */
    @Override
    public List<String> getLoadBalancerSubnets(String loadBalancerName) {
        List<String> subnets = new ArrayList<>();
        describeLoadBalancers(loadBalancerName).getLoadBalancerDescriptions().stream().forEach(i -> {
            subnets.addAll(i.getSubnets());
        });
        return subnets;
        // return describeLoadBalancers(loadBalancerName).getLoadBalancerDescriptions().get(0).getSubnets();
    }

    /**
     * Gets all the Elastic Load Balancers associated with the account.
     *
     * @return Map of (load balancer name, load balancer type)
     */
    @Override
    public Map<String, String> getLoadBalancerNames() throws ResourceNotFoundException {
        DescribeLoadBalancersRequest describeLoadBalancersRequest = new DescribeLoadBalancersRequest();
        List<LoadBalancerDescription> descriptions = loadBalancerDelegator.getAmazonElasticLoadBalancing().describeLoadBalancers(describeLoadBalancersRequest).getLoadBalancerDescriptions();
        List<String> loadBalancers = descriptions.stream().map(LoadBalancerDescription::getLoadBalancerName).collect(Collectors.toList());
        Map<String, String> map = new HashMap<>();
        for (String lb : loadBalancers) {
            map.put(lb.toLowerCase(), "ELB");
        }
        return map;
    }
}
