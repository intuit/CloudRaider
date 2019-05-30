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

import com.amazonaws.services.autoscaling.model.*;
import com.intuit.cloudraider.commons.ASGDelegator;
import com.intuit.cloudraider.core.interfaces.ASGRaider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * AWS Auto Scaling Group functionality.
 * <p>
  */
@Component(value="asgRaiderBean")
public class ASGRaiderImpl implements ASGRaider {


    @Autowired
    private ASGDelegator asgDelegator;

    /**
     * The Logger.
     */
    Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * Instantiates a new Asg raider.
     */
    public ASGRaiderImpl() {

    }

    /**
     * Get all the instances that are part of the given auto scaling group.
     *
     * @param asGroupName auto scaling group name
     * @return list of instance ids
     */
    @Override
    public List getAllInstanceInASG(String asGroupName) {
        DescribeAutoScalingGroupsResult describeAutoScalingGroupsResult = asgDelegator.getAsgClient()
                .describeAutoScalingGroups(new DescribeAutoScalingGroupsRequest().withAutoScalingGroupNames(asGroupName));
        List<String> instanceIds = new ArrayList<>();
        if (describeAutoScalingGroupsResult.getAutoScalingGroups().get(0).getInstances() != null) {
            for (com.amazonaws.services.autoscaling.model.Instance instance : describeAutoScalingGroupsResult.getAutoScalingGroups().get(0).getInstances()) {
                instanceIds.add(instance.getInstanceId());
            }

        }
        return instanceIds;
    }

    /**
     * Delete the given auto scaling group.
     *
     * @param asGroupName auto scaling group name
     * @return string responses (UNUSED)
     */
    @Override
    public String deleteAutoScalingGroup(String asGroupName) {
        DeleteAutoScalingGroupRequest request = new DeleteAutoScalingGroupRequest()
                .withAutoScalingGroupName(asGroupName)
                .withForceDelete(true);
        DeleteAutoScalingGroupResult response = asgDelegator.getAsgClient().deleteAutoScalingGroup(request);

        return "AutoScaling Group deleted";
    }

    /**
     * Remove the instance with the given instance id from the auto scaling group.
     *
     * @param instanceId   instance id
     * @param asgGroupName auto scaling group name
     * @return DetachInstancesResult for the given instance
     */
    @Override
    public DetachInstancesResult detachInstanceFromASG(String instanceId, String asgGroupName) {
        DetachInstancesRequest request = new DetachInstancesRequest()
                .withAutoScalingGroupName(asgGroupName)
                .withInstanceIds(instanceId)
                .withShouldDecrementDesiredCapacity(false);
        DetachInstancesResult response = asgDelegator.getAsgClient().detachInstances(request);

        return response;
    }

    /**
     * Terminate the instance with the given instance id.
     *
     * @param instanceId instance id
     * @return TerminateInstanceInAutoScalingGroupResult for the given instance
     */
    @Override
    public TerminateInstanceInAutoScalingGroupResult terminateInstanceINASG(String instanceId) {

        TerminateInstanceInAutoScalingGroupRequest request = new TerminateInstanceInAutoScalingGroupRequest()
                .withInstanceId(instanceId)
                .withShouldDecrementDesiredCapacity(false);
        TerminateInstanceInAutoScalingGroupResult response = asgDelegator.getAsgClient().terminateInstanceInAutoScalingGroup(request);

        return response;
    }

    /**
     * Checks if the given auto scaling group name describes an existing ASG.
     *
     * @param asGroupName auto scaling group name
     * @return true if the auto scaling group exists; false otherwise
     */
    @Override
    public boolean isValidAutoScalingGroup(String asGroupName) {
        boolean flag;
        DescribeAutoScalingGroupsResult describeAutoScalingGroupsResult = asgDelegator.getAsgClient().describeAutoScalingGroups(new DescribeAutoScalingGroupsRequest().withAutoScalingGroupNames(asGroupName));

        flag = describeAutoScalingGroupsResult.toString().contains(asGroupName);

        return flag;
    }

    /**
     * Deprecated due to misleading name. Replacement is named getAutoScalingGroups()
     */
    @Deprecated
    public List<AutoScalingGroup> getAllInstancesInASGByStackName(String key, String value) {
        ArrayList<AutoScalingGroup> asgs = new ArrayList<>();
        DescribeAutoScalingGroupsResult describeAutoScalingGroupsResult = this.asgDelegator.getAsgClient().describeAutoScalingGroups(new DescribeAutoScalingGroupsRequest());
        describeAutoScalingGroupsResult.getAutoScalingGroups().forEach((asg) -> {
            if (key.isEmpty() && value.isEmpty()) {
                asgs.add(asg);
            } else {
                asg.getTags().forEach((tag) -> {
                    if (tag.getKey().equalsIgnoreCase(key) && tag.getValue().equalsIgnoreCase(value)) {
                        asgs.add(asg);
                    }
                });
            }
        });
        return asgs;
    }

    /**
     * Gets all auto scaling groups that match the given key and value tag. To get all auto scaling groups, set both the
     * key and the value to the empty string: "".
     *
     * @param key key in tag
     * @param value value in tag
     * @return list of matching Auto Scaling Groups
     */
    @Override
    public List<AutoScalingGroup> getAutoScalingGroups(String key, String value) {
        ArrayList<AutoScalingGroup> asgs = new ArrayList<>();
        DescribeAutoScalingGroupsResult describeAutoScalingGroupsResult = this.asgDelegator.getAsgClient().describeAutoScalingGroups(new DescribeAutoScalingGroupsRequest());
        describeAutoScalingGroupsResult.getAutoScalingGroups().forEach((asg) -> {
            if (key.isEmpty() && value.isEmpty()) {
                asgs.add(asg);
            } else {
                asg.getTags().forEach((tag) -> {
                    if (tag.getKey().equalsIgnoreCase(key) && tag.getValue().equalsIgnoreCase(value)) {
                        asgs.add(asg);
                    }
                });
            }
        });
        return asgs;
    }
}
