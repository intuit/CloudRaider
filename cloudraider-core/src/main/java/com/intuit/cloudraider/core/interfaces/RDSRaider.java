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

import com.amazonaws.services.rds.model.DBInstance;
import com.intuit.cloudraider.model.DBStatus;

import java.util.List;

/**
 * AWS Relational Database Service functionality.
 * <p>
  */
public interface RDSRaider {

    /**
     * Get all database instances associated with the account.
     *
     * @return list of database instances
     */
    public List<DBInstance> getAllDbInstances();

    /**
     * Get all database instances that are in the given availability zone and are not ignored.
     *
     * @param availabilityZone      availability zone id
     * @param dbInstanceIdsToIgnore list of database ids
     * @return list of database instances
     */
    public List<DBInstance> getInstanceIdsForAvailabilityZone(String availabilityZone, List<String> dbInstanceIdsToIgnore);

    /**
     * Get the database statuses for the database instances requested.
     *
     * @param dbInstanceIds list of database ids
     * @return list of database status, one status per database instance
     */
    public List<DBStatus> getInstancesStatus(List<String> dbInstanceIds);

    /**
     * Get all database instance names associated with the account.
     *
     * @return list of names
     */
    public List<String> getAllDbInstanceNames();

    /**
     * Get the status of all database instances associated with the account.
     *
     * @return list of statuses
     */
    public List<String> getDBInstancesStatus();

    /**
     * Get all security groups attached to the specified database instance.
     *
     * @param dbName database name
     * @return list of security group ids
     */
    public List<String> getSecurityGroups(String dbName);

    /**
     * Get all subnets attached to the specified database instance.
     *
     * @param dbName database name
     * @return list of subnet ids
     */
    public List<String> getSubnetIds(String dbName);

    /**
     * Get the status for the specified database.
     *
     * @param dbName database name
     * @return database status
     */
    public String getDBInstanceStatus(String dbName);

    /**
     * Get the specified database instance's class.
     *
     * @param dbName database instance
     * @return database instance class
     */
    public String getDBInstanceClass(String dbName);

    /**
     * Get the specified database instance's storage size.
     *
     * @param dbName database instance
     * @return database instance storage size
     */
    public Integer getDBStorageSize(String dbName);

    /**
     * Get the specified database instance's iops value.
     *
     * @param dbName database instance
     * @return database instance iops
     */
    public Integer getIops(String dbName);

    /**
     * Reboot the given database instance.
     *
     * @param dbName database name
     */
    public void rebootDbInstance(String dbName);

    /**
     * Duplicate function of detachSecurityGroups().
     *
     * @param dbName        the db name
     * @param securityGroup the security group
     */
    @Deprecated
    public void detachSecurityGroup(String dbName, String securityGroup);

    /**
     * Duplicate function of attachSecurityGroups().
     *
     * @param dbName        the db name
     * @param securityGroup the security group
     */
    @Deprecated
    public void attachSecurityGroup(String dbName, String securityGroup);

    /**
     * Detach the given subnet from the specified database.
     *
     * @param dbName   database name
     * @param subnetId subnet id
     */
    public void detachSubnet(String dbName, String subnetId);

    /**
     * Attach the given subnet to the specified database.
     *
     * @param dbName   database name
     * @param subnetId subnet id
     */
    public void attachSubnet(String dbName, String subnetId);

    /**
     * Detach the given security group from the specified database.
     *
     * @param dbName         database name
     * @param securityGroups security group ids
     */
    public void detachSecurityGroups(String dbName, String... securityGroups);

    /**
     * Attach the given security group to the specified database.
     *
     * @param dbName         database name
     * @param securityGroups security group ids
     */
    public void attachSecurityGroups(String dbName, String... securityGroups);

    /**
     * Restore the given snapshot onto the specified database instance.
     *
     * @param dbName     database name
     * @param snapshotId snapshot id
     */
    public void restoreDBInstanceFromSnapshot(String dbName , String snapshotId);

    /**
     * Update the specified database instance with the new storage size.
     *
     * @param dbName  database name
     * @param newSize new storage size
     */
    public void modifyDbStorageSize(String dbName, int newSize);

    /**
     * Modify the specified database instance with the new instance class.
     *
     * @param dbName          database name
     * @param dbInstanceClass database instance class
     */
    public void modifyDbInstanceClass(String dbName, String dbInstanceClass);

    /**
     * Generate a snapshot with the given name for the specified database instance.
     *
     * @param dbName       database instance
     * @param snapshotName new snapshot name
     */
    public void generateSnapshot(String dbName, String snapshotName);

    /**
     * Modify the specified database instance with the new iops value.
     *
     * @param dbName database name
     * @param iops   new input/output operations per second
     */
    public void modifyDbIops(String dbName, Integer iops);

    /**
     * Stop the database instances with the matching names.
     *
     * @param dbNames list of database names
     */
    public void stopInstances(List<String> dbNames);

    /**
     * Start the database instances with the matching names.
     *
     * @param dbNames list of database names
     */
    public void startInstances(List<String> dbNames);

    /**
     * Reboot the database instances that match the provided names.
     *
     * @param dbNames database names
     */
    public void rebootDbInstances(List<String> dbNames);

    /**
     * Reboot the given database instance with a forced failover.
     *
     * @param dbName database name
     */
    public void rebootDbInstanceWithForceFailover(String dbName);
}
