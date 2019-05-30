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

package com.intuit.cloudraider.model;

import com.amazonaws.services.ec2.model.Tag;

import java.util.List;

/**
 * EC2 Transfer Object for representing AWS EC2 instances.
 */
public class EC2InstanceTO {

    private String instanceId;
    private String privateIpAddress;
    private String stateName;
    private List<String> securityGroupIds;
    private String subnetId;
    private String vpcId;
    private String imageId;
    private String availabilityZone;
    private String loadBalancerName;
    private List<Tag> tags;

    /**
     * Gets load balancer name.
     *
     * @return the load balancer name
     */
    public String getLoadBalancerName() {
        return loadBalancerName;
    }

    /**
     * Sets load balancer name.
     *
     * @param loadBalancerName the load balancer name
     */
    public void setLoadBalancerName(String loadBalancerName) {
        this.loadBalancerName = loadBalancerName;
    }

    /**
     * Gets instance id.
     *
     * @return the instance id
     */
    public String getInstanceId() {
        return instanceId;
    }

    /**
     * Sets instance id.
     *
     * @param instanceId the instance id
     */
    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    /**
     * Gets private ip address.
     *
     * @return the private ip address
     */
    public String getPrivateIpAddress() {
        return privateIpAddress;
    }

    /**
     * Sets private ip address.
     *
     * @param privateIpAddress the private ip address
     */
    public void setPrivateIpAddress(String privateIpAddress) {
        this.privateIpAddress = privateIpAddress;
    }

    /**
     * Gets state name.
     *
     * @return the state name
     */
    public String getStateName() {
        return stateName;
    }

    /**
     * Sets state name.
     *
     * @param stateName the state name
     */
    public void setStateName(String stateName) {
        this.stateName = stateName;
    }

    /**
     * Gets tags.
     *
     * @return the tags
     */
    public List<Tag> getTags() {
        return tags;
    }

    /**
     * Sets tags.
     *
     * @param tags the tags
     */
    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    /**
     * Gets availability zone.
     *
     * @return the availability zone
     */
    public String getAvailabilityZone() {
        return availabilityZone;
    }

    /**
     * Sets availability zone.
     *
     * @param availabilityZone the availability zone
     */
    public void setAvailabilityZone(String availabilityZone) {
        this.availabilityZone = availabilityZone;
    }

    /**
     * Gets security group ids.
     *
     * @return the security group ids
     */
    public List<String> getSecurityGroupIds() {
        return securityGroupIds;
    }

    /**
     * Sets security group ids.
     *
     * @param securityGroupIds the security group ids
     */
    public void setSecurityGroupIds(List<String> securityGroupIds) {
        this.securityGroupIds = securityGroupIds;
    }

    /**
     * Gets subnet id.
     *
     * @return the subnet id
     */
    public String getSubnetId() {
        return subnetId;
    }

    /**
     * Sets subnet id.
     *
     * @param subnetId the subnet id
     */
    public void setSubnetId(String subnetId) {
        this.subnetId = subnetId;
    }

    /**
     * Gets vpc id.
     *
     * @return the vpc id
     */
    public String getVpcId() {
        return vpcId;
    }

    /**
     * Sets vpc id.
     *
     * @param vpcId the vpc id
     */
    public void setVpcId(String vpcId) {
        this.vpcId = vpcId;
    }

    /**
     * Gets image id.
     *
     * @return the image id
     */
    public String getImageId() {
        return imageId;
    }

    /**
     * Sets image id.
     *
     * @param imageId the image id
     */
    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    @Override
    public String toString() {
        return "EC2InstanceTO{" +
                "instanceId='" + instanceId + '\'' +
                ", privateIpAddress='" + privateIpAddress + '\'' +
                ", stateName='" + stateName + '\'' +
                ", securityGroupIds=" + securityGroupIds +
                ", subnetId='" + subnetId + '\'' +
                ", vpcId='" + vpcId + '\'' +
                ", loadBalancer='" + loadBalancerName + '\'' +
                ", imageId='" + imageId + '\'' +
                ", availabilityZone='" + availabilityZone + '\'' +
                ", tags=" + tags +
                '}';
    }


    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof EC2InstanceTO)) {
            return false;
        }

        EC2InstanceTO ec2InstanceTO = (EC2InstanceTO)o;
        return instanceId == ec2InstanceTO.instanceId;
    }

    public int hashCode() {
        int result = 17;
        result = 31 * result + instanceId.hashCode();
        return result;
    }
}
