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

package com.intuit.cloudraider.utils;

import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Tag;
import com.intuit.cloudraider.model.EC2InstanceTO;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class to manage EC2 infrastructure.
 */
public class Ec2Utils {
    private static final String BASTION_TAG = "bastion";
    private static final String ADMIN_TAG = "admin";
    private static final String AWS_NAME_TAG_KEY = "Name";

    /**
     * Returns true if the provided instance is part of AWS architecture. At the moment, AWS architecture means
     * that the instance is either a bastion or an admin instance.
     *
     * @param instance instance to analyze
     * @return true if instance is part of AWS infrastructure
     */
    public static boolean isAwsInfrastructure(Instance instance) {
        boolean isAwsInfrastructure = false;
        for (Tag tag : instance.getTags()) {
            if (tag.getKey().contains(AWS_NAME_TAG_KEY)) {
                // used to access instances
                isAwsInfrastructure = isAwsInfrastructure != true ? tag.getValue().contains(BASTION_TAG) : true;
                // usually used to manage databases
                isAwsInfrastructure = isAwsInfrastructure != true ? tag.getValue().contains(ADMIN_TAG) : true;
            }

            if (isAwsInfrastructure) break; // early exit
        }
        return isAwsInfrastructure;
    }

    /**
     * From a provided list of ec2 instances, generate a respective list of ec2 instance ids.
     *
     * @param ec2InstanceTOList list of ec2 instances
     * @return list of ec2 instance ids
     */
    public static List<String> generateInstanceIdList(List<EC2InstanceTO> ec2InstanceTOList) {
        return ec2InstanceTOList.stream().map(EC2InstanceTO::getInstanceId).collect(Collectors.toList());
    }

}
