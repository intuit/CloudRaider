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

package com.intuit.cloudraider.cucumber.interfaces;

import com.intuit.cloudraider.cucumber.model.ExecutionStateCache;

/**
 * Interface for shared method calls between InstanceFailureStepDefinitions and SSMStepDefinitions.
 */
public interface EC2StepFunctions {

    /**
     * Sets the shared execution cache.
     *
     * @param executionStateCache executionStateCache
     */
    void setExecutionStateCache(ExecutionStateCache executionStateCache);

    /**
     * Terminate the given process on the instance.
     *
     * @param processName process name
     * @param instanceID instance id
     * @param instanceIP instance private ip
     */
    void terminateProcessGivenInstance(String processName, String instanceID, String instanceIP);

    /**
     * Spike the given number of cores on the instance.
     *
     * @param cores number of cores
     * @param instanceID instance id
     * @param instanceIP instance private ip
     */
    void spikeCPUGivenInstance(int cores, String instanceID, String instanceIP);

    /**
     * Add "size" GB to the specified volume.
     *
     * @param volumeType name of volume
     * @param size size of disk
     * @param instanceID instance id
     * @param instanceIP instance private ip
     */
    void diskFullGivenInstance(String volumeType, int size, String instanceID, String instanceIP);

    /**
     * Block traffic on the given port number.
     *
     * @param portNum port number to block
     * @param instanceID instance id
     * @param instanceIP instance private ip
     */
    void blockPortGivenInstance(int portNum, String instanceID, String instanceIP);

    /**
     * Injects network latency within the bounds on the default interface.
     *
     * @param lowerBound lower bound for latency
     * @param upperBound upper bound for latency
     * @param instanceID instance id
     * @param instanceIP instance private ip
     */
    void injectNetworkLatencyGivenInstance(int lowerBound, int upperBound, String instanceID, String instanceIP);
}
