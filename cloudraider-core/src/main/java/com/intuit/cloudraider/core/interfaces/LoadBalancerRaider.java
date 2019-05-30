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

package com.intuit.cloudraider.core.interfaces;

import com.amazonaws.services.elasticloadbalancing.model.Instance;
import com.intuit.cloudraider.exceptions.ResourceNotFoundException;
import com.intuit.cloudraider.exceptions.UnSupportedFeatureException;
import com.intuit.cloudraider.model.HealthCheckTarget;

import java.util.List;
import java.util.Map;

/**
 * AWS Load Balancer functionality.
 * <p>
  */
public interface LoadBalancerRaider {

    /**
     * Returns health check to original state.
     * <p>
     * Note (11/2017): does not work if force fail method has not yet been called (health check ping port is not divisible by 7),
     * will throw illegal state exception.
     *
     * @param loadBalancerName load balancer name
     * @throws ResourceNotFoundException the resource not found exception
     */
    public void undoForceFailLoadbalancerHealthCheck(final String loadBalancerName) throws ResourceNotFoundException;

    /**
     * Simulates a health check failure.
     * <p>
     * Note (11/2017): does not work if the health check ping port is divisible by 7,
     * will throw illegal state exception.
     *
     * @param loadBalancerName load balancer name
     * @throws ResourceNotFoundException the resource not found exception
     */
    public void forceFailLoadbalancerHealthCheck(final String loadBalancerName) throws ResourceNotFoundException;

    /**
     * Update the health check settings for the given load balancer.
     *
     * @param loadBalancerName  load balancer name
     * @param healthCheckTarget health check target
     * @throws ResourceNotFoundException the resource not found exception
     */
    public void updateLoadbalancerHealthCheck(String loadBalancerName, HealthCheckTarget healthCheckTarget) throws ResourceNotFoundException;

    /**
     * Deletes the load balancer with the given name.
     *
     * @param loadBalancerName load balancer name
     * @throws ResourceNotFoundException if the load balancer does not exist.
     */
    void deleteLoadBalancer(String loadBalancerName) throws ResourceNotFoundException;

    /**
     * Removes the specified load balancers from the given load balancer.
     *
     * @param loadBalancerName  load balancer name
     * @param loadBalancerPorts client port numbers of the listeners
     * @throws ResourceNotFoundException the resource not found exception
     */
    void deleteLoadBalancerListeners(String loadBalancerName, Integer... loadBalancerPorts) throws ResourceNotFoundException;

    /**
     * Update the port number for one of the given load balancer's listeners.
     *
     * @param loadBalancerName load balancer name
     * @param oldPort          old client port for listener
     * @param newPort          new client port for listener
     * @throws ResourceNotFoundException the resource not found exception
     */
    void updateLoadbalancerListner(String loadBalancerName, int oldPort,int newPort) throws ResourceNotFoundException;

    /**
     * De-registers the number of instances from the given load balancer.
     *
     * @param loadBalancerName  load balancer name
     * @param numberOfInstances number of instances
     * @throws ResourceNotFoundException if no instances are registered to the load balancer
     */
    void deregisterInstancesFromLoadBalancer(String loadBalancerName, int numberOfInstances) throws ResourceNotFoundException;

    /**
     * De-register the given instances from the provided load balancer.
     *
     * @param loadBalancerName load balancer name
     * @param instances        list of instance ids
     * @throws ResourceNotFoundException if the load balancer does not exist
     */
    void deregisterInstancesFromLoadBalancer(String loadBalancerName, List<String> instances) throws ResourceNotFoundException;

    /**
     * De-registers all instances from the given load balancer.
     *
     * @param loadBalancerName load balancer name
     * @throws ResourceNotFoundException if no instances are registered to the load balancer
     */
    void deregisterInstancesFromLoadBalancer(String loadBalancerName )throws ResourceNotFoundException;

    /**
     * Register the given instances to the specified load balancer.
     *
     * @param loadBalancerName load balancer name
     * @param instances        list of instance ids
     * @throws ResourceNotFoundException if the load balancer does not exist
     */
    void registerInstancesFromLoadBalancer(String loadBalancerName, List<String> instances) throws ResourceNotFoundException;

    /**
     * Deletes the given policy from the specified load balancer.
     *
     * @param loadBalancerName load balancer name
     * @param policyName       policy name
     * @throws ResourceNotFoundException if the load balancer does not exist
     */
    void deleteLoadBalancerPolicy(String loadBalancerName, String policyName) throws ResourceNotFoundException;

    /**
     * Attaches the given subnets to the specified load balancer.
     *
     * @param loadBalancerName load balancer name
     * @param subnets          subnet ids to add
     * @throws ResourceNotFoundException if the load balancer does not exist
     */
    void attachLoadBalancerToSubnets(String loadBalancerName, String... subnets) throws ResourceNotFoundException;

    /**
     * Detaches the given subnets to the specified load balancer.
     *
     * @param loadBalancerName load balancer name
     * @param subnets          subnet ids to add
     * @throws ResourceNotFoundException if the load balancer does not exist
     */
    void detachLoadBalancerFromSubnets(String loadBalancerName, String... subnets) throws ResourceNotFoundException;

    /**
     * Disables the given availability zones on the specified load balancer.
     *
     * @param loadBalancerName  load balancer name
     * @param availabilityZones availability zone ids
     * @throws ResourceNotFoundException if the load balancer does not exist
     */
    void disableAvailabilityZonesForLoadBalancer(String loadBalancerName, String... availabilityZones) throws ResourceNotFoundException;

    /**
     * Enables the given availability zones on the specified load balancer.
     *
     * @param loadBalancerName  load balancer name
     * @param availabilityZones availability zone ids
     * @throws ResourceNotFoundException if the load balancer does not exist
     */
    void enableAvailabilityZonesForLoadBalancer(String loadBalancerName, String... availabilityZones) throws ResourceNotFoundException;

    /**
     * Checks if the given load balancer exists.
     *
     * @param loadBalancerName load balancer name
     * @return true if the load balancer no longer exists
     * @throws ResourceNotFoundException the resource not found exception
     */
    Boolean isDeleted(String loadBalancerName) throws ResourceNotFoundException;

    /**
     * Gets the instances attached to the current load balancer
     *
     * @param loadBalancerName load balancer name
     * @return list of AWS instances
     * @throws ResourceNotFoundException the resource not found exception
     */
    List<Instance> getLoadBalancerInstances(String loadBalancerName) throws ResourceNotFoundException;

    /**
     * Feature is currently disabled.
     * <p>
     * Update the load balancer name.
     *
     * @param loadBalancerName    old name
     * @param newLoadBalancerName new name
     * @throws ResourceNotFoundException the resource not found exception
     */
    void changeLoadBalancerName(String loadBalancerName, String newLoadBalancerName) throws ResourceNotFoundException;

    /**
     * Gets all in service instances attached to the provided load balancer.
     *
     * @param elbName load balancer name
     * @return list of instance ids
     * @throws ResourceNotFoundException the resource not found exception
     */
    public List<String> getInServiceInstances(String elbName) throws ResourceNotFoundException;

    /**
     * Gets all out of service instances attached to the provided load balancer.
     *
     * @param elbName load balancer name
     * @return list of instance ids
     * @throws ResourceNotFoundException the resource not found exception
     */
    public List<String> getOutOfServiceInstances(String elbName) throws ResourceNotFoundException;

    /**
     * Duplicate function of deleteSecurityGroups().
     *
     * @param elbName       the elb name
     * @param securityGroup the security group
     * @throws ResourceNotFoundException the resource not found exception
     */
    @Deprecated
    public void deleteSecurityGroup(String elbName, String securityGroup) throws ResourceNotFoundException;

    /**
     * Duplicate function of addSecurityGroups().
     *
     * @param elbName       the elb name
     * @param securityGroup the security group
     * @throws ResourceNotFoundException the resource not found exception
     */
    @Deprecated
    public void addSecurityGroup(String elbName, String securityGroup) throws ResourceNotFoundException;

    /**
     * Gets all attached security groups to the given load balancer.
     *
     * @param elbName load balancer name
     * @return list of security group ids
     * @throws ResourceNotFoundException the resource not found exception
     */
    public List<String> getSecurityGroups(String elbName) throws ResourceNotFoundException;

    /**
     * Detach the given security groups from the specified load balancer.
     *
     * @param elbName        load balancer name
     * @param securityGroups security group ids
     * @throws ResourceNotFoundException the resource not found exception
     */
    public void deleteSecurityGroups(String elbName, String... securityGroups) throws ResourceNotFoundException;

    /**
     * Attach the given security groups to the specified load balancer.
     *
     * @param elbName        load balancer name
     * @param securityGroups security group ids
     * @throws ResourceNotFoundException the resource not found exception
     */
    public void addSecurityGroups(String elbName, String... securityGroups) throws ResourceNotFoundException;

    /**
     * Gets the subnets attached to the specified load balancer.
     *
     * @param loadBalancerName load balancer name
     * @return list of subnet ids
     * @throws ResourceNotFoundException the resource not found exception
     */
    List<String> getLoadBalancerSubnets(String loadBalancerName) throws ResourceNotFoundException;

    /**
     * Gets all the Load Balancers associated with the account.
     *
     * @return Map of (load balancer name, load balancer type)
     * @throws ResourceNotFoundException the resource not found exception
     */
    Map<String, String> getLoadBalancerNames() throws ResourceNotFoundException;
}
