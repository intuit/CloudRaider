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

import com.amazonaws.services.rds.model.*;
import com.intuit.cloudraider.commons.RDSDelegator;
import com.intuit.cloudraider.core.interfaces.RDSRaider;
import com.intuit.cloudraider.exceptions.InvalidInputDataException;
import com.intuit.cloudraider.model.DBStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * AWS Relational Database Service functionality.
 * <p>
  */
@Component(value="rdsRaiderBean")
public class RDSRaiderImpl implements RDSRaider {

    /**
     * The Logger.
     */
    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private RDSDelegator rdsDelegator;

    /**
     * Instantiates a new Rds raider.
     */
    public RDSRaiderImpl(){
    }

    /**
     * Get all database instances associated with the account.
     *
     * @return list of database instances
     */
    @Override
    public List<DBInstance> getAllDbInstances() {
        return rdsDelegator.getAmazonRds().describeDBInstances().getDBInstances();
    }

    /**
     * Get all database instances that are in the given availability zone and are not ignored.
     *
     * @param availabilityZone      availability zone id
     * @param dbInstanceIdsToIgnore list of database ids
     * @return list of database instances
     */
    @Override
    public List<DBInstance> getInstanceIdsForAvailabilityZone(String availabilityZone, List<String> dbInstanceIdsToIgnore) {
        if (dbInstanceIdsToIgnore == null) throw new InvalidInputDataException("dbInstanceIdsToIgnore can not be null");

        //not using filters because they seem to cause errors : name = db-instance-id, and value = empty array list
        List<DBInstance> dbInstances = rdsDelegator.getAmazonRds().describeDBInstances(new DescribeDBInstancesRequest()).getDBInstances();

        for (Iterator<DBInstance> it = dbInstances.iterator(); it.hasNext(); ) {
            DBInstance dbInstance = it.next();

            if (!dbInstance.getAvailabilityZone().contains(availabilityZone)) {
                it.remove();
            }
            if (dbInstanceIdsToIgnore.contains(dbInstance.getDBInstanceIdentifier())) {
                it.remove();
            }
        }

        return dbInstances;
    }

    /**
     * Get the database statuses for the database instances requested.
     *
     * @param dbInstanceIds list of database ids
     * @return list of database status, one status per database instance
     */
    @Override
    public List<DBStatus> getInstancesStatus(List<String> dbInstanceIds) {
        List<DBInstance> dbInstances = rdsDelegator.getAmazonRds().describeDBInstances(new DescribeDBInstancesRequest()).getDBInstances();
        List<DBStatus> statuses = new ArrayList<DBStatus>();

        for (Iterator<DBInstance> it = dbInstances.iterator(); it.hasNext(); ) {
            DBInstance dbInstance = it.next();
            if (dbInstanceIds.contains(dbInstance.getDBInstanceIdentifier())) {
                statuses.add(new DBStatus(dbInstance.getDBInstanceIdentifier(), dbInstance.getDBInstanceStatus()));
            }
        }
        return statuses;
    }

    /**
     * Stop the database instances with the matching names.
     *
     * @param dbNames list of database names
     */
    @Override
    public void stopInstances(List<String> dbNames) {
        if (dbNames.isEmpty()) {
            throw new InvalidInputDataException("Empty dbNames list");
        }

        for (String dbName : dbNames) {
            DBInstance dbInstance = rdsDelegator.getAmazonRds().stopDBInstance(
                    new StopDBInstanceRequest().withDBInstanceIdentifier(dbName));

            if (dbInstance == null) {
                throw new com.intuit.cloudraider.exceptions.ResourceNotFoundException("Unable to stop DB instance: " + dbName);
            }
        }
    }

    /**
     * Start the database instances with the matching names.
     *
     * @param dbNames list of database names
     */
    @Override
    public void startInstances(List<String> dbNames) {
        if (dbNames.isEmpty()) {
            throw new InvalidInputDataException("Empty dbNames list");
        }

        for (String dbName : dbNames) {
            DBInstance dbInstance = rdsDelegator.getAmazonRds().startDBInstance(
                    new StartDBInstanceRequest().withDBInstanceIdentifier(dbName));
            if (dbInstance == null) {
                throw new com.intuit.cloudraider.exceptions.ResourceNotFoundException("Unable to start DB instance: " + dbName);
            }

        }
    }

    /**
     * Get all database instance names associated with the account.
     *
     * @return list of names
     */
    @Override
    public List<String> getAllDbInstanceNames() {
        List<String> dbInstanceNames = new ArrayList<String>();
        List<DBInstance> dbInstances = rdsDelegator.getAmazonRds().describeDBInstances().getDBInstances();
        dbInstances.forEach(dbInstance -> dbInstanceNames.add(dbInstance.getDBName()));
        return dbInstanceNames;
    }

    /**
     * Get the status of all database instances associated with the account.
     *
     * @return list of statuses
     */
    @Override
    public List<String> getDBInstancesStatus() {
        List<DBInstance> dbInstances = rdsDelegator.getAmazonRds().describeDBInstances().getDBInstances();
        List<String> statuses = new ArrayList<String>();

        dbInstances.forEach(dbInstance -> statuses.add(dbInstance.getDBInstanceStatus()));
        return statuses;
    }

    /**
     * Get the status for the specified database.
     *
     * @param dbName database name
     * @return database status
     */
    @Override
    public String getDBInstanceStatus(String dbName) {
        DescribeDBInstancesResult dbInstancesresult = rdsDelegator.getAmazonRds().describeDBInstances(new DescribeDBInstancesRequest().withDBInstanceIdentifier(dbName));

        return dbInstancesresult.getDBInstances().get(0).getDBInstanceStatus();
    }

    /**
     * Reboot the database instances that match the provided names.
     *
     * @param dbNames database names
     */
    @Override
    public void rebootDbInstances(List<String> dbNames) {
        if (dbNames.isEmpty()) {
            throw new InvalidInputDataException("Empty dbNames list");
        }

        for (String dbName : dbNames) {
            this.rebootDbInstance(dbName);
        }
    }

    /**
     * Reboot the given database instance.
     *
     * @param dbName database name
     */
    @Override
    public void rebootDbInstance(String dbName) {
        if (dbName == null || dbName.isEmpty()) {
            throw new InvalidInputDataException("Null/Empty db name");
        }
        DBInstance dbInstance = rdsDelegator.getAmazonRds().rebootDBInstance(
                new RebootDBInstanceRequest().withDBInstanceIdentifier(dbName));

        if (dbInstance == null) {
            throw new com.intuit.cloudraider.exceptions.ResourceNotFoundException("Unable to reboot DB instance: " + dbName);
        }
    }

    /**
     * Reboot the given database instance with a forced failover.
     *
     * @param dbName database name
     */
    @Override
    public void rebootDbInstanceWithForceFailover(String dbName) {
        if (dbName == null || dbName.isEmpty()) {
            throw new InvalidInputDataException("Null/Empty db name");
        }
        DBInstance dbInstance = rdsDelegator.getAmazonRds().rebootDBInstance(
                new RebootDBInstanceRequest().withDBInstanceIdentifier(dbName).withForceFailover(true));

        if (dbInstance == null) {
            throw new com.intuit.cloudraider.exceptions.ResourceNotFoundException("Unable to reboot DB instance: " + dbName);
        }
    }

    /**
     * Duplicate function of detachSecurityGroups().
     */
    @Deprecated
    public void detachSecurityGroup(String dbName, String securityGroup) {
        if (dbName.isEmpty() || dbName == null) {
            throw new InvalidInputDataException("Empty/Null dbName provided in request");
        } else if (securityGroup.isEmpty() || securityGroup == null) {
            throw new InvalidInputDataException("Empty/Null securityGroup provided in request");
        }
        List<String> securityGroups = this.getSecurityGroups(dbName);

        if (securityGroups != null && securityGroups.contains(securityGroup)) {
            securityGroups.remove(securityGroup);
            rdsDelegator.getAmazonRds().modifyDBInstance(new ModifyDBInstanceRequest().withDBInstanceIdentifier(dbName).withVpcSecurityGroupIds(securityGroups).withApplyImmediately(true));
        } else {
            throw new com.intuit.cloudraider.exceptions.ResourceNotFoundException("Unable to detach SG: " + securityGroup);
        }
    }

    /**
     * Duplicate function of attachSecurityGroups().
     */
    @Deprecated
    public void attachSecurityGroup(String dbName, String securityGroup) {
        if (dbName.isEmpty() || dbName == null) {
            throw new InvalidInputDataException("Empty/Null dbName provided in request");
        } else if (securityGroup.isEmpty() || securityGroup == null) {
            throw new InvalidInputDataException("Empty/Null securityGroup provided in request");
        }

        List<String> securityGroups = this.getSecurityGroups(dbName);

        if (securityGroups != null && !securityGroups.contains(securityGroup)) {
            securityGroups.add(securityGroup);
            rdsDelegator.getAmazonRds().modifyDBInstance(new ModifyDBInstanceRequest().withDBInstanceIdentifier(dbName).withVpcSecurityGroupIds(securityGroups).withApplyImmediately(true));
        }
    }

    /**
     * Get all security groups attached to the specified database instance.
     *
     * @param dbName database name
     * @return list of security group ids
     */
    @Override
    public List<String> getSecurityGroups(String dbName) {
        List<String> secGroupIds = new ArrayList<String>();
        List<VpcSecurityGroupMembership> securityGroupMemberships = rdsDelegator.getAmazonRds().describeDBInstances(new DescribeDBInstancesRequest().withDBInstanceIdentifier(dbName)).getDBInstances().get(0).getVpcSecurityGroups();
        securityGroupMemberships.forEach(securityGroupMembership -> secGroupIds.add(securityGroupMembership.getVpcSecurityGroupId()));
        return secGroupIds;
    }

    /**
     * Get all subnets attached to the specified database instance.
     *
     * @param dbName database name
     * @return list of subnet ids
     */
    @Override
    public List<String> getSubnetIds(String dbName) {
        List<String> subnetIds = new ArrayList<String>();
        List<Subnet> subnets = rdsDelegator.getAmazonRds().describeDBInstances(new DescribeDBInstancesRequest().withDBInstanceIdentifier(dbName)).getDBInstances().get(0).getDBSubnetGroup().getSubnets();
        subnets.forEach(subnet -> subnetIds.add(subnet.getSubnetIdentifier()));
        return subnetIds;
    }

    /**
     * Detach the given security group from the specified database.
     *
     * @param dbName database name
     * @param securityGroups security group ids
     */
    @Override
    public void detachSecurityGroups(String dbName, String... securityGroups) {
        if (dbName.isEmpty() || dbName == null) {
            throw new InvalidInputDataException("Empty/Null dbName provided in request");
        } else if (securityGroups.length <= 0 || securityGroups == null) {
            throw new InvalidInputDataException("Empty/Null securityGroups provided in request");
        }

        List<String> existingSecurityGroups = this.getSecurityGroups(dbName);

        if (existingSecurityGroups != null && existingSecurityGroups.containsAll(Arrays.asList(securityGroups))) {
            existingSecurityGroups.removeAll(Arrays.asList(securityGroups));
            rdsDelegator.getAmazonRds().modifyDBInstance(new ModifyDBInstanceRequest().withDBInstanceIdentifier(dbName).withVpcSecurityGroupIds(existingSecurityGroups));
        } else {
            throw new com.intuit.cloudraider.exceptions.ResourceNotFoundException("Unable to detach SecurityGroups: " + securityGroups);
        }
    }

    /**
     * Attach the given security group to the specified database.
     *
     * @param dbName database name
     * @param securityGroups security group ids
     */
    @Override
    public void attachSecurityGroups(String dbName, String... securityGroups) {
        if (dbName.isEmpty() || dbName == null) {
            throw new InvalidInputDataException("Empty/Null dbName provided in request");
        } else if (securityGroups.length <= 0 || securityGroups == null) {
            throw new InvalidInputDataException("Empty/Null securityGroups provided in request");
        }

        List<String> existingSecurityGroups = this.getSecurityGroups(dbName);

        if (existingSecurityGroups != null && !existingSecurityGroups.containsAll(Arrays.asList(securityGroups))) {
            existingSecurityGroups.addAll(Arrays.asList(securityGroups));
            rdsDelegator.getAmazonRds().modifyDBInstance(new ModifyDBInstanceRequest().withDBInstanceIdentifier(dbName).withVpcSecurityGroupIds(existingSecurityGroups));
        }
    }

    /**
     * Detach the given subnet from the specified database.
     *
     * @param dbName database name
     * @param subnetId subnet id
     */
    @Override
    public void detachSubnet(String dbName, String subnetId) {
        if (dbName.isEmpty() || dbName == null) {
            throw new InvalidInputDataException("Empty/Null dbName provided in request");
        } else if (subnetId.isEmpty() || subnetId == null) {
            throw new InvalidInputDataException("Empty/Null subnetId provided in request");
        }

        List<String> subnetIds = this.getSubnetIds(dbName);

        if (subnetIds != null && subnetIds.contains(subnetId)) {
            subnetIds.remove(subnetId);

            ModifyDBSubnetGroupRequest modifyDBSubnetGroupRequest = new ModifyDBSubnetGroupRequest()
                    .withSubnetIds(subnetIds)
                    .withDBSubnetGroupName(getSubnetGroupName(dbName));
            rdsDelegator.getAmazonRds().modifyDBSubnetGroup(modifyDBSubnetGroupRequest);
        } else {
            throw new com.intuit.cloudraider.exceptions.ResourceNotFoundException("Unable to detach Subnet: " + subnetId);
        }
    }

    /**
     * Attach the given subnet to the specified database.
     *
     * @param dbName database name
     * @param subnetId subnet id
     */
    @Override
    public void attachSubnet(String dbName, String subnetId) {
        if (dbName.isEmpty() || dbName == null) {
            throw new InvalidInputDataException("Empty/Null dbName provided in request");
        } else if (subnetId.isEmpty() || subnetId == null) {
            throw new InvalidInputDataException("Empty/Null subnetId provided in request");
        }

        List<String> subnetIds = this.getSubnetIds(dbName);

        if (subnetIds != null && !subnetIds.contains(subnetId)) {
            subnetIds.add(subnetId);
            ModifyDBSubnetGroupRequest modifyDBSubnetGroupRequest = new ModifyDBSubnetGroupRequest()
                    .withSubnetIds(subnetIds)
                    .withDBSubnetGroupName(getSubnetGroupName(dbName));
            rdsDelegator.getAmazonRds().modifyDBSubnetGroup(modifyDBSubnetGroupRequest);
        }
    }

    /**
     * Restore the given snapshot onto the specified database instance.
     *
     * @param dbName database name
     * @param snapshotId snapshot id
     */
    @Override
    public void restoreDBInstanceFromSnapshot(String dbName, String snapshotId) {
        RestoreDBInstanceFromDBSnapshotRequest restoreDBInstanceFromDBSnapshotRequest = new RestoreDBInstanceFromDBSnapshotRequest()
                .withDBInstanceIdentifier(dbName)
                .withDBSnapshotIdentifier(snapshotId)
                .withDBSubnetGroupName(getSubnetGroupName(dbName));
        try {
            rdsDelegator.getAmazonRds().restoreDBInstanceFromDBSnapshot(restoreDBInstanceFromDBSnapshotRequest);
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }

    /**
     * Update the specified database instance with the new storage size.
     *
     * @param dbName database name
     * @param newSize new storage size
     */
    @Override
    public void modifyDbStorageSize(String dbName, int newSize) {
        ModifyDBInstanceRequest modifyDBInstanceRequest = new ModifyDBInstanceRequest()
                .withDBInstanceIdentifier(dbName)
                .withApplyImmediately(true)
                .withAllocatedStorage(newSize);
        try {
            rdsDelegator.getAmazonRds().modifyDBInstance(modifyDBInstanceRequest);
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }

    /**
     * Modify the specified database instance with the new instance class.
     *
     * @param dbName database name
     * @param dbInstanceClass database instance class
     */
    @Override
    public void modifyDbInstanceClass(String dbName, String dbInstanceClass) {
        ModifyDBInstanceRequest modifyDBInstanceRequest = new ModifyDBInstanceRequest()
                .withDBInstanceIdentifier(dbName)
                .withApplyImmediately(true)
                .withDBInstanceClass(dbInstanceClass);
        try {
            rdsDelegator.getAmazonRds().modifyDBInstance(modifyDBInstanceRequest);

        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }

    /**
     * Modify the specified database instance with the new iops value.
     *
     * @param dbName database name
     * @param iops new input/output operations per second
     */
    @Override
    public void modifyDbIops(String dbName, Integer iops) {
        ModifyDBInstanceRequest modifyDBInstanceRequest = new ModifyDBInstanceRequest()
                .withDBInstanceIdentifier(dbName)
                .withApplyImmediately(true)
                .withIops(iops);
        try {
            rdsDelegator.getAmazonRds().modifyDBInstance(modifyDBInstanceRequest);
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }

    /**
     * Get the specified database instance's class.
     *
     * @param dbName database instance
     * @return database instance class
     */
    @Override
    public String getDBInstanceClass(String dbName) {
        return rdsDelegator.getAmazonRds().describeDBInstances(new DescribeDBInstancesRequest().withDBInstanceIdentifier(dbName))
                .getDBInstances()
                .get(0)
                .getDBInstanceClass();
    }

    /**
     * Get the specified database instance's storage size.
     *
     * @param dbName database instance
     * @return database instance storage size
     */
    @Override
    public Integer getDBStorageSize(String dbName) {
        return rdsDelegator.getAmazonRds().describeDBInstances(new DescribeDBInstancesRequest().withDBInstanceIdentifier(dbName))
                .getDBInstances()
                .get(0)
                .getAllocatedStorage();
    }

    /**
     * Get the specified database instance's iops value.
     *
     * @param dbName database instance
     * @return database instance iops
     */
    @Override
    public Integer getIops(String dbName) {
        return rdsDelegator.getAmazonRds().describeDBInstances(new DescribeDBInstancesRequest().withDBInstanceIdentifier(dbName))
                .getDBInstances()
                .get(0)
                .getIops();
    }

    /**
     * Generate a snapshot with the given name for the specified database instance.
     *
     * @param dbName database instance
     * @param snapshotName new snapshot name
     */
    @Override
    public void generateSnapshot(String dbName, String snapshotName) {
        CreateDBSnapshotRequest createDBSnapshotRequest = new CreateDBSnapshotRequest()
                .withDBInstanceIdentifier(dbName)
                .withDBSnapshotIdentifier(snapshotName);

        try {
            rdsDelegator.getAmazonRds().createDBSnapshot(createDBSnapshotRequest);
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }

    /**
     * Gets the subnet group for the specified database instance.
     *
     * @param dbName database name
     * @return subnet group name
     */
    private String getSubnetGroupName(String dbName) {
        return rdsDelegator.getAmazonRds().describeDBInstances(new DescribeDBInstancesRequest().withDBInstanceIdentifier(dbName))
                .getDBInstances()
                .get(0)
                .getDBSubnetGroup()
                .getDBSubnetGroupName();
    }
}
