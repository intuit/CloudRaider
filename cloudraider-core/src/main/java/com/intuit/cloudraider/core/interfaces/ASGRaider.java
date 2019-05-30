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

import com.amazonaws.services.autoscaling.model.AutoScalingGroup;
import com.amazonaws.services.autoscaling.model.DetachInstancesResult;
import com.amazonaws.services.autoscaling.model.TerminateInstanceInAutoScalingGroupResult;

import java.util.List;

/**
 * AWS Auto Scaling Group functionality.
 *
 */
public interface ASGRaider {

    /**
     * Get all the instances that are part of the given auto scaling group.
     *
     * @param asGroupName auto scaling group name
     * @return list of instance ids
     */
    public List getAllInstanceInASG(String asGroupName);

    /**
     * Delete the given auto scaling group.
     *
     * @param asGroupName auto scaling group name
     * @return string responses (UNUSED)
     */
    public String deleteAutoScalingGroup(String asGroupName);

    /**
     * Remove the instance with the given instance id from the auto scaling group.
     *
     * @param instanceId   instance id
     * @param asgGroupName auto scaling group name
     * @return DetachInstancesResult for the given instance
     */
    public DetachInstancesResult detachInstanceFromASG(String instanceId, String asgGroupName);

    /**
     * Checks if the given auto scaling group name describes an existing ASG.
     *
     * @param asGroupName auto scaling group name
     * @return true if the auto scaling group exists; false otherwise
     */
    public boolean isValidAutoScalingGroup(String asGroupName);

    /**
     * Terminate the instance with the given instance id.
     *
     * @param instanceId instance id
     * @return TerminateInstanceInAutoScalingGroupResult for the given instance
     */
    public TerminateInstanceInAutoScalingGroupResult terminateInstanceINASG(String instanceId);

    /**
     * Deprecated due to misleading name. Replacement is named getAutoScalingGroups()
     *
     * @param var1 the var 1
     * @param var2 the var 2
     * @return the all instances in asg by stack name
     */
    @Deprecated
    List<AutoScalingGroup> getAllInstancesInASGByStackName(String var1, String var2);

    /**
     * Gets all auto scaling groups that match the given key and value tag. To get all auto scaling groups, set both the
     * key and the value to the empty string: "".
     *
     * @param key   key in tag
     * @param value value in tag
     * @return list of matching Auto Scaling Groups
     */
    List<AutoScalingGroup> getAutoScalingGroups(String key, String value);
}
