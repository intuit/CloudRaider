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

import com.amazonaws.services.ec2.model.Volume;

import java.util.List;
import java.util.Map;

/**
 * AWS Elastic Block Store functionality.
 * <p>
  */
public interface EBSRaider {

    /**
     * Detach the given volume id from its attached instance.
     *
     * @param volumeId volume id
     * @return DetachVolumeResult response
     */
    public String detachEbsVolume(String volumeId);

    /**
     * Attach the given volume id from its attached instance.
     *
     * @param instanceId instance id
     * @param deviceName device name
     * @param volumeId   volume id
     * @return AttachVolumeResult string
     */
    public String attachEbsVolume(String instanceId, String deviceName, String volumeId);

    /**
     * Gets the volumes and the respective status for the volume ids specified.
     *
     * @param volumeIds variable length volume id
     * @return Map containing (volume id, volume status)
     */
    public Map getVolumesState(String... volumeIds);

    /**
     * Gets the volumes attached to the given instance.
     *
     * @param instanceId instance id
     * @return list of volumes
     */
    public List<Volume> getVolumesForGivenInstanceId(String instanceId);
}
