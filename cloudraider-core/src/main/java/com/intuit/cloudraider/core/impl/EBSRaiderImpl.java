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


import com.amazonaws.services.ec2.model.*;
import com.intuit.cloudraider.commons.EC2Delegator;
import com.intuit.cloudraider.core.interfaces.EBSRaider;
import com.intuit.cloudraider.exceptions.InvalidInputDataException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AWS Elastic Block Store functionality.
 * <p>
  */
@Component(value="ebsRaiderBean")
public class EBSRaiderImpl implements EBSRaider {

    /**
     * The Logger.
     */
    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private EC2Delegator ec2Delegator;

    /**
     * Instantiates a new Ebs raider.
     */
    public EBSRaiderImpl() {
    }

    /**
     * Detach the given volume id from its attached instance.
     *
     * @param volumeId volume id
     * @return DetachVolumeResult response
     */
    public String detachEbsVolume(String volumeId) {
        if (volumeId.isEmpty() || volumeId == null) {
            throw new InvalidInputDataException("Empty/Null volumeId provided in request");
        }
        DetachVolumeRequest request1 = new DetachVolumeRequest(volumeId).withForce(true);
        DetachVolumeResult response = ec2Delegator.getEc2().detachVolume(request1);
        return response.toString();
    }

    /**
     * Attach the given volume id from its attached instance.
     *
     * @param instanceId instance id
     * @param deviceName device name
     * @param volumeId volume id
     * @return AttachVolumeResult string
     */
    public String attachEbsVolume(String instanceId, String deviceName, String volumeId) {
        AttachVolumeRequest attachRequest = new AttachVolumeRequest()
                .withInstanceId(instanceId).withDevice(deviceName)
                .withVolumeId(volumeId);
        AttachVolumeResult response = ec2Delegator.getEc2().attachVolume(attachRequest);

        return response.toString();
    }

    /**
     * Gets the volumes and the respective status for the volume ids specified.
     *
     * @param volumeIds variable length volume id
     * @return Map containing (volume id, volume status)
     */
    public Map getVolumesState(String... volumeIds) {

        Map volumeState = new HashMap();
        List<Volume> volumes = ec2Delegator.getEc2().describeVolumes(new DescribeVolumesRequest().withVolumeIds(volumeIds)).getVolumes();


        for (Volume volume : volumes) {
            String status = volume.getState();
            String volumeId = volume.getVolumeId();
            volumeState.put(volumeId, status);

        }

        return volumeState;
    }

    /**
     * Gets the volumes attached to the given instance.
     *
     * @param instanceId instance id
     * @return list of volumes
     */
    public List<Volume> getVolumesForGivenInstanceId(String instanceId) {
        return ec2Delegator.getEc2().describeVolumes(new DescribeVolumesRequest()
                .withFilters(new Filter()
                        .withName("attachment.instance-id")
                        .withValues(instanceId)))
                .getVolumes();
    }
}

