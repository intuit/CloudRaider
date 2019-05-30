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

package com.intuit.cloudraider.cucumber.model;

import com.amazonaws.services.ec2.model.Tag;
import com.intuit.cloudraider.model.EC2InstanceTO;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;


/**
 * Cache that keeps track of state during the execution of cucumber features/scenarios.
 * One important feature to note: each execution of getInstances() is randomized -- different order is returned
 */
@Component
public class ExecutionStateCache {

    private List<EC2InstanceTO> instances;
    private List<EC2InstanceTO> unhealthyInstances;
    private List<EC2InstanceTO> stoppedInstances;
    private List<EC2InstanceTO> ssmCommandInvocationInstances;
    private List<EC2InstanceTO> deregisteredInstances;
    private List<String> blockedDomains;
    private List<String> detachedSubnets;
    private List<String> detachedSecurityGroups;
    private String ec2Tag;
    private String loadBalancerName;
    private List<String> processNames;
    private boolean healProcess;
    private boolean healNetwork;
    private boolean cpuSpiked;

    private int lastJenkinsPerfBuildNumber;
    private int healthyHostCount;


    private boolean ramDiskFull;
    private boolean blockPort;
    private List<Integer> portNums;
    private boolean blockDynamoDB;

    private boolean blockS3;
    private int portNum;
    private String dbName;


    private String cacheNodeName;
    private String commandId;
    private String elastiCacheClusterName;
    private List<String> dbInstances;


    private List<String> cacheNodes;


    private String dynamoDBTable;
    private long dynamoReadCapacity;
    private long dynamoWriteCapacity;


    private List<Tag> compulsoryTags;
    private List<Tag> ignoreTags;
    private String[] availaibilityzones;

    /**
     * Instantiates a new Execution state cache.
     */
    public ExecutionStateCache() {


        instances = new ArrayList<>();
        unhealthyInstances = new ArrayList<>();
        detachedSubnets = new ArrayList<>();
        blockedDomains = new ArrayList<>();
        deregisteredInstances = new ArrayList<>();
        stoppedInstances = new ArrayList<>();
        dbInstances = new ArrayList<>();
        cacheNodes = new ArrayList<>();
        detachedSecurityGroups = new ArrayList<>();
        ssmCommandInvocationInstances = new ArrayList<>();
        portNums = new ArrayList<>();

        processNames = new ArrayList<>();
        instances = new CopyOnWriteArrayList<>();
        unhealthyInstances = new CopyOnWriteArrayList<>();
        detachedSubnets = new CopyOnWriteArrayList<>();
        blockedDomains = new CopyOnWriteArrayList<>();
        dbInstances = new CopyOnWriteArrayList<>();
        detachedSecurityGroups = new CopyOnWriteArrayList<>();
        ssmCommandInvocationInstances = new CopyOnWriteArrayList<>();
        deregisteredInstances = new CopyOnWriteArrayList<>();

    }


    /**
     * Gets cache nodes.
     *
     * @return the cache nodes
     */
    public List<String> getCacheNodes() {
        return cacheNodes;
    }

    /**
     * Sets cache nodes.
     *
     * @param cacheNodes the cache nodes
     */
    public void setCacheNodes(List<String> cacheNodes) {
        this.cacheNodes = cacheNodes;
    }

    /**
     * Gets cache node name.
     *
     * @return the cache node name
     */
    public String getCacheNodeName() {
        return cacheNodeName;
    }

    /**
     * Sets cache node name.
     *
     * @param cacheNodeName the cache node name
     */
    public void setCacheNodeName(String cacheNodeName) {
        this.cacheNodeName = cacheNodeName;
    }

    /**
     * Find ec 2 instance given id ec 2 instance to.
     *
     * @param instanceID the instance id
     * @return the ec 2 instance to
     */
    public EC2InstanceTO findEC2InstanceGivenID(String instanceID) {
        for (EC2InstanceTO inst : instances) {
            if (inst.getInstanceId().equalsIgnoreCase(instanceID)) {
                return inst;
            }
        }
        return null;
    }

    /**
     * Gets instances.
     *
     * @return the instances
     */
    public List<EC2InstanceTO> getInstances() {
        Collections.shuffle(instances);
        return instances;
    }

    /**
     * Sets instances.
     *
     * @param instances the instances
     */
    public void setInstances(List<EC2InstanceTO> instances) {
        this.instances = instances;
    }

    /**
     * Clear instances.
     */
    public void clearInstances() {
        instances.clear();
    }

    /**
     * Add instances.
     *
     * @param instancesList the instances list
     */
    public void addInstances(List<EC2InstanceTO> instancesList) {
        instances.addAll(instancesList);
        instances = instances.stream().distinct().collect(Collectors.toList());

    }

    /**
     * Gets stopped instances.
     *
     * @return the stopped instances
     */
    public List<EC2InstanceTO> getStoppedInstances() {
        return stoppedInstances;
    }

    /**
     * Sets stopped instances.
     *
     * @param stoppedInstances the stopped instances
     */
    public void setStoppedInstances(List<EC2InstanceTO> stoppedInstances) {
        this.stoppedInstances = stoppedInstances;
    }

    /**
     * Gets db instances.
     *
     * @return the db instances
     */
    public List<String> getDBInstances() {
        Collections.shuffle(dbInstances);
        return dbInstances;
    }

    /**
     * Sets db instances.
     *
     * @param dbInstances the db instances
     */
    public void setDBInstances(List<String> dbInstances) {
        this.dbInstances = dbInstances;
    }

    /**
     * Add db instances.
     *
     * @param dbInstancesList the db instances list
     */
    public void addDBInstances(List<String> dbInstancesList) {
        dbInstances.addAll(dbInstancesList);
        dbInstances = dbInstances.stream().distinct().collect(Collectors.toList());
    }

    /**
     * Add deregistered instance.
     *
     * @param deregistered the deregistered
     */
    public void addDeregisteredInstance(EC2InstanceTO deregistered) {
        deregisteredInstances.add(deregistered);
        deregisteredInstances = deregisteredInstances.stream().distinct().collect(Collectors.toList());
    }

    /**
     * Add deregistered instance.
     *
     * @param deregistered the deregistered
     */
    public void addDeregisteredInstance(List<EC2InstanceTO> deregistered) {
        deregisteredInstances.addAll(deregistered);
        deregisteredInstances = deregisteredInstances.stream().distinct().collect(Collectors.toList());
    }

    /**
     * Clear deregistered instance.
     */
    public void clearDeregisteredInstance() {
        deregisteredInstances.clear();
    }

    /**
     * Gets deregisted instance.
     *
     * @return the deregisted instance
     */
    public List<EC2InstanceTO> getDeregistedInstance() {
        return deregisteredInstances;
    }

    /**
     * Gets db name.
     *
     * @return the db name
     */
    public String getDbName() {
        return dbName;
    }

    /**
     * Sets db name.
     *
     * @param dbName the db name
     */
    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    /**
     * Add un healthy instance.
     *
     * @param ec2InstanceTO the ec 2 instance to
     */
    public void addUnHealthyInstance(EC2InstanceTO ec2InstanceTO) {
        unhealthyInstances.add(ec2InstanceTO);
        unhealthyInstances = unhealthyInstances.stream().distinct().collect(Collectors.toList());
    }

    /**
     * Add un healthy instances.
     *
     * @param ec2InstanceTOList the ec 2 instance to list
     */
    public void addUnHealthyInstances(List<EC2InstanceTO> ec2InstanceTOList) {
        unhealthyInstances.addAll(ec2InstanceTOList);
        unhealthyInstances = unhealthyInstances.stream().distinct().collect(Collectors.toList());
    }

    /**
     * Add detached subnet.
     *
     * @param subnet the subnet
     */
    public void addDetachedSubnet(String subnet) {
        detachedSubnets.add(subnet);
        detachedSubnets = detachedSubnets.stream().distinct().collect(Collectors.toList());
    }

    /**
     * Remove detached subnet.
     *
     * @param subnet the subnet
     */
    public void removeDetachedSubnet(String subnet) {
        detachedSubnets.remove(subnet);
    }

    /**
     * Remove detached subnets.
     *
     * @param subnets the subnets
     */
    public void removeDetachedSubnets(List<String> subnets) {
        detachedSubnets.removeAll(subnets);
    }

    /**
     * Gets detached subnets.
     *
     * @return the detached subnets
     */
    public List<String> getDetachedSubnets() {
        return detachedSubnets;
    }

    /**
     * Add detached security group.
     *
     * @param group the group
     */
    public void addDetachedSecurityGroup(String group) {
        detachedSecurityGroups.add(group);
        detachedSecurityGroups = detachedSecurityGroups.stream().distinct().collect(Collectors.toList());
    }

    /**
     * Remove detached security group.
     *
     * @param group the group
     */
    public void removeDetachedSecurityGroup(String group) {
        detachedSecurityGroups.remove(group);
    }

    /**
     * Remove detached security groups.
     *
     * @param groups the groups
     */
    public void removeDetachedSecurityGroups(List<String> groups) {
        detachedSecurityGroups.removeAll(groups);
    }

    /**
     * Gets detached security groups.
     *
     * @return the detached security groups
     */
    public List<String> getDetachedSecurityGroups() {
        return detachedSecurityGroups;
    }

    /**
     * Gets unhealthy instances.
     *
     * @return the unhealthy instances
     */
    public List<EC2InstanceTO> getUnhealthyInstances() {
        return unhealthyInstances;
    }

    /**
     * Sets unhealthy instances.
     *
     * @param unhealthyInstances the unhealthy instances
     */
    public void setUnhealthyInstances(List<EC2InstanceTO> unhealthyInstances) {
        this.unhealthyInstances = unhealthyInstances;
    }

    /**
     * Gets ec 2 tag.
     *
     * @return the ec 2 tag
     */
    public String getEc2Tag() {
        return ec2Tag;
    }

    /**
     * Sets ec 2 tag.
     *
     * @param ec2Tag the ec 2 tag
     */
    public void setEc2Tag(String ec2Tag) {
        this.ec2Tag = ec2Tag;
    }

    /**
     * Gets process names.
     *
     * @return the process names
     */
    public List<String> getProcessNames() {
        return processNames;
    }

    /**
     * Add process name.
     *
     * @param processName the process name
     */
    public void addProcessName(String processName) {
        processNames.add(processName);
        processNames = processNames.stream().distinct().collect(Collectors.toList());
    }

    /**
     * Clear process names.
     */
    public void clearProcessNames() {
        processNames.clear();
    }

    /**
     * Is heal process boolean.
     *
     * @return the boolean
     */
    public boolean isHealProcess() {
        return healProcess;
    }

    /**
     * Sets heal process.
     *
     * @param healProcess the heal process
     */
    public void setHealProcess(boolean healProcess) {
        this.healProcess = healProcess;
    }

    /**
     * Is heal network boolean.
     *
     * @return the boolean
     */
    public boolean isHealNetwork() {
        return healNetwork;
    }

    /**
     * Sets heal network.
     *
     * @param healNetwork the heal network
     */
    public void setHealNetwork(boolean healNetwork) {
        this.healNetwork = healNetwork;
    }

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
     * Gets blocked domains.
     *
     * @return the blocked domains
     */
    public List<String> getBlockedDomains() {
        return blockedDomains;
    }

    /**
     * Sets blocked domains.
     *
     * @param blockedDomains the blocked domains
     */
    public void setBlockedDomains(List<String> blockedDomains) {
        this.blockedDomains = blockedDomains;
    }

    /**
     * Is cpu spiked boolean.
     *
     * @return the boolean
     */
    public boolean isCpuSpiked() {
        return cpuSpiked;
    }

    /**
     * Sets cpu spiked.
     *
     * @param cpuSpiked the cpu spiked
     */
    public void setCpuSpiked(boolean cpuSpiked) {
        this.cpuSpiked = cpuSpiked;
    }

    /**
     * Gets elasti cache cluster name.
     *
     * @return the elasti cache cluster name
     */
    public String getElastiCacheClusterName() {
        return elastiCacheClusterName;
    }

    /**
     * Sets elasti cache cluster name.
     *
     * @param elastiCacheClusterName the elasti cache cluster name
     */
    public void setElastiCacheClusterName(String elastiCacheClusterName) {
        this.elastiCacheClusterName = elastiCacheClusterName;
    }

    /**
     * Gets ssm command invocation instances.
     *
     * @return the ssm command invocation instances
     */
    public List<EC2InstanceTO> getSsmCommandInvocationInstances() {
        return ssmCommandInvocationInstances;
    }

    /**
     * Sets ssm command invocation instances.
     *
     * @param ssmCommandInvocationInstances the ssm command invocation instances
     */
    public void setSsmCommandInvocationInstances(List<EC2InstanceTO> ssmCommandInvocationInstances) {
        this.ssmCommandInvocationInstances = ssmCommandInvocationInstances;
    }

    /**
     * Add ssm command invocation instances.
     *
     * @param instance the instance
     */
    public void addSsmCommandInvocationInstances(EC2InstanceTO instance) {
        ssmCommandInvocationInstances.add(instance);
        ssmCommandInvocationInstances = ssmCommandInvocationInstances.stream().distinct().collect(Collectors.toList());

    }

    /**
     * Gets command id.
     *
     * @return the command id
     */
    public String getCommandId() {
        return commandId;
    }

    /**
     * Sets command id.
     *
     * @param commandId the command id
     */
    public void setCommandId(String commandId) {
        this.commandId = commandId;
    }

    /**
     * Is block port boolean.
     *
     * @return the boolean
     */
    public boolean isBlockPort() {
        return blockPort;
    }

    /**
     * Sets block port.
     *
     * @param blockPort the block port
     */
    public void setBlockPort(boolean blockPort) {
        this.blockPort = blockPort;
    }

    /**
     * Gets port nums.
     *
     * @return the port nums
     */
    public List<Integer> getPortNums() {
        return portNums;
    }

    /**
     * Add port num.
     *
     * @param portNum the port num
     */
    public void addPortNum(int portNum) {
        portNums.add(portNum);
        portNums = portNums.stream().distinct().collect(Collectors.toList());
    }

    /**
     * Clear port nums.
     */
    public void clearPortNums() {
        portNums.clear();
    }

    /**
     * Is ram disk full boolean.
     *
     * @return the boolean
     */
    public boolean isRamDiskFull() {
        return ramDiskFull;
    }

    /**
     * Sets ram disk full.
     *
     * @param ramDiskFull the ram disk full
     */
    public void setRamDiskFull(boolean ramDiskFull) {
        this.ramDiskFull = ramDiskFull;
    }

    /**
     * Gets last jenkins perf build number.
     *
     * @return the last jenkins perf build number
     */
    public int getLastJenkinsPerfBuildNumber() {
        return lastJenkinsPerfBuildNumber;
    }

    /**
     * Sets last jenkins perf build number.
     *
     * @param lastJenkinsPerfBuildNumber the last jenkins perf build number
     */
    public void setLastJenkinsPerfBuildNumber(int lastJenkinsPerfBuildNumber) {
        this.lastJenkinsPerfBuildNumber = lastJenkinsPerfBuildNumber;
    }

    /**
     * Gets healthy host count.
     *
     * @return the healthy host count
     */
    public int getHealthyHostCount() {
        return healthyHostCount;
    }


    /**
     * Is block dynamo db boolean.
     *
     * @return the boolean
     */
    public boolean isBlockDynamoDB() {
        return blockDynamoDB;
    }

    /**
     * Sets block dynamo db.
     *
     * @param blockDynamoDB the block dynamo db
     */
    public void setBlockDynamoDB(boolean blockDynamoDB) {
        this.blockDynamoDB = blockDynamoDB;
    }

    /**
     * Is block s 3 boolean.
     *
     * @return the boolean
     */
    public boolean isBlockS3() {
        return blockS3;
    }

    /**
     * Sets block s 3.
     *
     * @param blockS3 the block s 3
     */
    public void setBlockS3(boolean blockS3) {
        this.blockS3 = blockS3;
    }


    /**
     * Gets dynamo db table.
     *
     * @return the dynamo db table
     */
    public String getDynamoDBTable() {
        return dynamoDBTable;
    }

    /**
     * Sets dynamo db table.
     *
     * @param dynamoDBTable the dynamo db table
     */
    public void setDynamoDBTable(String dynamoDBTable) {
        this.dynamoDBTable = dynamoDBTable;
    }


    /**
     * Gets dynamo read capacity.
     *
     * @return the dynamo read capacity
     */
    public long getDynamoReadCapacity() {
        return dynamoReadCapacity;
    }

    /**
     * Sets dynamo read capacity.
     *
     * @param dynamoReadCapacity the dynamo read capacity
     */
    public void setDynamoReadCapacity(long dynamoReadCapacity) {
        this.dynamoReadCapacity = dynamoReadCapacity;
    }

    /**
     * Gets dynamo write capacity.
     *
     * @return the dynamo write capacity
     */
    public long getDynamoWriteCapacity() {
        return dynamoWriteCapacity;
    }

    /**
     * Sets dynamo write capacity.
     *
     * @param dynamoWriteCapacity the dynamo write capacity
     */
    public void setDynamoWriteCapacity(long dynamoWriteCapacity) {
        this.dynamoWriteCapacity = dynamoWriteCapacity;
    }


    /**
     * Sets healthy host count.
     *
     * @param healthyHostCount the healthy host count
     */
    public void setHealthyHostCount(int healthyHostCount) {
        this.healthyHostCount = healthyHostCount;
    }

    /**
     * Gets compulsory tags.
     *
     * @return the compulsory tags
     */
    public List<Tag> getCompulsoryTags() {
        return compulsoryTags;
    }

    /**
     * Sets compulsory tags.
     *
     * @param compulsoryTags the compulsory tags
     */
    public void setCompulsoryTags(List<Tag> compulsoryTags) {
        this.compulsoryTags = compulsoryTags;
    }

    /**
     * Gets ignore tags.
     *
     * @return the ignore tags
     */
    public List<Tag> getIgnoreTags() {
        return ignoreTags;
    }

    /**
     * Sets ignore tags.
     *
     * @param ignoreTags the ignore tags
     */
    public void setIgnoreTags(List<Tag> ignoreTags) {
        this.ignoreTags = ignoreTags;
    }

    /**
     * Get availaibilityzones string [ ].
     *
     * @return the string [ ]
     */
    public String[] getAvailaibilityzones() {
        return availaibilityzones;
    }

    /**
     * Sets availaibilityzones.
     *
     * @param availaibilityzones the availaibilityzones
     */
    public void setAvailaibilityzones(String[] availaibilityzones) {
        this.availaibilityzones = availaibilityzones;
    }


    @Override
    public String toString() {
        return "ExecutionStateCache{" +
                "instances=" + instances +
                ", unhealthyInstances=" + unhealthyInstances +
                ", stoppedInstances=" + stoppedInstances +
                ", ssmCommandInvocationInstances=" + ssmCommandInvocationInstances +
                ", deregisteredInstances=" + deregisteredInstances +
                ", blockedDomains=" + blockedDomains +
                ", detachedSubnets=" + detachedSubnets +
                ", detachedSecurityGroups=" + detachedSecurityGroups +
                ", ec2Tag='" + ec2Tag + '\'' +
                ", loadBalancerName='" + loadBalancerName + '\'' +
                ", processNames=" + processNames +
                ", healProcess=" + healProcess +
                ", healNetwork=" + healNetwork +
                ", cpuSpiked=" + cpuSpiked +
                ", lastJenkinsPerfBuildNumber=" + lastJenkinsPerfBuildNumber +
                ", healthyHostCount=" + healthyHostCount +
                ", ramDiskFull=" + ramDiskFull +
                ", blockPort=" + blockPort +
                ", portNums=" + portNums +
                ", blockDynamoDB=" + blockDynamoDB +
                ", blockS3=" + blockS3 +
                ", portNum=" + portNum +
                ", dbName='" + dbName + '\'' +
                ", commandId='" + commandId + '\'' +
                ", elastiCacheClusterName='" + elastiCacheClusterName + '\'' +
                ", dbInstances=" + dbInstances +
                ", cacheNodes=" + cacheNodes +
                ", dynamoDBTable='" + dynamoDBTable + '\'' +
                ", dynamoReadCapacity=" + dynamoReadCapacity +
                ", dynamoWriteCapacity=" + dynamoWriteCapacity +
                ", compulsoryTags=" + compulsoryTags +
                ", ignoreTags=" + ignoreTags +
                ", availaibilityzones=" + Arrays.toString(availaibilityzones) +
                '}';
    }


    /**
     * Clear.
     */
    public void clear() {

        instances = new CopyOnWriteArrayList<>();
        unhealthyInstances = new CopyOnWriteArrayList<>();
        detachedSubnets = new CopyOnWriteArrayList<>();
        blockedDomains = new CopyOnWriteArrayList<>();
        dbInstances = new CopyOnWriteArrayList<>();
        cacheNodes = new CopyOnWriteArrayList<>();
        detachedSecurityGroups = new CopyOnWriteArrayList<>();
        ssmCommandInvocationInstances = new CopyOnWriteArrayList<>();
        deregisteredInstances = new CopyOnWriteArrayList<>();
        processNames = new CopyOnWriteArrayList<>();
        ignoreTags = new CopyOnWriteArrayList<>();
        availaibilityzones = null;

        compulsoryTags = null;
        ec2Tag = null;
        loadBalancerName = null;
        dbName = null;
        commandId = null;
        elastiCacheClusterName = null;
        dynamoDBTable = null;

        healProcess = false;
        healNetwork = false;
        cpuSpiked = false;
        ramDiskFull = false;
        blockPort = false;
        blockDynamoDB = false;
        blockS3 = false;

        portNum = 0;
        dynamoReadCapacity = 0l;
        dynamoWriteCapacity = 0l;
    }

}
