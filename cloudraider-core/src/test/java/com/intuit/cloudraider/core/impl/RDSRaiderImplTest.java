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

import com.amazonaws.services.rds.AmazonRDS;
import com.amazonaws.services.rds.model.*;
import com.intuit.cloudraider.commons.RDSDelegator;
import com.intuit.cloudraider.core.interfaces.RDSRaider;
import com.intuit.cloudraider.exceptions.InvalidInputDataException;
import com.intuit.cloudraider.exceptions.ResourceNotFoundException;
import com.intuit.cloudraider.model.BasicCredentials;
import com.intuit.cloudraider.model.Credentials;
import com.intuit.cloudraider.model.DBStatus;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The type Rds raider impl test.
 */
@PowerMockIgnore({"javax.management.*","javax.net.ssl.*"})
@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@PrepareForTest({RDSRaiderImpl.class,Credentials.class, RDSDelegator.class})
public class RDSRaiderImplTest {

    @Autowired
    private  RDSRaider rdsRaider;

    @Autowired
    private RDSDelegator rdsDelegator;

    private static AmazonRDS amazonRDS;
    private DBInstance dbInstance;

    /**
     * Sets method.
     *
     * @throws Exception the exception
     */
    @Before
    public void setupMethod() throws Exception{


        amazonRDS = PowerMockito.mock(AmazonRDS.class);
        PowerMockito.when(rdsDelegator.getAmazonRds()).thenReturn(amazonRDS);

        DescribeDBInstancesResult describeDBInstancesResult = PowerMockito.mock(DescribeDBInstancesResult.class);

        PowerMockito.when(rdsDelegator.getAmazonRds().describeDBInstances()).thenReturn(describeDBInstancesResult);
        PowerMockito.when(rdsDelegator.getAmazonRds().describeDBInstances(Mockito.anyObject())).thenReturn(describeDBInstancesResult);

        dbInstance = new DBInstance();
        dbInstance.setAvailabilityZone("us-west-2a");
        dbInstance.setAllocatedStorage(50);
        dbInstance.setDBName("Test-DB");
        dbInstance.setDBInstanceIdentifier("Test-DB");
        dbInstance.setDBInstanceStatus("running");
        dbInstance.setDBInstanceClass("m4.8xl");
        dbInstance.setIops(1000);

        VpcSecurityGroupMembership vpcSecurityGroupMembership = new VpcSecurityGroupMembership();
        vpcSecurityGroupMembership.setVpcSecurityGroupId("sg-123");
        vpcSecurityGroupMembership.setStatus("active");

        Subnet subnet = new Subnet().withSubnetIdentifier("subnet-123");

        DBSubnetGroup dbSubnetGroup = new DBSubnetGroup();
        dbSubnetGroup.setSubnets(Arrays.asList(subnet));
        dbInstance.setDBSubnetGroup(dbSubnetGroup);

        dbInstance.setVpcSecurityGroups(Arrays.asList(vpcSecurityGroupMembership));

        List<DBInstance> dbList = new ArrayList<>();
        dbList.add(dbInstance);

        PowerMockito.when(describeDBInstancesResult.getDBInstances()).thenReturn(dbList);

    }


    /**
     * Test get all db instances.
     */
    @Test
    public void testGetAllDbInstances(){

        List<DBInstance> dbInstances = rdsRaider.getAllDbInstances();
        Assert.assertNotNull(dbInstances);
        Assert.assertEquals(dbInstances.get(0).getDBName(),"Test-DB");
    }

    /**
     * Test get az db instances without ignore db list.
     */
    @Test
    public void testGetAzDbInstancesWithoutIgnoreDBList(){
        List<DBInstance> dbInstances = rdsRaider.getInstanceIdsForAvailabilityZone("us-west-2a",new ArrayList<>() );
        Assert.assertNotNull(dbInstances);
        Assert.assertEquals(dbInstances.get(0).getDBName(),"Test-DB");
    }

    /**
     * Test get az db instances with ignore db list.
     */
    @Test
    public void testGetAzDbInstancesWithIgnoreDBList(){

        List<DBInstance> dbInstances = rdsRaider.getInstanceIdsForAvailabilityZone("us-west-2a", Arrays.asList("Test-DB"));
        Assert.assertNotNull(dbInstances);
        Assert.assertEquals(dbInstances.size(), 0);
    }

    /**
     * Test get instaces status.
     */
    @Test
    public void testGetInstacesStatus() {

        List<DBStatus> statuses = rdsRaider.getInstancesStatus(Arrays.asList("Test-DB"));
        Assert.assertNotNull(statuses);
        Assert.assertEquals(statuses.get(0).getStatus(),"running");


    }

    /**
     * Test get all db instances names.
     */
    @Test
    public void testGetAllDbInstancesNames(){

        List<String> dbInstances = rdsRaider.getAllDbInstanceNames();
        Assert.assertNotNull(dbInstances);
        Assert.assertEquals(dbInstances.get(0),"Test-DB");
    }

    /**
     * Test get db instaces status.
     */
    @Test
    public void testGetDBInstacesStatus() {

        List<String> statuses = rdsRaider.getDBInstancesStatus();
        Assert.assertNotNull(statuses);
        Assert.assertEquals(statuses.get(0),"running");
    }

    /**
     * Test get db instance class.
     */
    @Test
    public void testGetDBInstanceClass() {

        String dbInstanceClass = rdsRaider.getDBInstanceClass("Test-DB");
        Assert.assertEquals(dbInstanceClass,"m4.8xl");
    }

    /**
     * Test get db storage size.
     */
    @Test
    public void testGetDBStorageSize() {

        Integer size = rdsRaider.getDBStorageSize("Test-DB");
        Assert.assertEquals(size,new Integer(50));
    }

    /**
     * Test get iops.
     */
    @Test
    public void testGetIops() {

        Integer iops = rdsRaider.getIops("Test-DB");
        Assert.assertEquals(iops,new Integer(1000));
    }

    /**
     * Test get security groups.
     */
    @Test
    public void testGetSecurityGroups() {

        List<String> securityGroups = rdsRaider.getSecurityGroups("Test-DB");
        Assert.assertEquals(securityGroups.get(0),"sg-123");
    }

    /**
     * Test reboot db instance.
     */
    @Test
    public void testRebootDbInstance() {
        PowerMockito.when(amazonRDS.rebootDBInstance(Mockito.anyObject())).thenReturn(dbInstance);
        rdsRaider.rebootDbInstance("Test-DB");

    }

    /**
     * Test reboot missing db instance.
     */
    @Test (expected = InvalidInputDataException.class)
    public void testRebootMissingDbInstance() {
        rdsRaider.rebootDbInstance("");

    }

    /**
     * Test reboot invalid db instance.
     */
    @Test (expected = ResourceNotFoundException.class)
    public void testRebootInvalidDbInstance() {
        PowerMockito.when(amazonRDS.rebootDBInstance(Mockito.anyObject())).thenReturn(null);
        rdsRaider.rebootDbInstance("blah");

    }

    /**
     * Test reboot db instance with failover.
     */
    @Test
    public void testRebootDbInstanceWithFailover() {
        PowerMockito.when(amazonRDS.rebootDBInstance(Mockito.anyObject())).thenReturn(dbInstance);
        rdsRaider.rebootDbInstanceWithForceFailover("Test-DB");

    }

    /**
     * Test reboot missing db instance with failover.
     */
    @Test (expected = InvalidInputDataException.class)
    public void testRebootMissingDbInstanceWithFailover() {
        rdsRaider.rebootDbInstanceWithForceFailover("");

    }

    /**
     * Test reboot invalid db instance with failover.
     */
    @Test (expected = ResourceNotFoundException.class)
    public void testRebootInvalidDbInstanceWithFailover() {
        PowerMockito.when(amazonRDS.rebootDBInstance(Mockito.anyObject())).thenReturn(null);
        rdsRaider.rebootDbInstanceWithForceFailover("blah");

    }

    /**
     * Test reboot db instances.
     */
    @Test
    public void testRebootDbInstances() {
        PowerMockito.when(amazonRDS.rebootDBInstance(Mockito.anyObject())).thenReturn(dbInstance);
        rdsRaider.rebootDbInstances(Arrays.asList("Test-DB"));

    }

    /**
     * Test reboot missing db instances.
     */
    @Test (expected = InvalidInputDataException.class)
    public void testRebootMissingDbInstances() {
        PowerMockito.when(amazonRDS.rebootDBInstance(Mockito.anyObject())).thenReturn(dbInstance);
        rdsRaider.rebootDbInstances(Arrays.asList(""));
    }


    /**
     * Test stop db instances.
     */
    @Test
    public void testStopDbInstances() {
        PowerMockito.when(amazonRDS.stopDBInstance(Mockito.anyObject())).thenReturn(dbInstance);
        rdsRaider.stopInstances(Arrays.asList("Test-DB"));

    }

    /**
     * Test stop missing db instances.
     */
    @Test (expected = InvalidInputDataException.class)
    public void testStopMissingDbInstances() {
        rdsRaider.stopInstances(new ArrayList<>());
    }

    /**
     * Test stop invalid db instances.
     */
    @Test (expected = ResourceNotFoundException.class)
    public void testStopInvalidDbInstances() {
        PowerMockito.when(amazonRDS.stopDBInstance(Mockito.anyObject())).thenReturn(null);
        rdsRaider.stopInstances(Arrays.asList("blah"));

    }

    /**
     * Test start db instances.
     */
    @Test
    public void testStartDbInstances() {
        PowerMockito.when(amazonRDS.startDBInstance(Mockito.anyObject())).thenReturn(dbInstance);
        rdsRaider.startInstances(Arrays.asList("Test-DB"));

    }

    /**
     * Test start missing db instances.
     */
    @Test (expected = InvalidInputDataException.class)
    public void testStartMissingDbInstances() {
        rdsRaider.startInstances(new ArrayList<>());
    }

    /**
     * Test start invalid db instances.
     */
    @Test (expected = ResourceNotFoundException.class)
    public void testStartInvalidDbInstances() {
        PowerMockito.when(amazonRDS.startDBInstance(Mockito.anyObject())).thenReturn(null);
        rdsRaider.startInstances(Arrays.asList("blah"));

    }

    /**
     * Test detach security group.
     */
    @Test
    public void testDetachSecurityGroup() {
        rdsRaider.detachSecurityGroup("Test-DB", "sg-123");
    }

    /**
     * Test detach unknown security group.
     */
    @Test  (expected = ResourceNotFoundException.class)
    public void testDetachUnknownSecurityGroup() {
        rdsRaider.detachSecurityGroup("Test-DB", "sg-999");
    }

    /**
     * Test detach missing security group.
     */
    @Test  (expected = InvalidInputDataException.class)
    public void testDetachMissingSecurityGroup() {
        rdsRaider.detachSecurityGroup("Test-DB", "");
    }


    /**
     * Test attach security group.
     */
    @Test
    public void testAttachSecurityGroup() {
        rdsRaider.attachSecurityGroup("Test-DB", "sg-123");
    }

    /**
     * Test attach missing security group.
     */
    @Test  (expected = InvalidInputDataException.class)
    public void testAttachMissingSecurityGroup() {
        rdsRaider.attachSecurityGroup("Test-DB", "");
    }


    /**
     * Test detach subnet.
     */
    @Test
    public void testDetachSubnet() {
        rdsRaider.detachSubnet("Test-DB", "subnet-123");
    }

    /**
     * Test detach unknown subnet.
     */
    @Test  (expected = ResourceNotFoundException.class)
    public void testDetachUnknownSubnet() {
        rdsRaider.detachSecurityGroup("Test-DB", "subnet-999");
    }

    /**
     * Test detach missing subnet.
     */
    @Test  (expected = InvalidInputDataException.class)
    public void testDetachMissingSubnet() {
        rdsRaider.detachSecurityGroup("Test-DB", "");
    }


    /**
     * Test attach subnet.
     */
    @Test
    public void testAttachSubnet() {
        rdsRaider.attachSubnet("Test-DB", "subnet-123");
    }

    /**
     * Test attach missing subnet.
     */
    @Test  (expected = InvalidInputDataException.class)
    public void testAttachMissingSubnet() {
        rdsRaider.attachSubnet("Test-DB", "");
    }


    /**
     * Test detach security groups.
     */
    @Test
    public void testDetachSecurityGroups() {
        rdsRaider.detachSecurityGroups("Test-DB", "sg-123");
    }

    /**
     * Test detach unknown security groups.
     */
    @Test  (expected = ResourceNotFoundException.class)
    public void testDetachUnknownSecurityGroups() {
        rdsRaider.detachSecurityGroups("Test-DB", "sg-999");
    }

    /**
     * Test detach missing security groups.
     */
    @Test  (expected = ResourceNotFoundException.class)
    public void testDetachMissingSecurityGroups() {
        rdsRaider.detachSecurityGroups("Test-DB", "");
    }


    /**
     * Test attach security groups.
     */
    @Test
    public void testAttachSecurityGroups() {
        rdsRaider.attachSecurityGroups("Test-DB", "sg-123");
    }

    /**
     * Test modify db storage size.
     */
    @Test
    public void testModifyDbStorageSize() {
        rdsRaider.modifyDbStorageSize("Test-DB", 40);
    }

    /**
     * Test modify db instance class.
     */
    @Test
    public void testModifyDbInstanceClass() {
        rdsRaider.modifyDbInstanceClass("Test-DB", "m4.12xl");
    }

    /**
     * Test modify db iops.
     */
    @Test
    public void testModifyDbIops() {
        rdsRaider.modifyDbIops("Test-DB", 10000);
    }

    /**
     * Testr restore db instance from snaphot.
     */
    @Test
    public void testrRestoreDBInstanceFromSnaphot() {
        rdsRaider.restoreDBInstanceFromSnapshot("Test-DB", "snapshot-123");
    }

    /**
     * Test generate snapshot.
     */
    @Test
    public void testGenerateSnapshot() {
        rdsRaider.generateSnapshot("Test-DB", "snapshot-123");
    }


    /**
     * The type Rds raider impl test context configuration.
     */
    @Configuration
    protected static class RDSRaiderImplTestContextConfiguration {

        /**
         * Rds delegator rds delegator.
         *
         * @return the rds delegator
         */
        @Bean
        public RDSDelegator rdsDelegator() {
            return Mockito.mock(RDSDelegator.class);
        }

        /**
         * Rds raider rds raider.
         *
         * @return the rds raider
         */
        @Bean (name={"rdsRaiderBean"})
        public RDSRaider rdsRaider() {
            return new RDSRaiderImpl();
        }

        /**
         * Credentials credentials.
         *
         * @return the credentials
         */
        @Bean
        public Credentials credentials() {
            return new BasicCredentials();
        }



    }

}
