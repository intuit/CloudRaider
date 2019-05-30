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

import com.amazonaws.services.elasticloadbalancing.model.Instance;
import com.amazonaws.services.elasticloadbalancingv2.model.*;
import com.amazonaws.util.StringUtils;
import com.intuit.cloudraider.commons.ApplicationLoadBalancerDelegator;
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
 * AWS Application Load Balancer functionality.
 * <p>
  */
@Component(value="albRaiderBean")
public class ApplicationLoadBalancerRaiderImpl implements LoadBalancerRaider {

    /**
     * The Logger.
     */
    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ApplicationLoadBalancerDelegator applicationLoadBalancerDelegator;

    /**
     * Instantiates a new Application load balancer raider.
     */
    public ApplicationLoadBalancerRaiderImpl() {
    }


    /**
     * Deletes the load balancer with the given name.
     *
     * @param loadBalancerName load balancer name
     * @throws ResourceNotFoundException if the load balancer does not exist.
     */
    @Override
    public void deleteLoadBalancer(String loadBalancerName) throws ResourceNotFoundException {
        DeleteLoadBalancerRequest deleteLoadBalancerRequest = new DeleteLoadBalancerRequest().withLoadBalancerArn(getLoadBalancerArn(loadBalancerName));
        applicationLoadBalancerDelegator.getAmazonApplicationLoadBalancing().deleteLoadBalancer(deleteLoadBalancerRequest);
    }

    /**
     * Gets the ARN for the specified load balancer.
     *
     * @param loadBalancerName load balancer name
     * @return load balancer arn
     */
    private String getLoadBalancerArn(String loadBalancerName) {
        return applicationLoadBalancerDelegator.getAmazonApplicationLoadBalancing().describeLoadBalancers(new DescribeLoadBalancersRequest().withNames(loadBalancerName))
                .getLoadBalancers()
                .parallelStream()
                .map(LoadBalancer::getLoadBalancerArn)
                .findFirst()
                .get();
    }

    /**
     * Feature is not implemented.
     *
     * Removes the specified load balancers from the given load balancer.
     *
     * @param loadBalancerName load balancer name
     * @param loadBalancerPorts client port numbers of the listeners
     */
    @Override
    public void deleteLoadBalancerListeners(String loadBalancerName, Integer... loadBalancerPorts) {
//        try{
//            DeleteLoadBalancerListenersRequest deleteLoadBalancerListenersRequest = new DeleteLoadBalancerListenersRequest(loadBalancerName, Arrays.asList(loadBalancerPorts));
//            return applicationLoadBalancerDelegator.getAmazonApplicationLoadBalancing().deleteLoadBalancerListeners(deleteLoadBalancerListenersRequest).toString();
//        } catch (Exception e){
//            throw new RuntimeException(e.getMessage());
//        }
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
        throw new UnSupportedFeatureException("This Feature is not support currently");
    }

    /**
     * De-registers the number of instances from the given load balancer.
     *
     * @param loadBalancerName load balancer name
     * @param numberOfInstances number of instances
     * @throws ResourceNotFoundException if no instances are registered to the load balancer
     */
    @Override
    public void deregisterInstancesFromLoadBalancer(String loadBalancerName, int numberOfInstances) {
        List<String> targetGroupArns = this.getTargetGroupArns(loadBalancerName);

        List<TargetDescription> targets = new ArrayList();
        List<Instance> instances = getAllInstances(loadBalancerName);
        if (numberOfInstances <= 0 || instances.isEmpty()) {
            String error = "Number of instances can't be 0 or less " + loadBalancerName;
        }

        if (numberOfInstances > instances.size()) {
            numberOfInstances = instances.size();
        }

        for (int i = 0; i < numberOfInstances; i++) {
            TargetDescription target = new TargetDescription().withId(instances.get(i).getInstanceId());
            targets.add(target);
        }

        DeregisterTargetsRequest deregisterTargetsRequest = new DeregisterTargetsRequest().withTargetGroupArn(targetGroupArns.get(0))
                .withTargets(targets);
        applicationLoadBalancerDelegator.getAmazonApplicationLoadBalancing().deregisterTargets(deregisterTargetsRequest);

    }

    /**
     * De-register the given instances from the provided load balancer.
     *
     * @param loadBalancerName load balancer name
     * @param instances list of instance ids
     * @throws ResourceNotFoundException if the load balancer does not exist
     */
    @Override
    public void deregisterInstancesFromLoadBalancer(String loadBalancerName, List<String> instances) {
        List<String> targetGroupArns = this.getTargetGroupArns(loadBalancerName);
        List<TargetDescription> targets = new ArrayList<>();

        for (String instanceId : instances) {
            TargetDescription target = new TargetDescription().withId(instanceId);
            targets.add(target);
        }

        DeregisterTargetsRequest deregisterTargetsRequest = new DeregisterTargetsRequest().withTargetGroupArn(targetGroupArns.get(0))
                .withTargets(targets);
        applicationLoadBalancerDelegator.getAmazonApplicationLoadBalancing().deregisterTargets(deregisterTargetsRequest);

    }
    /**
     * Register the given instances to the specified load balancer.
     *
     * @param loadBalancerName load balancer name
     * @param instances list of instance ids
     * @throws ResourceNotFoundException if the load balancer does not exist
     */
    @Override
    public void registerInstancesFromLoadBalancer(String loadBalancerName, List<String> instances) {
        List<String> targetGroupArns = this.getTargetGroupArns(loadBalancerName);
        List<TargetDescription> targets = new ArrayList<>();

        for (String instanceId : instances) {
            TargetDescription target = new TargetDescription().withId(instanceId);
            targets.add(target);
        }

        RegisterTargetsRequest registerTargetsRequest = new RegisterTargetsRequest().withTargetGroupArn(targetGroupArns.get(0)).
                withTargets(targets);
        applicationLoadBalancerDelegator.getAmazonApplicationLoadBalancing().registerTargets(registerTargetsRequest);
    }

    /**
     * De-registers all instances from the given load balancer.
     *
     * @param loadBalancerName load balancer name
     * @throws ResourceNotFoundException if no instances are registered to the load balancer
     */
    @Override
    public void deregisterInstancesFromLoadBalancer(String loadBalancerName) {
        List<String> targetGroupArns = this.getTargetGroupArns(loadBalancerName);
        List<TargetDescription> targets = new ArrayList<>();
        List<Instance> instances = getAllInstances(loadBalancerName);

        for (Instance instance : instances) {
            TargetDescription target = new TargetDescription().withId(instance.getInstanceId());
            targets.add(target);
        }

        DeregisterTargetsRequest deregisterTargetsRequest = new DeregisterTargetsRequest().withTargetGroupArn(targetGroupArns.get(0))
                .withTargets(targets);
        applicationLoadBalancerDelegator.getAmazonApplicationLoadBalancing().deregisterTargets(deregisterTargetsRequest);
    }

    /**
     * Feature is currently disabled.
     *
     * Deletes the given policy from the specified load balancer.
     *
     * @param loadBalancerName load balancer name
     * @param policyName policy name
     * @throws ResourceNotFoundException if the load balancer does not exist
     */
    @Override
    public void deleteLoadBalancerPolicy(String loadBalancerName, String policyName) {
//        try {
//            applicationLoadBalancerDelegator.getAmazonApplicationLoadBalancing().
//            DeleteLoadBalancerPolicyRequest deleteLoadBalancerPolicyRequest = new DeleteLoadBalancerPolicyRequest(loadBalancerName, policyName);
//            return applicationLoadBalancerDelegator.getAmazonApplicationLoadBalancing().deleteLoadBalancerPolicy(deleteLoadBalancerPolicyRequest).toString();
//        } catch (Exception e){
//            throw new RuntimeException(e.getMessage());
//        }
        throw new UnSupportedFeatureException("Feature is not Implemented");
    }

    /**
     * Attaches the given subnets to the specified load balancer.
     *
     * @param loadBalancerName load balancer name
     * @param subnets subnet ids to add
     * @throws ResourceNotFoundException if the load balancer does not exist
     */
    @Override
    public void attachLoadBalancerToSubnets(String loadBalancerName, String... subnets) {
        try {
            Set<String> subnetCollection = this.getSubnets(loadBalancerName);
            subnetCollection.addAll(Arrays.asList(subnets));

            // applicationLoadBalancerDelegator.getAmazonApplicationLoadBalancing().
            SetSubnetsResult result = applicationLoadBalancerDelegator.getAmazonApplicationLoadBalancing().setSubnets(new SetSubnetsRequest()
                    .withLoadBalancerArn(this.getLoadBalancerArn(loadBalancerName))
                    .withSubnets(subnetCollection)
            );
            // AttachLoadBalancerToSubnetsRequest attachLoadBalancerToSubnetsRequest = new AttachLoadBalancerToSubnetsRequest().withLoadBalancerName(loadBalancerName).withSubnets(subnets);
        } catch (Exception e) {
            throw new ResourceNotFoundException(e.getMessage());
        }
    }

    /**
     * Gets the subnets attached to the specified load balancer.
     *
     * @param loadBalancerName load balancer name
     * @return set of subnet ids
     */
    private Set<String> getSubnets(String loadBalancerName) {
        return applicationLoadBalancerDelegator.getAmazonApplicationLoadBalancing().describeLoadBalancers(new DescribeLoadBalancersRequest().withNames(loadBalancerName))
                .getLoadBalancers()
                .parallelStream()
                .map(LoadBalancer::getAvailabilityZones)
                .flatMap(l -> l.stream())
                .collect(Collectors.toList())
                .stream()
                .map(AvailabilityZone::getSubnetId)
                .collect(Collectors.toSet());
    }

    /**
     * Gets the subnets attached to the specified load balancer in the given availability zones.
     *
     * @param loadBalancerName load balancer name
     * @param availabilityZone list of availability zones
     * @return set of subnet ids
     */
    private Set<String> getSubnetsInAZ(String loadBalancerName, List<String> availabilityZone) {
        return applicationLoadBalancerDelegator.getAmazonApplicationLoadBalancing().describeLoadBalancers(new DescribeLoadBalancersRequest().withNames(loadBalancerName))
                .getLoadBalancers()
                .parallelStream()
                .map(LoadBalancer::getAvailabilityZones)
                .flatMap(l -> l.stream())
                .collect(Collectors.toList())
                .stream()
                .filter(a -> availabilityZone.contains(a.getZoneName()))
                .map(AvailabilityZone::getSubnetId)
                .collect(Collectors.toSet());
    }

    /**
     * Detaches the given subnets to the specified load balancer.
     *
     * @param loadBalancerName load balancer name
     * @param subnets subnet ids to add
     * @throws ResourceNotFoundException if the load balancer does not exist
     */
    @Override
    public void detachLoadBalancerFromSubnets(String loadBalancerName, String... subnets) {
        try {
            Set<String> subnetCollection = this.getSubnets(loadBalancerName);
            subnetCollection.removeAll(Arrays.asList(subnets));

            SetSubnetsResult result = applicationLoadBalancerDelegator.getAmazonApplicationLoadBalancing().setSubnets(new SetSubnetsRequest()
                    .withLoadBalancerArn(this.getLoadBalancerArn(loadBalancerName))
                    .withSubnets(subnetCollection)
            );
        } catch (Exception e) {
            throw new ResourceNotFoundException(e.getMessage());
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
    public void disableAvailabilityZonesForLoadBalancer(String loadBalancerName, String... availabilityZones) {
        try {
            Set<String> subnets = this.getSubnets(loadBalancerName);
            Set<String> availabilityZonesSubnets = this.getSubnetsInAZ(loadBalancerName, Arrays.asList(availabilityZones));

            subnets.removeAll(availabilityZonesSubnets);

            SetSubnetsResult result = applicationLoadBalancerDelegator.getAmazonApplicationLoadBalancing().setSubnets(new SetSubnetsRequest()
                    .withLoadBalancerArn(this.getLoadBalancerArn(loadBalancerName))
                    .withSubnets(subnets));
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * Feature is currently disabled.
     *
     * Enables the given availability zones on the specified load balancer.
     *
     * @param loadBalancerName load balancer name
     * @param availabilityZones availability zone ids
     * @throws ResourceNotFoundException if the load balancer does not exist
     */
    @Override
    public void enableAvailabilityZonesForLoadBalancer(String loadBalancerName, String... availabilityZones) {
        throw new UnSupportedFeatureException("Feature is not Implemented");
    }

    /**
     * Checks if the given load balancer exists.
     *
     * @param loadBalancerName load balancer name
     * @return true if the load balancer no longer exists
     */
    @Override
    public Boolean isDeleted(String loadBalancerName) {
        try {
            if (describeLoadBalancers(loadBalancerName).getLoadBalancers()
                    .stream()
                    .filter(l -> l.getLoadBalancerName().equalsIgnoreCase(loadBalancerName))
                    .findFirst().get() != null) {
                return false;
            } else {
                return true;
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * Gets the instances attached to the current load balancer
     *
     * @param loadBalancerName load balancer name
     * @return list of AWS instances
     */
    @Override
    public List<Instance> getLoadBalancerInstances(String loadBalancerName) {
        try {
            return getAllInstances(loadBalancerName);
        } catch (Exception e) {
            throw new ResourceNotFoundException(e.getMessage());
        }
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
    public void changeLoadBalancerName(String loadBalancerName, String newLoadBalancerName) {
        throw new UnSupportedFeatureException("Feature is Not Implemented");
    }

    /**
     * Deprecated.
     * Replaced with describeLoadBalancers().
     */
    @Deprecated
    private DescribeLoadBalancersResult describeLoadBalancersResult(String loadBalancerName) {
        List<String> loadBalancers = new ArrayList<String>();
        loadBalancers.add(loadBalancerName);
        DescribeLoadBalancersRequest describeLoadBalancersRequest = new DescribeLoadBalancersRequest().withNames(loadBalancers);
        return applicationLoadBalancerDelegator.getAmazonApplicationLoadBalancing().describeLoadBalancers(describeLoadBalancersRequest);
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
        DescribeLoadBalancersRequest describeLoadBalancersRequest = new DescribeLoadBalancersRequest().withNames(loadBalancers);
        return applicationLoadBalancerDelegator.getAmazonApplicationLoadBalancing().describeLoadBalancers(describeLoadBalancersRequest);
    }

    /**
     * Gets all in service instances attached to the provided load balancer.
     *
     * @param albName load balancer name
     * @return list of instance ids
     */
    @Override
    public List<String> getInServiceInstances(String albName) {
        return getServiceInstances(albName, "healthy");
    }

    /**
     * Gets all out of service instances attached to the provided load balancer.
     *
     * @param albName load balancer name
     * @return list of instance ids
     */
    @Override
    public List<String> getOutOfServiceInstances(String albName) {
        return getServiceInstances(albName, "unhealthy");
    }

    /**
     * Get all instances, regardless of service status.
     *
     * @param albName load balancer name
     * @return list of AWS Instances
     */
    private List<Instance> getAllInstances(String albName) {
        List instances = this.getInServiceInstances(albName);
        instances.addAll(this.getOutOfServiceInstances(albName));
        return instances;
    }

    /**
     * Helper function for getting In or Out Service instances.
     *
     * @param albName load balancer name.
     * @param state state to match
     * @return list of instance ids
     */
    private List<String> getServiceInstances(String albName, String state) {
        List<String> targetGroupArns = this.getTargetGroupArns(albName);
        DescribeTargetHealthRequest healthRequest = new DescribeTargetHealthRequest().withTargetGroupArn(targetGroupArns.get(0));

        List<String> instances = applicationLoadBalancerDelegator.getAmazonApplicationLoadBalancing().describeTargetHealth(healthRequest)
                .getTargetHealthDescriptions()
                .parallelStream()
                .filter(t -> t.getTargetHealth().getState().equalsIgnoreCase(state))
                .map(TargetHealthDescription::getTarget)
                .collect(Collectors.toList())
                .parallelStream()
                .map(TargetDescription::getId)
                .collect(Collectors.toList());

        Collections.shuffle(instances);

        return instances;
    }

    /**
     * Get all target groups for the given load balancer.
     *
     * @param albName load balancer name
     * @return list of target group arns
     */
    private List<String> getTargetGroupArns(String albName) {
        return applicationLoadBalancerDelegator.getAmazonApplicationLoadBalancing().describeTargetGroups(new DescribeTargetGroupsRequest().withNames(albName))
                .getTargetGroups()
                .parallelStream()
                .map(TargetGroup::getTargetGroupArn)
                .collect(Collectors.toList());
    }

    /**
     * Duplicate function of deleteSecurityGroups().
     */
    @Deprecated
    public void deleteSecurityGroup(String albName, String securityGroup) {
        List<String> securityGroups = new ArrayList<>();
        securityGroups.addAll(this.getSecurityGroups(albName));
        try {
            if (securityGroup.contains(securityGroup)) {
                securityGroups.remove(securityGroup);
                SetSecurityGroupsResult sgResult = applicationLoadBalancerDelegator.getAmazonApplicationLoadBalancing().setSecurityGroups(new SetSecurityGroupsRequest()
                        .withLoadBalancerArn(this.getLoadBalancerArn(albName))
                        .withSecurityGroups(securityGroups));
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }

    /**
     * Duplicate function of addSecurityGroups().
     */
    @Deprecated
    public void addSecurityGroup(String albName, String securityGroup) {
        List<String> securityGroups = new ArrayList<>();
        securityGroups.addAll(this.getSecurityGroups(albName));
        try {
            if (!securityGroups.contains(securityGroup)) {
                securityGroups.add(securityGroup);

                applicationLoadBalancerDelegator.getAmazonApplicationLoadBalancing().setSecurityGroups(new SetSecurityGroupsRequest()
                        .withLoadBalancerArn(this.getLoadBalancerArn(albName))
                        .withSecurityGroups(securityGroups));
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage());
        }

    }

    /**
     * Gets all attached security groups to the given load balancer.
     *
     * @param albName load balancer name
     * @return list of security group ids
     */
    @Override
    public List<String> getSecurityGroups(String albName) {
        return applicationLoadBalancerDelegator.getAmazonApplicationLoadBalancing().describeLoadBalancers(new DescribeLoadBalancersRequest()
                .withLoadBalancerArns(this.getLoadBalancerArn(albName)))
                .getLoadBalancers()
                .parallelStream()
                .map(l -> l.getSecurityGroups())
                .findFirst()
                .get();
    }

    /**
     * Detach the given security groups from the specified load balancer.
     *
     * @param albName load balancer name
     * @param securityGroups security group ids
     */
    @Override
    public void deleteSecurityGroups(String albName, String... securityGroups) {
        List<String> existingSecurityGroups = new ArrayList<>();
        existingSecurityGroups.addAll(this.getSecurityGroups(albName));

        try {
            if (existingSecurityGroups.containsAll(Arrays.asList(securityGroups))) {
                existingSecurityGroups.removeAll(Arrays.asList(securityGroups));
                applicationLoadBalancerDelegator.getAmazonApplicationLoadBalancing().setSecurityGroups(new SetSecurityGroupsRequest()
                        .withLoadBalancerArn(this.getLoadBalancerArn(albName))
                        .withSecurityGroups(existingSecurityGroups));
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }

    /**
     * Attach the given security groups to the specified load balancer.
     *
     * @param albName load balancer name
     * @param securityGroups security group ids
     */
    @Override
    public void addSecurityGroups(String albName, String... securityGroups) {
        List<String> existingSecurityGroups = new ArrayList<>();
        existingSecurityGroups.addAll(this.getSecurityGroups(albName));

        try {
            if (!existingSecurityGroups.containsAll(Arrays.asList(securityGroups))) {
                existingSecurityGroups.addAll(Arrays.asList(securityGroups));
                applicationLoadBalancerDelegator.getAmazonApplicationLoadBalancing().setSecurityGroups(new SetSecurityGroupsRequest()
                        .withLoadBalancerArn(this.getLoadBalancerArn(albName))
                        .withSecurityGroups(existingSecurityGroups));
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage());
        }
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
        TargetGroup targetGroup = this.validateAndGetExistingTargetGroup(loadBalancerName);
        int pingPort = targetGroup.getPort();
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
        TargetGroup targetGroup = this.validateAndGetExistingTargetGroup(loadBalancerName);
        int pingPort = Integer.parseInt(targetGroup.getHealthCheckPort());
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
    private void validateScrambledPingPort(int pingPort) {
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
    public void updateLoadbalancerHealthCheck(String loadBalancerName, HealthCheckTarget healthCheckTarget) {
        TargetGroup targetGroup = validateAndGetExistingTargetGroup(loadBalancerName);// remove this if this method becomes private

        if (StringUtils.isNullOrEmpty(healthCheckTarget.getPingPath()))
            healthCheckTarget.setPingPath(targetGroup.getHealthCheckPath());
        if (healthCheckTarget.getPingProtocol() == null)
            healthCheckTarget.setPingProtocol(HealthCheckUtils.toPingProtocol(targetGroup.getHealthCheckProtocol()));
        if (healthCheckTarget.getPingPort() == null) healthCheckTarget.setPingPort(targetGroup.getPort());


        ModifyTargetGroupRequest modifyTargetGroupRequest = new ModifyTargetGroupRequest().withTargetGroupArn(targetGroup.getTargetGroupArn())
                .withHealthCheckPort(healthCheckTarget.getPingPort().toString())
                .withHealthCheckPath(healthCheckTarget.getPingPath())
                .withHealthCheckProtocol(healthCheckTarget.getPingProtocol().toString());

        applicationLoadBalancerDelegator.getAmazonApplicationLoadBalancing().modifyTargetGroup(modifyTargetGroupRequest);
    }

    /**
     * Confirms that a load balancer with the given name exists and gets its corresponding target group information.
     *
     * @param loadBalancerName load balancer name
     * @return AWS TargetGroup
     */
    private TargetGroup validateAndGetExistingTargetGroup(String loadBalancerName) {
        String targetGroupArn = this.getTargetGroupArns(loadBalancerName).get(0);
        DescribeTargetGroupsRequest targetGroupRequest = new DescribeTargetGroupsRequest().withTargetGroupArns(targetGroupArn);

        TargetGroup targetGroup = applicationLoadBalancerDelegator.getAmazonApplicationLoadBalancing().describeTargetGroups(targetGroupRequest).getTargetGroups().get(0);

        if (targetGroup.getHealthCheckPort().isEmpty())
            throw new IllegalStateException("No load balancers exists for " + loadBalancerName);

        return targetGroup;
    }

    /**
     * Gets the subnets attached to the specified load balancer.
     *
     * @param loadBalancerName load balancer name
     * @return list of subnet ids
     */
    @Override
    public List<String> getLoadBalancerSubnets(String loadBalancerName) {
        return new ArrayList<>(getSubnets(loadBalancerName));
    }

    /**
     * Gets all the Application or Network Load Balancers associated with the account.
     *
     * @return Map of (load balancer name, load balancer type)
     */
    @Override
    public Map<String, String> getLoadBalancerNames() throws ResourceNotFoundException {
        DescribeLoadBalancersRequest request = new DescribeLoadBalancersRequest();
        List<String> loadBalancers = applicationLoadBalancerDelegator.getAmazonApplicationLoadBalancing().describeLoadBalancers(request).getLoadBalancers()
                .stream().map(x -> (x.getLoadBalancerName() + "\t" + x.getType())).collect(Collectors.toList());
        Map<String, String> map = new HashMap<>();
        for (String lb : loadBalancers) {
            String[] arr = lb.split("\t");
            if (arr[1].equalsIgnoreCase(LoadBalancerTypeEnum.Application.toString())) {
                map.put(arr[0].toLowerCase(), "ALB");
            } else {
                map.put(arr[0].toLowerCase(), "NLB");
            }
        }
        return map;
    }
}
