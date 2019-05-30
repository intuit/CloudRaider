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

import com.amazonaws.services.route53.model.HealthCheck;
import com.amazonaws.services.route53.model.HealthCheckObservation;
import com.intuit.cloudraider.model.HealthCheckStatus;

import java.util.List;

/**
 * Route 53 functionality.
 * <p>
  */
public interface Route53Raider {

    /**
     * Get all health checks.
     *
     * @return list of health checks
     */
    public List<HealthCheck> listHealthCheckers();

    /**
     * Get the status of the given health check.
     *
     * @param healthCheckerId health check id
     * @return list of HealthCheckObservation
     */
    public List<HealthCheckObservation> getHealthCheckerStatus(String healthCheckerId);

    /**
     * Checks the health check status given id.
     *
     * @param healthCheckerId health checker id
     * @return SUCCESS if all health checks pass; FAILURE if any health check has failed
     */
    public HealthCheckStatus getSimpleHealthCheckerStatus(String healthCheckerId);

    /**
     * Update the traffic policy with the new version.
     *
     * @param trafficPolicyName traffic policy name
     * @param version           version
     */
    public void updateTrafficPolicy(String trafficPolicyName, int version);

    /**
     * Get the version number of the given traffic policy.
     *
     * @param trafficPolicyName traffic policy name
     * @return version number
     */
    public int getTrafficPolicyVersion(String trafficPolicyName);

    /**
     * Update the health check with the provided regions.
     *
     * @param healthCheckId health check id
     * @param regions       list of health check regions
     */
    public void updateHealthCheckRegions(String healthCheckId, List<String> regions);

    /**
     * Update the provided health check to use default regions.
     *
     * @param healthCheckId health check id
     */
    public void updateHealthCheckToDefaultRegions(String healthCheckId);

    /**
     * Gets health check regions associated with the given health check.
     *
     * @param healthCheckId health check id
     * @return list of health check regions
     */
    public List<String> getHealthCheckRegions(String healthCheckId);
}
