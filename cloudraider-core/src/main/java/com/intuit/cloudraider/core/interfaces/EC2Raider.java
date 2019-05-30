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

import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Tag;
import com.intuit.cloudraider.exceptions.InvalidInputDataException;
import com.intuit.cloudraider.exceptions.ResourceNotFoundException;
import com.intuit.cloudraider.model.EC2InstanceTO;
import com.intuit.cloudraider.model.EC2Status;

import java.util.List;
import java.util.Map;


public interface EC2Raider {

    /**
     * Get list of instances by availability zone, ignoring instances with any of the "ignore tags"
     * Note: will ignore tag names containing "bastion" and "admin".
     * Instance must have all the tags in compulsory tags to be returned in the list.
     *
     * @param availabilityZone availability zone (e.g. "us-west-1")
     * @param ignoreTags       list of tags to ignore
     * @param compulsoryTags   list of tags that must be included
     * @return list of terminated instance ids
     */
    public List<EC2InstanceTO> getEc2InstanceIdsWithCompulsoryTagsForAvailabilityZone(String availabilityZone, List<Tag> ignoreTags, List<Tag> compulsoryTags);

    /**
     * Get list of instances by availability zone, ignoring instances with any of the "ignore tags"
     * Note: will ignore tag names containing "bastion" and "admin".
     *
     * @param availabilityZone    availability zone
     * @param instanceIdsToIgnore instances to ignore
     * @return list of EC2InstanceTO (ec2 instances) in given availability zone
     */
    public List<EC2InstanceTO> getEc2InstancesForAvailabilityZone(String availabilityZone, List<String> instanceIdsToIgnore);

    /**
     * Get list of instance private ip addresses by availability zone, ignoring instances with any of the "ignore tags"
     * Note: will ignore tag names containing "bastion" and "admin".
     *
     * @param availabilityZone    availability zone
     * @param instanceIdsToIgnore instances to ignore
     * @return list of ec2 private ip addresses
     */
    public List<String> getEc2InstanceIPsForAvailabilityZone(String availabilityZone, List<String> instanceIdsToIgnore);

    /**
     * Get all private ip addresses of instances with the matching name and part of the given availability zone.
     *
     * @param name             name
     * @param availabilityZone availability zone
     * @return list of private ip addresses
     */
    public List<String> getInstancesIPsForAZWithName(String name, String availabilityZone);

    /**
     * Get the instance ids of instances which match at least one of the tags
     *
     * @param tags tags
     * @return list of instance ids
     */
    List<String> getInstanceIdsForTags(List<Tag> tags);

    /**
     * Get all instances with the given name.
     *
     * @param name instance name
     * @return list of EC2InstanceTO (ec2 instances)
     */
    List<EC2InstanceTO> getInstancesByName(String name);

    /**
     * Terminate the given instances.
     *
     * @param instanceIds instance ids
     * @throws InvalidInputDataException the invalid input data exception
     */
    public void terminateEc2InstancesById(String... instanceIds) throws InvalidInputDataException;

    /**
     * Terminate the instance with the given id.
     *
     * @param instanceId instance id
     * @throws InvalidInputDataException the invalid input data exception
     */
    public void terminateEc2InstancesById(String instanceId) throws InvalidInputDataException;

    /**
     * Terminate the given instances.
     *
     * @param instanceIds list of instance ids
     * @throws InvalidInputDataException the invalid input data exception
     */
    public void terminateEc2InstancesById(List<String> instanceIds) throws InvalidInputDataException;

    /**
     * Terminates the EC2 instances with the given name.
     *
     * @param names names
     */
    public void terminateEC2InstancesByName(String... names);

    /**
     * Terminates the set number of EC2 instances with the given name.
     *
     * @param name              name
     * @param numberOfInstances number of instances to terminate
     */
    public void terminateEC2InstancesByName(String name, int numberOfInstances);

    /**
     * Gets all the instances that match at least one of the provided tags.
     *
     * @param tags list of tags
     * @return list of EC2 instances
     */
    List<EC2InstanceTO> getInstancesFromAnyTags(List<Tag> tags);

    /**
     * Get the EC2 instance with the given id.
     *
     * @param instanceId instance id
     * @return EC2InstanceTO (ec2 instances)
     * @throws ResourceNotFoundException the resource not found exception
     */
    public EC2InstanceTO getEC2InstanceById(String instanceId) throws ResourceNotFoundException;

    /**
     * Get the instances with the given ids.
     *
     * @param instanceIds list of instance ids
     * @return list of EC2InstanceTO (ec2 instances)
     */
    public List<EC2InstanceTO> getEC2InstancesByIds(List<String> instanceIds);

    /**
     * Get all available instances that do not contain any of the filteredWords in their names.
     *
     * @param filteredWords words to filter out from the name
     * @return list of EC2InstanceTO (ec2 instances)
     */
    public List<EC2InstanceTO> getFilteredActiveInstances(List<String> filteredWords);

    /**
     * Stop the instance with the matching id.
     *
     * @param instanceId instance id
     * @throws InvalidInputDataException the invalid input data exception
     */
    public void stopEc2Instances(String instanceId) throws InvalidInputDataException;

    /**
     * Restart the instance with the matching id.
     *
     * @param instanceId instance id
     * @throws InvalidInputDataException the invalid input data exception
     */
    public void restartEc2Instances(String instanceId) throws InvalidInputDataException;

    /**
     * Get the status of the instance with the matching id.
     *
     * @param instanceId instance id
     * @return instance status
     * @throws ResourceNotFoundException the resource not found exception
     */
    public String getInstanceStatusById(String instanceId)throws ResourceNotFoundException;

    /**
     * Detach the given security group from the instance.
     *
     * @param instanceId    instance id
     * @param securityGroup security group id
     * @throws InvalidInputDataException the invalid input data exception
     */
    public void detachSecurityGroup(String instanceId, String securityGroup) throws InvalidInputDataException;

    /**
     * Attach the given security group to the instance.
     *
     * @param instanceId    instance id
     * @param securityGroup security group id
     * @throws InvalidInputDataException the invalid input data exception
     */
    public void attachSecurityGroup(String instanceId, String securityGroup) throws InvalidInputDataException;

    /**
     * Get the security groups attached to the given instance.
     *
     * @param instanceId instance id
     * @return list of security group ids
     */
    public List<String> getSecurityGroups(String instanceId);

    /**
     * Detach the given security groups from the instance.
     *
     * @param instanceId     instance id
     * @param securityGroups security group ids
     * @throws InvalidInputDataException the invalid input data exception
     */
    public void detachSecurityGroups(String instanceId, String... securityGroups) throws InvalidInputDataException;

    /**
     * Attach the given security groups to the instance.
     *
     * @param instanceId     instance id
     * @param securityGroups security group ids
     * @throws InvalidInputDataException the invalid input data exception
     */
    public void attachSecurityGroups(String instanceId, String... securityGroups) throws InvalidInputDataException;

    /**
     * Get AWS instance with the matching id.
     *
     * @param instanceId instance id
     * @return AWS Instance
     */
    public Instance getInstanceDetailsById(String instanceId);

    /**
     * Gets the EC2Status of the specified instance.
     *
     * @param name name of instance
     * @return EC2Status instance status
     * @throws ResourceNotFoundException the resource not found exception
     */
    public EC2Status getInstanceStatus(String name)throws ResourceNotFoundException;

    /**
     * Get status of the specified instance.
     *
     * @param name name of instance
     * @return Map of instance status (instance id, instance state)
     * @throws ResourceNotFoundException the resource not found exception
     */
    public Map getEC2InstanceState(String name) throws ResourceNotFoundException;

    /**
     * Deprecated due to misleading name. This function only uses the "name" tag value to filter.
     * Replaced with getInstancesIpsForAZWithName();
     *
     * @param tag                  the tag
     * @param availabilityZoneName the availability zone name
     * @return the instances ips for az
     */
    @Deprecated
    public List getInstancesIpsForAZ(String tag, String availabilityZoneName);

    /**
     * Deprecated due to misleading name. This function only uses the "name" tag value to filter.
     * Replaced with getInstancesByName();
     *
     * @param tagName the tag name
     * @return the instances ids for one tag
     */
    @Deprecated
    public List<EC2InstanceTO> getInstancesIdsForOneTag(String tagName);

    /**
     * Deprecated due to misleading name. This function only uses the "name" tag value to filter. Furthermore, the EC2
     * tag used only contains the name.
     * Replaced with getEc2InstancesForAvailabilityZone();
     *
     * @param tag              the tag
     * @param availabilityZone the availability zone
     * @return the instances for az
     */
    @Deprecated
    public List<EC2InstanceTO> getInstancesForAZ(String tag, String availabilityZone);

    /**
     * Deprecated due to misleading name. This function only uses the "name" tag value to filter.
     * Replaced with terminateEC2InstancesByName();
     *
     * @param tagNames the tag names
     * @throws InvalidInputDataException the invalid input data exception
     */
    @Deprecated
    public void terminateEc2InstancesByTags(String... tagNames) throws InvalidInputDataException;

    /**
     * Deprecated due to misleading name. This function only uses the "name" tag value to filter.
     * Replaced with terminateEC2InstancesByName();
     *
     * @param tagNames          the tag names
     * @param numberOfInstances the number of instances
     * @throws InvalidInputDataException the invalid input data exception
     */
    @Deprecated
    public void terminateEc2InstancesByTags(String tagNames, int numberOfInstances) throws InvalidInputDataException;

    /**
     * Deprecated due to misleading name -- only the tag value matters.
     * Replaced with getInstancesFromAnyTags().
     *
     * @param tag the tag
     * @return the ec 2 status by tag
     * @throws ResourceNotFoundException the resource not found exception
     */
    @Deprecated
    public EC2InstanceTO getEC2StatusByTag(String tag)throws ResourceNotFoundException;
}
