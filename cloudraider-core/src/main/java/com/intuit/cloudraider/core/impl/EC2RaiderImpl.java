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
import com.intuit.cloudraider.core.interfaces.EC2Raider;
import com.intuit.cloudraider.cucumber.util.CucumberHelperFunctions;
import com.intuit.cloudraider.exceptions.InvalidInputDataException;
import com.intuit.cloudraider.exceptions.ResourceNotFoundException;
import com.intuit.cloudraider.model.EC2InstanceTO;
import com.intuit.cloudraider.model.EC2Status;
import com.intuit.cloudraider.utils.Ec2Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;


/**
 * AWS EC2 functionality.
 * <p>
  */
@Component(value="ec2raiderBean")
public class EC2RaiderImpl implements EC2Raider {

    /**
     * The constant AVAILABILITY_ZONE_FILTER_KEY.
     */
    public static final String AVAILABILITY_ZONE_FILTER_KEY = "availability-zone";
    /**
     * The Logger.
     */
    Logger logger = LoggerFactory.getLogger(this.getClass());
    
    @Autowired
    private EC2Delegator ec2Delegator;

    /**
     * Instantiates a new Ec 2 raider.
     */
    public EC2RaiderImpl() {
    }

    /**
     * Get list of instances by availability zone, ignoring instances with any of the "ignore tags"
     * Note: will ignore tag names containing "bastion" and "admin".
     * Instance must have all the tags in compulsory tags to be returned in the list.
     *
     * @param availabilityZone availability zone (e.g. "us-west-1")
     * @param ignoreTags list of tags to ignore
     * @param compulsoryTags list of tags that must be included
     * @return list of terminated instance ids
     */

    @Override
    public List<EC2InstanceTO> getEc2InstanceIdsWithCompulsoryTagsForAvailabilityZone(String availabilityZone, List<Tag> ignoreTags, List<Tag> compulsoryTags) {
        if (ignoreTags == null) {
            ignoreTags = new ArrayList<>();
        }
        if (compulsoryTags == null) {
            compulsoryTags = new ArrayList<>();
        }

        List<EC2InstanceTO> instances = new ArrayList<>();

        for (Reservation reservation : getReservationsByAvailabilityZone(availabilityZone)) {
            for (Instance instance : reservation.getInstances()) {
                if (Ec2Utils.isAwsInfrastructure(instance)) {
                    continue;
                }
                if(instance.getState().getName().equalsIgnoreCase("terminated")) {
                    continue;
                }

                // OR condition with ignoreTags and AND condition with compulsoryTags
                // 1. If any one of the ignore tags is present then instance will be ignored
                // 2. For instance to be considered all compulsory tags should be present.

                boolean flag = true;

                for (Tag instanceTag : instance.getTags()) {
                    boolean instanceContainsTag = false;
                    for (Tag ignoreTag : ignoreTags) {
                        if (ignoreTag.equals(instanceTag)) {
                            instanceContainsTag = true;
                            break;
                        }
                    }
                    if(instanceContainsTag) {
                        flag = false;
                        break;
                    }
                }

                if (!flag) {
                    continue;
                }

                flag = CucumberHelperFunctions.containsAllCompulsoryTags(compulsoryTags, instance.getTags());

                if (flag) {
                    EC2InstanceTO ec2InstanceTO = createEc2Instance(instance);
                    instances.add(ec2InstanceTO);
                }
            }
        }
        return instances;
    }

    /**
     * Get list of instances by availability zone, ignoring instances with any of the "ignore tags"
     * Note: will ignore tag names containing "bastion" and "admin".
     *
     * @param availabilityZone availability zone
     * @param instanceIdsToIgnore instances to ignore
     * @return list of EC2InstanceTO (ec2 instances) in given availability zone
     */
    @Override
    public List<EC2InstanceTO> getEc2InstancesForAvailabilityZone(String availabilityZone, List<String> instanceIdsToIgnore) {
        if (instanceIdsToIgnore == null) {
            instanceIdsToIgnore = new ArrayList<>();
        }
        List<String> instanceIds = new ArrayList<>();

        for (Reservation reservation : getReservationsByAvailabilityZone(availabilityZone)) {
            for (Instance instance : reservation.getInstances()) {
                if (Ec2Utils.isAwsInfrastructure(instance)
                        || instanceIdsToIgnore.contains(instance.getInstanceId())
                        || instance.getState().getName().equalsIgnoreCase("terminated")) {
                    continue;
                }
                if(instance.getState().getName().equalsIgnoreCase("terminated")) {
                	continue;
                }
                instanceIds.add(instance.getInstanceId());
            }
        }
        return getEC2InstancesByIds(instanceIds);
    }

    /**
     * Get list of instance private ip addresses by availability zone, ignoring instances with any of the "ignore tags"
     * Note: will ignore tag names containing "bastion" and "admin".
     *
     * @param availabilityZone availability zone
     * @param instanceIdsToIgnore instances to ignore
     * @return list of ec2 private ip addresses
     */
    @Override
    public List<String> getEc2InstanceIPsForAvailabilityZone(String availabilityZone, List<String> instanceIdsToIgnore) {

        if (instanceIdsToIgnore == null) {
            instanceIdsToIgnore = new ArrayList<>();
        }

        List<String> instanceIps = new ArrayList<String>();
        for (Reservation reservation : getReservationsByAvailabilityZone(availabilityZone)) {
            for (Instance instance : reservation.getInstances()) {
                if (Ec2Utils.isAwsInfrastructure(instance)
                        || instanceIdsToIgnore.contains(instance.getInstanceId())) {
                    continue;
                }
                instanceIps.add(instance.getPrivateIpAddress());
            }
        }
        return instanceIps;
    }

    /**
     * Get all private ip addresses of instances with the matching name and part of the given availability zone.
     *
     * @param name name
     * @param availabilityZone availability zone
     * @return list of private ip addresses
     */
    @Override
    public List<String> getInstancesIPsForAZWithName(String name, String availabilityZone) {
        List<EC2InstanceTO> instances = getInstancesByName(name);
        return instances.stream()
                .filter(x -> x.getAvailabilityZone().equalsIgnoreCase(availabilityZone))
                .map(EC2InstanceTO::getPrivateIpAddress)
                .collect(Collectors.toList());
    }

    /**
     * Get the reservations in the availability zone.
     *
     * @param availabilityZone availability zone
     * @return list of EC2 Reservations
     */
    private List<Reservation> getReservationsByAvailabilityZone(String availabilityZone) {
        Filter availabilityZoneFilter = new Filter(AVAILABILITY_ZONE_FILTER_KEY, Arrays.asList(availabilityZone));
        DescribeInstancesResult result = ec2Delegator.getEc2().describeInstances(
                new DescribeInstancesRequest().withFilters(availabilityZoneFilter)
        );
        
        return result.getReservations();
    }

    /**
     * Get the instance ids of instances which match at least one of the tags
     *
     * @param tags tags
     * @return list of instance ids
     */
    @Override
    public List<String> getInstanceIdsForTags(List<Tag> tags) {
        List<EC2InstanceTO> instances = getInstancesFromAnyTags(tags);
        return instances.stream().map(EC2InstanceTO::getInstanceId).collect(Collectors.toList());
    }

    /**
     * Get all instances with the given name.
     *
     * @param name instance name
     * @return list of EC2InstanceTO (ec2 instances)
     */
    @Override
    public List<EC2InstanceTO> getInstancesByName(String name) {
        Tag t = new Tag().withKey("Name").withValue(name);
        List<Tag> tags = new ArrayList<>();
        tags.add(t);

        return getInstancesFromAnyTags(tags);
    }

    /**
     * Terminate the given instances.
     *
     * @param instanceIds instance ids
     */
    @Override
    public void terminateEc2InstancesById(String... instanceIds) {

        if (instanceIds.length <= 0 || instanceIds == null) {
            throw new InvalidInputDataException("Invalid list of instanceIds");
        }
        TerminateInstancesRequest request1 = new TerminateInstancesRequest().withInstanceIds(instanceIds);
        TerminateInstancesResult response = ec2Delegator.getEc2().terminateInstances(request1);
    }

    /**
     * Terminate the instance with the given id.
     *
     * @param instanceId instance id
     */
    @Override
    public void terminateEc2InstancesById(String instanceId) {

        if (instanceId == null || instanceId.isEmpty()) {
            throw new InvalidInputDataException("Empty/Null instanceId provided in request");
        }
       logger.info("EC2Raider: Terminating instance with id: " + instanceId);
        TerminateInstancesRequest request1 = new TerminateInstancesRequest().withInstanceIds(instanceId);
        TerminateInstancesResult response = ec2Delegator.getEc2().terminateInstances(request1);
    }

    /**
     * Terminate the given instances.
     *
     * @param instanceIds list of instance ids
     */
    @Override
    public void terminateEc2InstancesById(List<String> instanceIds) {
        if (instanceIds.isEmpty()) {
            throw new InvalidInputDataException("Invalid list of instanceIds");
        }

        TerminateInstancesRequest request1 = new TerminateInstancesRequest().withInstanceIds(instanceIds);
        TerminateInstancesResult response = ec2Delegator.getEc2().terminateInstances(request1);
    }

    /**
     * Terminates the EC2 instances with the given name.
     *
     * @param names names
     */
    @Override
    public void terminateEC2InstancesByName(String... names) {
        List<String> ids = terminateEC2InstancesByNameHelper(names);

        if (ids.isEmpty()) {
            throw new ResourceNotFoundException("No Instances are available");
        }

        TerminateInstancesRequest request1 = new TerminateInstancesRequest().withInstanceIds(ids);
        TerminateInstancesResult response = ec2Delegator.getEc2().terminateInstances(request1);
    }

    /**
     * Terminates the set number of EC2 instances with the given name.
     *
     * @param name name
     * @param numberOfInstances number of instances to terminate
     */
    @Override
    public void terminateEC2InstancesByName(String name, int numberOfInstances) {
        List<String> ids = terminateEC2InstancesByNameHelper(name);

        if (ids.isEmpty()) {
            throw new ResourceNotFoundException("No Instances are available");
        } else if (numberOfInstances > ids.size()) {
            logger.debug("EC2Raider: Number of Instance provided for termination is greater than running instances, changing number from: "
                    + numberOfInstances + " to " + ids.size());
            numberOfInstances = ids.size();
        }

        TerminateInstancesRequest request1 = new TerminateInstancesRequest().withInstanceIds(ids.subList(0, numberOfInstances));
        TerminateInstancesResult response = ec2Delegator.getEc2().terminateInstances(request1);
    }

    /**
     * Helper function for terminateEC2InstancesByName
     * @param names names to terminate
     * @return list of matching instance ids
     */
    private List<String> terminateEC2InstancesByNameHelper(String... names) {
        List<String> namesList = Arrays.asList(names);
        List<String> ids = new ArrayList<>();
        List<Tag> tags = new ArrayList<>();

        for (String name : namesList) {
            Tag t = new Tag().withKey("Name").withValue(name);
            tags.add(t);
            ids.addAll(getInstanceIdsForTags(tags));
            tags.clear();
        }
        ids = ids.stream().distinct().collect(Collectors.toList());
        return ids;
    }

    /**
     * Gets all the instances that match at least one of the provided tags.
     *
     * @param tags list of tags
     * @return list of EC2 instances
     */
    @Override
    public List<EC2InstanceTO> getInstancesFromAnyTags(List<Tag> tags) {
        List<EC2InstanceTO> instances = new ArrayList<>();
        for (Tag tag : tags) {
            String key = tag.getKey();
            String value = tag.getValue();
            for (Reservation res : ec2Delegator.getEc2().describeInstances().getReservations()) {
                for (Instance i : res.getInstances()) {
                    if (i.getState().getName().equalsIgnoreCase("running")) {
                        for (Tag instanceTag : i.getTags()) {
                            if (key.equalsIgnoreCase(instanceTag.getKey()) && value.equalsIgnoreCase(instanceTag.getValue())) {
                                instances.add(createEc2Instance(i));
                                break;
                            }
                        }
                    }
                }
            }
        }
        return instances;
    }

    /**
     * Get the EC2 instance with the given id.
     *
     * @param instanceId instance id
     * @return EC2InstanceTO (ec2 instances)
     */
    @Override
    public EC2InstanceTO getEC2InstanceById(String instanceId) {

        if (instanceId.isEmpty() || instanceId == null) {
            throw new InvalidInputDataException("Empty/Null instanceId provided in request");
        }

        EC2InstanceTO ec2InstanceTO = null;
        for (Reservation res : ec2Delegator.getEc2().describeInstances().getReservations()) {
            for (Instance i : res.getInstances()) {
                if (i.getInstanceId().equalsIgnoreCase(instanceId) && i.getState().getName().equalsIgnoreCase("running")) {
                    ec2InstanceTO = this.createEc2Instance(i);
                    break;
                }
            }
        }

        if (ec2InstanceTO == null) {
            throw new ResourceNotFoundException("Unable to find EC2 Instances with given instance id: " + instanceId);
        }
        return ec2InstanceTO;
    }

    /**
     * Get the instances with the given ids.
     *
     * @param instanceIds list of instance ids
     * @return list of EC2InstanceTO (ec2 instances)
     */
    @Override
    public List<EC2InstanceTO> getEC2InstancesByIds(List<String> instanceIds) {
        List<EC2InstanceTO> ec2InstanceTOList = new ArrayList<>();

        instanceIds.stream().forEach(
                instanceId -> {
                    ec2InstanceTOList.add(getEC2InstanceById(instanceId));
                }
        );

        return ec2InstanceTOList;
    }

    /**
     * Get all available instances that do not contain any of the filteredWords in their names.
     *
     * @param filteredWords words to filter out from the name
     * @return list of EC2InstanceTO (ec2 instances)
     */
    @Override
    public List<EC2InstanceTO> getFilteredActiveInstances(List<String> filteredWords) {
        DescribeInstancesRequest request = new DescribeInstancesRequest();
        ArrayList<String> vals = new ArrayList<>();
        vals.add("running");

        Filter filter = new Filter("instance-state-name", vals);

        List<Reservation> res = ec2Delegator.getEc2().describeInstances(request.withFilters(filter)).getReservations();
        List<String> ids = new ArrayList<>();
        for (Reservation reservation : res) {
            List<Instance> instances = reservation.getInstances();
            for (Instance in : instances) {
                List<Tag> tags = in.getTags();
                Optional<Tag> name = tags.stream().filter(y -> y.getKey().equalsIgnoreCase("name")).findFirst();
                boolean flag = true;
                for (String word : filteredWords) {
                    if (name.isPresent() && name.get().getValue().toLowerCase().contains(word.toLowerCase())) {
                        flag = false;
                        break;
                    }
                }
                if (flag) {
                    ids.add(in.getInstanceId());
                }
            }
        }

        return getEC2InstancesByIds(ids.stream().distinct().collect(Collectors.toList()));
    }

    /**
     * Given an EC2 instance object from AWS, create internally used EC2InstanceTO object to represent same info.
     *
     * @param i instance
     * @return EC2InstanceTO (ec2 instances)
     */
    private EC2InstanceTO createEc2Instance(Instance i) {
        EC2InstanceTO ec2InstanceTO = new EC2InstanceTO();
        ec2InstanceTO.setStateName(i.getState().getName());
        ec2InstanceTO.setPrivateIpAddress(i.getPrivateIpAddress());
        ec2InstanceTO.setInstanceId(i.getInstanceId());
        ec2InstanceTO.setTags(i.getTags());
        ec2InstanceTO.setAvailabilityZone(i.getPlacement().getAvailabilityZone());
        ec2InstanceTO.setSecurityGroupIds(i.getSecurityGroups().parallelStream().map(GroupIdentifier::getGroupId).collect(Collectors.toList()));
        ec2InstanceTO.setSubnetId(i.getSubnetId());
        ec2InstanceTO.setVpcId(i.getVpcId());
        //ec2InstanceTO.setLoadBalancerName(getLoadBalancerName(i.getInstanceId()));
        return ec2InstanceTO;
    }

    /**
     * Stop the instance with the matching id.
     *
     * @param instanceId instance id
     */
    @Override
    public void stopEc2Instances(String instanceId) {

        if (instanceId.isEmpty() || instanceId == null) {
            throw new InvalidInputDataException("Empty/Null instanceId provided in request");
        }

        StopInstancesRequest request = new StopInstancesRequest()
                .withInstanceIds(instanceId);

        StopInstancesResult response = ec2Delegator.getEc2().stopInstances(request);
    }

    /**
     * Restart the instance with the matching id.
     *
     * @param instanceId instance id
     */
    @Override
    public void restartEc2Instances(String instanceId) {

        if (instanceId.isEmpty() || instanceId == null) {
            throw new InvalidInputDataException("Empty/Null instanceId provided in request");
        }

        RebootInstancesRequest request = new RebootInstancesRequest()
                .withInstanceIds(instanceId);

        RebootInstancesResult response = ec2Delegator.getEc2().rebootInstances(request);
    }

    /**
     * Get the status of the instance with the matching id.
     *
     * @param instanceId instance id
     * @return instance status
     */
    @Override
    public String getInstanceStatusById(String instanceId) {
        String result = null;

        for (Reservation res : ec2Delegator.getEc2().describeInstances().getReservations()) {
            for (Instance i : res.getInstances()) {
                if (i.getInstanceId().equalsIgnoreCase(instanceId)) {

                    result = i.getState().getName();
                }
            }
        }

        if (result == null) {
            throw new ResourceNotFoundException("Unable to find EC2 instances for id: " + instanceId);
        }

        return result;
    }

    /**
     * Detach the given security group from the instance.
     *
     * @param instanceId instance id
     * @param securityGroup security group id
     */
    @Override
    public void detachSecurityGroup(String instanceId, String securityGroup) {

        if (instanceId.isEmpty() || instanceId == null) {
            throw new InvalidInputDataException("Empty/Null instanceId provided in request");
        } else if (securityGroup.isEmpty() || securityGroup == null) {
            throw new InvalidInputDataException("Empty/Null securityGroup provided in request");
        }

        List<String> securityGroups = this.getSecurityGroups(instanceId);

        if (securityGroups != null && securityGroups.contains(securityGroup)) {
            securityGroups.remove(securityGroup);
            ec2Delegator.getEc2().modifyInstanceAttribute(new ModifyInstanceAttributeRequest().withInstanceId(instanceId).withGroups(securityGroups));

        } else {
            throw new InvalidInputDataException("Invalid SecurityGroup: " + securityGroup + " provided in request");
        }

    }

    /**
     * Attach the given security group to the instance.
     *
     * @param instanceId instance id
     * @param securityGroup security group id
     */
    @Override
    public void attachSecurityGroup(String instanceId, String securityGroup) {

        if (instanceId.isEmpty() || instanceId == null) {
            throw new InvalidInputDataException("Empty/Null instanceId provided in request");
        } else if (securityGroup.isEmpty() || securityGroup == null) {
            throw new InvalidInputDataException("Empty/Null securityGroup provided in request");
        }

        List<String> securityGroups = this.getSecurityGroups(instanceId);

        if (securityGroups != null && !securityGroups.contains(securityGroup)) {
            securityGroups.add(securityGroup);
            ec2Delegator.getEc2().modifyInstanceAttribute(new ModifyInstanceAttributeRequest().withInstanceId(instanceId).withGroups(securityGroups));
        }


    }

    /**
     * Get the security groups attached to the given instance.
     *
     * @param instanceId instance id
     * @return list of security group ids
     */
    @Override
    public List<String> getSecurityGroups(String instanceId) {
        if (instanceId.isEmpty() || instanceId == null) {
            throw new InvalidInputDataException("Empty/Null instanceId provided in request");
        }

        List<String> secGroupIds = new ArrayList<String>();
        List<GroupIdentifier> groups = ec2Delegator.getEc2().describeInstanceAttribute(new DescribeInstanceAttributeRequest().withAttribute("groupSet").withInstanceId(instanceId)).getInstanceAttribute().getGroups();
        groups.forEach(group -> secGroupIds.add(group.getGroupId()));
        return secGroupIds;
    }

    /**
     * Detach the given security groups from the instance.
     *
     * @param instanceId instance id
     * @param securityGroups security group ids
     */
    @Override
    public void detachSecurityGroups(String instanceId, String... securityGroups) {

        if (instanceId.isEmpty() || instanceId == null) {
            throw new InvalidInputDataException("Empty/Null instanceId provided in request");
        } else if (securityGroups.length <= 0 || securityGroups == null) {
            throw new InvalidInputDataException("Empty/Null securityGroups provided in request");
        }


        List<String> existingSecurityGroups = this.getSecurityGroups(instanceId);

        if (existingSecurityGroups != null && existingSecurityGroups.containsAll(Arrays.asList(securityGroups))) {
            existingSecurityGroups.removeAll(Arrays.asList(securityGroups));
            ec2Delegator.getEc2().modifyInstanceAttribute(new ModifyInstanceAttributeRequest().withInstanceId(instanceId).withGroups(existingSecurityGroups));
        } else {
            throw new InvalidInputDataException("Invalid SecurityGroups: " + securityGroups + " provided in request");
        }
    }

    /**
     * Attach the given security groups to the instance.
     *
     * @param instanceId instance id
     * @param securityGroups security group ids
     */
    @Override
    public void attachSecurityGroups(String instanceId, String... securityGroups) {

        if (instanceId.isEmpty() || instanceId == null) {
            throw new InvalidInputDataException("Empty/Null instanceId provided in request");
        } else if (securityGroups.length <= 0 || securityGroups == null) {
            throw new InvalidInputDataException("Empty/Null securityGroups provided in request");
        }

        List<String> existingSecurityGroups = this.getSecurityGroups(instanceId);

        if (existingSecurityGroups != null && !existingSecurityGroups.containsAll(Arrays.asList(securityGroups))) {
            existingSecurityGroups.addAll(Arrays.asList(securityGroups));
            ec2Delegator.getEc2().modifyInstanceAttribute(new ModifyInstanceAttributeRequest().withInstanceId(instanceId).withGroups(existingSecurityGroups));
        }

    }

    /**
     * Get AWS instance with the matching id.
     *
     * @param instanceId instance id
     * @return AWS Instance
     */
    @Override
    public Instance getInstanceDetailsById(String instanceId) {
        Instance result = null;
        Iterator var3 = this.ec2Delegator.getEc2().describeInstances().getReservations().iterator();

        while(var3.hasNext()) {
            Reservation res = (Reservation)var3.next();
            Iterator var5 = res.getInstances().iterator();

            while(var5.hasNext()) {
                Instance i = (Instance)var5.next();
                if (i.getInstanceId().equalsIgnoreCase(instanceId)) {
                    result = i;
                }
            }
        }

        return result;
    }

    /**
     * Gets the EC2Status of the specified instance.
     *
     * @param name name of instance
     * @return EC2Status
     */
    @Override
    public EC2Status getInstanceStatus(String name) {

        EC2Status ec2Status = null;
        Map instanceState = new HashMap();

        List tagNames = new ArrayList();
        tagNames.add(name);

        Filter filter1 = new Filter("tag:Name", tagNames);

        DescribeInstancesRequest request = new DescribeInstancesRequest();
        DescribeInstancesResult result = ec2Delegator.getEc2().describeInstances(request.withFilters(filter1));

        List<Reservation> reservations = result.getReservations();
        for (Reservation reservation : reservations) {
            List<Instance> instances = reservation.getInstances();

            for (Instance instance : instances) {
                instanceState.put(instance.getInstanceId(), instance.getState().getName());
            }
        }

        if (instanceState.isEmpty()) {
            throw new ResourceNotFoundException("Unable to find EC2 instances for tag: " + name);
        }

        ec2Status = new EC2Status();
        ec2Status.setStatus(instanceState);
        ec2Status.setTagName(name);

        return ec2Status;
    }

    /**
     * Get status of the specified instance.
     *
     * @param name name of instance
     * @return Map of instance status (instance id, instance state)
     */
    @Override
    public Map getEC2InstanceState(String name) {

        return getInstanceStatus(name).getStatus();
    }

    /**
     * Deprecated due to misleading name. This function only uses the "name" tag value to filter.
     * Replaced with getInstancesIpsForAZWithName();
     */
    @Deprecated
    public List getInstancesIpsForAZ(String tag, String availabilityZoneName) {

        DescribeInstancesRequest request = new DescribeInstancesRequest();

        ArrayList<String> tags = new ArrayList<>();
        tags.add(tag);

        Filter filter1 = new Filter("tag:Name", tags);

        List result =
                ec2Delegator.getEc2().describeInstances(request.withFilters(filter1))
                        .getReservations()
                        .stream()
                        .map(Reservation::getInstances)
                        .flatMap(l -> l.stream())
                        .collect(Collectors.toList())
                        .stream()
                        .filter(x -> x.getPlacement().getAvailabilityZone().equalsIgnoreCase(availabilityZoneName) && x.getState().getName().equalsIgnoreCase("running"))
                        .map(Instance::getPrivateIpAddress)
                        .collect(Collectors.toList());


        if (result.isEmpty()) {
            throw new ResourceNotFoundException("Unable to find EC2 instances IP address for tag: " + tag + " with AZ: " + availabilityZoneName);
        }
        return result;

    }

    /**
     * Deprecated due to misleading name. This function only uses the "name" tag value to filter.
     * Replaced with getInstancesByName();
     */
    @Deprecated
    public List<EC2InstanceTO> getInstancesIdsForOneTag(String tagName) {

        List<String> instanceIds = new ArrayList<String>();


        List tagNames = new ArrayList();

        tagNames.add(tagName);


        DescribeInstancesRequest request = new DescribeInstancesRequest();

        Filter filter1 = new Filter("tag:Name", tagNames);

        DescribeInstancesResult result = ec2Delegator.getEc2().describeInstances(request.withFilters(filter1));

        List<Reservation> reservations = result.getReservations();

        for (Reservation reservation : reservations) {
            List<Instance> instances = reservation.getInstances();


            for (Instance instance : instances) {
                if (instance.getState().getName().equalsIgnoreCase("running")) {
                    instanceIds.add(instance.getInstanceId());
                }
            }
        }
        return getEC2InstancesByIds(instanceIds);
    }

    /**
     * Deprecated due to misleading name. This function only uses the "name" tag value to filter.
     * Replaced with terminateEC2InstancesByName();
     */
    @Deprecated
    public void terminateEc2InstancesByTags(String... tagName) {
        List<String> instanceIds = new ArrayList<String>();

        DescribeInstancesRequest request = new DescribeInstancesRequest();

        Filter filter1 = new Filter("tag:Name", Arrays.asList(tagName));

        DescribeInstancesResult result = ec2Delegator.getEc2().describeInstances(request.withFilters(filter1));
        List<Reservation> reservations = result.getReservations();

        for (Reservation reservation : reservations) {
            List<Instance> instances = reservation.getInstances();


            for (Instance instance : instances) {
                instanceIds.add(instance.getInstanceId());
            }
        }

        if (instanceIds.isEmpty()) {
            throw new ResourceNotFoundException("No Instances are avaialble");
        }

        TerminateInstancesRequest request1 = new TerminateInstancesRequest().withInstanceIds(instanceIds);
        TerminateInstancesResult response = ec2Delegator.getEc2().terminateInstances(request1);

    }

    /**
     * Deprecated due to misleading name. This function only uses the "name" tag value to filter.
     * Replaced with terminateEC2InstancesByName();
     */
    @Deprecated
    public void terminateEc2InstancesByTags(String tagName, int numberOfInstances) {
        List<String> instanceIds = new ArrayList<String>();

        List tags = new ArrayList<String>();
        tags.add(tagName);

        DescribeInstancesRequest request = new DescribeInstancesRequest();

        Filter filter1 = new Filter("tag:Name", tags);

        DescribeInstancesResult result = ec2Delegator.getEc2().describeInstances(request.withFilters(filter1));
        List<Reservation> reservations = result.getReservations();

        for (Reservation reservation : reservations) {
            List<Instance> instances = reservation.getInstances();


            for (Instance instance : instances) {
                instanceIds.add(instance.getInstanceId());
            }
        }

        if (instanceIds.isEmpty()) {
            throw new ResourceNotFoundException("No Instances are avaialble");
        } else if (numberOfInstances > instanceIds.size()) {
           logger.debug("EC2Raider: Number of Instance provided for termination is greater than running instances, changing number from: " + numberOfInstances + " to " + instanceIds.size());
            numberOfInstances = instanceIds.size();

        }

        TerminateInstancesRequest request1 = new TerminateInstancesRequest().withInstanceIds(instanceIds.subList(0, numberOfInstances));

        TerminateInstancesResult response = ec2Delegator.getEc2().terminateInstances(request1);
    }

    /**
     * Deprecated due to misleading name. This function only uses the "name" tag value to filter. Furthermore, the EC2
     * tag used only contains the name.
     * Replaced with getEc2InstancesForAvailabilityZone();
     */
    @Deprecated
    public List<EC2InstanceTO> getInstancesForAZ(String tag, String availabilityZoneName) {

        if (tag.isEmpty() || tag == null) {
            throw new InvalidInputDataException("Empty/Null tag provided in request");
        } else if (availabilityZoneName.isEmpty() || availabilityZoneName == null) {
            throw new InvalidInputDataException("Empty/Null availabilityZoneName provided in request");
        }

        DescribeInstancesRequest request = new DescribeInstancesRequest();

        ArrayList<String> tags = new ArrayList<>();
        tags.add(tag);

        Filter filter1 = new Filter("tag:Name", tags);

        List<String> result =
                ec2Delegator.getEc2().describeInstances(request.withFilters(filter1))
                        .getReservations()
                        .stream()
                        .map(Reservation::getInstances)
                        .flatMap(l -> l.stream())
                        .collect(Collectors.toList())
                        .stream()
                        .filter(x -> x.getPlacement().getAvailabilityZone().equalsIgnoreCase(availabilityZoneName) && x.getState().getName().equalsIgnoreCase("running"))
                        .map(Instance::getInstanceId)
                        .collect(Collectors.toList());


        if (result.isEmpty()) {
            throw new ResourceNotFoundException("Unable to find EC2 instances for tag: " + tag + " with AZ: " + availabilityZoneName);
        }

        List<EC2InstanceTO> list = getEC2InstancesByIds(result);
        Collections.shuffle(list);
        return list;

    }

    /**
     * Deprecated due to misleading name -- only the tag value matters.
     * Replaced with getInstancesFromAnyTags().
     */
    @Deprecated
    public EC2InstanceTO getEC2StatusByTag(String tag) {
        EC2InstanceTO ec2InstanceTO = null;
        for (Reservation res : ec2Delegator.getEc2().describeInstances().getReservations()) {
            for (Instance i : res.getInstances()) {
                if (i.getState().getName().equalsIgnoreCase("running")) {
                    for (Tag t : i.getTags()) {
                        if (t.getValue().equalsIgnoreCase(tag)) {
                            ec2InstanceTO = this.createEc2Instance(i);
                            break;
                        }
                    }
                }
            }
        }

        if (ec2InstanceTO == null) {
            throw new ResourceNotFoundException("Unable to find EC2 Instances with given tag: " + tag);
        }
        return ec2InstanceTO;

    }

}