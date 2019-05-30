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

import com.amazonaws.services.route53.model.*;
import com.intuit.cloudraider.commons.Route53Delegator;
import com.intuit.cloudraider.core.interfaces.Route53Raider;
import com.intuit.cloudraider.model.HealthCheckStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


/**
 * Route 53 functionality.
 * <p>
  */
@Component(value="r53RaiderBean")
public class Route53RaiderImpl implements Route53Raider {

    /**
     * The Logger.
     */
    Logger logger = LoggerFactory.getLogger(this.getClass());
    
    @Autowired
    private Route53Delegator route53Delegator;

    /**
     * Instantiates a new Route 53 raider.
     */
    public Route53RaiderImpl() {

    }

    /**
     * Get all health checks.
     *
     * @return list of health checks
     */
    @Override
    public List<HealthCheck> listHealthCheckers() {
        return route53Delegator.getAmazonRoute53().listHealthChecks().getHealthChecks();
    }

    /**
     * Get the status of the given health check.
     *
     * @param healthCheckerId health check id
     * @return list of HealthCheckObservation
     */
    @Override
    public List<HealthCheckObservation> getHealthCheckerStatus(String healthCheckerId) {
        GetHealthCheckStatusRequest getHealthCheckStatusRequest = new GetHealthCheckStatusRequest();
        getHealthCheckStatusRequest.withHealthCheckId(healthCheckerId);
        return route53Delegator.getAmazonRoute53().getHealthCheckStatus(getHealthCheckStatusRequest).getHealthCheckObservations();
    }

    /**
     * Checks the health check status given id.
     *
     * @param healthCheckerId health checker id
     * @return SUCCESS if all health checks pass; FAILURE if any health check has failed
     */
    @Override
    public HealthCheckStatus getSimpleHealthCheckerStatus(String healthCheckerId) {

        GetHealthCheckStatusRequest getHealthCheckStatusRequest = new GetHealthCheckStatusRequest();
        getHealthCheckStatusRequest.withHealthCheckId(healthCheckerId);
        List<HealthCheckObservation> healthCheckObservations = route53Delegator.getAmazonRoute53().getHealthCheckStatus(getHealthCheckStatusRequest).getHealthCheckObservations();

        for (HealthCheckObservation obs : healthCheckObservations) {
            if (obs.getStatusReport().getStatus().contains("Failure")) {
                return HealthCheckStatus.FAILURE;
            }
        }

        return HealthCheckStatus.SUCCESS;
    }

    /**
     * Update the traffic policy with the new version.
     *
     * @param trafficPolicyName traffic policy name
     * @param version           version
     */
    @Override
    public void updateTrafficPolicy(String trafficPolicyName, int version) {
        // UpdateTrafficPolicyInstanceRequest updateTrafficPolicyInstanceRequest = new UpdateTrafficPolicyInstanceRequest().withTrafficPolicyId(trafficPolicyId)
        //  route53Delegator.getAmazonRoute53().updateTrafficPolicyInstance()
        String r53TrafficPolicyId = getR53TrafficPolicyId(trafficPolicyName);
        String r53PolicyId = getR53PolicyId(r53TrafficPolicyId);

        UpdateTrafficPolicyInstanceRequest updateTrafficPolicyInstanceRequest = new UpdateTrafficPolicyInstanceRequest()
                .withId(r53PolicyId)
                .withTrafficPolicyId(r53TrafficPolicyId)
                .withTTL(20l)
                .withTrafficPolicyVersion(version);

        route53Delegator.getAmazonRoute53().updateTrafficPolicyInstance(updateTrafficPolicyInstanceRequest);

    }

    /**
     * Get the version number of the given traffic policy.
     *
     * @param trafficPolicyName traffic policy name
     * @return version number
     */
    @Override
    public int getTrafficPolicyVersion(String trafficPolicyName) {
        // UpdateTrafficPolicyInstanceRequest updateTrafficPolicyInstanceRequest = new UpdateTrafficPolicyInstanceRequest().withTrafficPolicyId(trafficPolicyId)
        //  route53Delegator.getAmazonRoute53().updateTrafficPolicyInstance()
        String r53PolicyId = getR53TrafficPolicyId(trafficPolicyName);
        int policyVersion = route53Delegator.getAmazonRoute53().listTrafficPolicyInstances()
                .getTrafficPolicyInstances()
                .parallelStream()
                .filter(tpi -> tpi.getTrafficPolicyId().equalsIgnoreCase(r53PolicyId))
                .map(TrafficPolicyInstance::getTrafficPolicyVersion)
                .collect(Collectors.toList())
                .stream()
                .findFirst()
                .get();

        return policyVersion;

    }

    /**
     * Get the id of the traffjc policy matching the provided name.
     *
     * @param trafficPolicyName traffic policy name
     * @return policy id
     */
    private String getR53TrafficPolicyId(String trafficPolicyName) {
        String id = route53Delegator.getAmazonRoute53().listTrafficPolicies()
                .getTrafficPolicySummaries()
                .parallelStream()
                .filter(tps -> tps.getName().equalsIgnoreCase(trafficPolicyName))
                .map(TrafficPolicySummary::getId)
                .collect(Collectors.toList())
                .stream()
                .findFirst()
                .get();

        return id;
    }

    /**
     * Returns the provided traffic policy instance id given a traffic policy id.
     *
     * @param policyId policy id
     * @return traffic policy instance id
     */
    public String getR53PolicyId(String policyId) {
        String trafficPolicyId = route53Delegator.getAmazonRoute53().listTrafficPolicyInstances()
                .getTrafficPolicyInstances()
                .parallelStream()
                .filter(tpi -> tpi.getTrafficPolicyId().equalsIgnoreCase(policyId))
                .map(TrafficPolicyInstance::getId)
                .collect(Collectors.toList())
                .stream()
                .findFirst()
                .get();

        return trafficPolicyId;
    }

    /**
     * Update the health check with the provided regions.
     *
     * @param healthCheckId health check id
     * @param regions list of health check regions
     */
    @Override
    public void updateHealthCheckRegions(String healthCheckId, List<String> regions) {
        UpdateHealthCheckRequest updateHealthCheckRequest = new UpdateHealthCheckRequest()
                .withHealthCheckId(healthCheckId)
                .withRegions(regions);
        route53Delegator.getAmazonRoute53().updateHealthCheck(updateHealthCheckRequest);

    }

    /**
     * Gets health check regions associated with the given health check.
     *
     * @param healthCheckId health check id
     * @return list of health check regions
     */
    @Override
    public List<String> getHealthCheckRegions(String healthCheckId) {

        return route53Delegator.getAmazonRoute53().getHealthCheck(new GetHealthCheckRequest().withHealthCheckId(healthCheckId))
                .getHealthCheck()
                .getHealthCheckConfig()
                .getRegions();
    }

    /**
     * Update the provided health check to use default regions.
     *
     * @param healthCheckId health check id
     */
    @Override
    public void updateHealthCheckToDefaultRegions(String healthCheckId) {

        List<String> defaultRegions = Arrays.asList("sa-east-1", "us-west-1", "us-west-2", "ap-northeast-1", "ap-southeast-1", "eu-west-1", "us-east-1", "ap-southeast-2");
        this.updateHealthCheckRegions(healthCheckId, defaultRegions);

    }
}
