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

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.*;
import com.intuit.cloudraider.commons.EC2Delegator;
import com.intuit.cloudraider.exceptions.InvalidInputDataException;
import com.intuit.cloudraider.exceptions.ResourceNotFoundException;
import com.intuit.cloudraider.model.BasicCredentials;
import com.intuit.cloudraider.model.Credentials;
import com.intuit.cloudraider.model.EC2InstanceTO;
import com.intuit.cloudraider.model.EC2Status;
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
import java.util.List;
import java.util.Map;


@PowerMockIgnore({"javax.management.*","javax.net.ssl.*"})
@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@PrepareForTest({EC2RaiderImpl.class, Credentials.class, EC2Delegator.class, AmazonEC2.class })
public class EC2RaiderImplTest {


    @Autowired
    private EC2RaiderImpl ec2RaiderImplUnderTest;


    @Autowired
    private  EC2Delegator ec2Delegator;

    private  Instance instance;

    /**
     * Sets method.
     *
     * @throws Exception the exception
     */
    @Before
    public void setupMethod() throws Exception{


        AmazonEC2 amazonEC2 = PowerMockito.mock(AmazonEC2.class);
        PowerMockito.when(ec2Delegator.getEc2()).thenReturn(amazonEC2);

        DescribeInstancesResult describeInstancesResult = PowerMockito.mock(DescribeInstancesResult.class);
        PowerMockito.when(ec2Delegator.getEc2().describeInstances()).thenReturn(describeInstancesResult);
        PowerMockito.when(ec2Delegator.getEc2().describeInstances(Mockito.anyObject())).thenReturn(describeInstancesResult);


        instance = new Instance();
        instance.setInstanceId("1234");
        instance.setState(new InstanceState().withName("Running"));
        instance.setPrivateIpAddress("10.1.1.1");
        instance.setPlacement(new Placement().withAvailabilityZone("us-west-2c"));
        GroupIdentifier sg = new GroupIdentifier();
        sg.setGroupId("sg-123");
        ArrayList securityGroups = new ArrayList();
        securityGroups.add(sg);
        instance.setSecurityGroups(securityGroups);

        Tag tag = new Tag().withKey("Name").withValue("tag1");
        List<Tag> tags = new ArrayList<>();
        tags.add(tag);
        instance.setTags(tags);

        Reservation reservation = new Reservation();

        ArrayList<Instance> instances = new ArrayList();
        instances.add(instance);
        reservation.setInstances(instances);

        ArrayList<Reservation> reservations = new ArrayList<>();
        reservations.add(reservation);

        PowerMockito.when(ec2Delegator.getEc2().describeInstances().getReservations()).thenReturn(reservations);
        PowerMockito.when(ec2Delegator.getEc2().describeInstances(Mockito.anyObject()).getReservations()).thenReturn(reservations);

        DescribeInstanceAttributeResult describeInstanceAttributeResult = PowerMockito.mock(DescribeInstanceAttributeResult.class);
        PowerMockito.when(ec2Delegator.getEc2().describeInstanceAttribute(Mockito.anyObject())).thenReturn(describeInstanceAttributeResult);
        PowerMockito.when(ec2Delegator.getEc2().describeInstanceAttribute(Mockito.anyObject()).getInstanceAttribute()).thenReturn(PowerMockito.mock(InstanceAttribute.class));
        PowerMockito.when(ec2Delegator.getEc2().describeInstanceAttribute(Mockito.anyObject()).getInstanceAttribute().getGroups()).thenReturn(securityGroups);

      //  ec2RaiderImplUnderTest = new EC2RaiderImpl();
    }

    /**
     * Test ec 2 status by id.
     *
     * @throws Exception the exception
     */
    @Test
    public void testEC2StatusById() throws Exception{


        EC2InstanceTO ec2InstanceTO = ec2RaiderImplUnderTest.getEC2InstanceById("1234");

        Assert.assertEquals(ec2InstanceTO.getInstanceId(), "1234");
        Assert.assertEquals(ec2InstanceTO.getPrivateIpAddress(), "10.1.1.1");
        Assert.assertEquals(ec2InstanceTO.getAvailabilityZone(), "us-west-2c");

    }

    /**
     * Test get instances from any tags.
     *
     * @throws Exception the exception
     */
    @Test
    public void testGetInstancesFromAnyTags() throws Exception {

        Tag tag = new Tag().withKey("Name").withValue("tag1");
        List<Tag> tags = new ArrayList<>();
        tags.add(tag);

        List<EC2InstanceTO> ec2InstanceTOs = ec2RaiderImplUnderTest.getInstancesFromAnyTags(tags);

        Assert.assertEquals(1, ec2InstanceTOs.size());

        EC2InstanceTO ec2InstanceTO = ec2InstanceTOs.get(0);

        Assert.assertEquals(ec2InstanceTO.getInstanceId(), "1234");
        Assert.assertEquals(ec2InstanceTO.getPrivateIpAddress(), "10.1.1.1");
        Assert.assertEquals(ec2InstanceTO.getAvailabilityZone(), "us-west-2c");

    }

    /**
     * Test ec 2 status by tag.
     *
     * @throws Exception the exception
     */
    @Test
    public void testEC2StatusByTag() throws Exception{

        EC2InstanceTO ec2InstanceTO = ec2RaiderImplUnderTest.getEC2StatusByTag("tag1");

        Assert.assertEquals(ec2InstanceTO.getInstanceId(), "1234");
        Assert.assertEquals(ec2InstanceTO.getPrivateIpAddress(), "10.1.1.1");
        Assert.assertEquals(ec2InstanceTO.getAvailabilityZone(), "us-west-2c");
     }


    /**
     * Test ec 2 status by invalid tag.
     *
     * @throws Exception the exception
     */
    @Test (expected = ResourceNotFoundException.class)
    public void testEC2StatusByInvalidTag() throws Exception{

        EC2InstanceTO ec2InstanceTO = ec2RaiderImplUnderTest.getEC2StatusByTag("error");

        Assert.assertEquals(ec2InstanceTO.getInstanceId(), "1234");
        Assert.assertEquals(ec2InstanceTO.getPrivateIpAddress(), "10.1.1.1");
        Assert.assertEquals(ec2InstanceTO.getAvailabilityZone(), "us-west-2c");

    }

    /**
     * Test ec 2 status by invalid id.
     *
     * @throws Exception the exception
     */
    @Test (expected = ResourceNotFoundException.class)
    public void testEC2StatusByInvalidId() throws Exception{

        EC2InstanceTO ec2InstanceTO = ec2RaiderImplUnderTest.getEC2InstanceById("999");

    }

    /**
     * Test ec 2 status missing instance id.
     */
    @Test (expected = InvalidInputDataException.class)
    public void testEC2StatusMissingInstanceId() {
        EC2InstanceTO ec2InstanceTO = ec2RaiderImplUnderTest.getEC2InstanceById("");
    }

    /**
     * Test terminate ec 2 instances by id.
     *
     * @throws Exception the exception
     */
    @Test
    public void testTerminateEc2InstancesById() throws Exception{

        TerminateInstancesResult response = PowerMockito.mock(TerminateInstancesResult.class);
        TerminateInstancesRequest request = new TerminateInstancesRequest().withInstanceIds("i-1234");
        PowerMockito.when(ec2Delegator.getEc2().terminateInstances(request)).thenReturn(response);
        ec2RaiderImplUnderTest.terminateEc2InstancesById("i-1234");

    }

    /**
     * Test terminate ec 2 instances by ids.
     *
     * @throws Exception the exception
     */
    @Test
    public void testTerminateEc2InstancesByIds() throws Exception{

        List<String> instanceIds = new ArrayList<>();
        instanceIds.add("i-123");
        instanceIds.add("i-456");
        TerminateInstancesResult response = PowerMockito.mock(TerminateInstancesResult.class);
        TerminateInstancesRequest request = new TerminateInstancesRequest().withInstanceIds(instanceIds);
        PowerMockito.when(ec2Delegator.getEc2().terminateInstances(request)).thenReturn(response);

        ec2RaiderImplUnderTest.terminateEc2InstancesById(instanceIds);

    }

    /**
     * Test terminate ec 2 instances by id array.
     *
     * @throws Exception the exception
     */
    @Test
    public void testTerminateEc2InstancesByIdArray() throws Exception{

        String[] instanceIds = new String[2];
        instanceIds[0]= "i-123";
        instanceIds[1] = "i-456";
        TerminateInstancesResult response = PowerMockito.mock(TerminateInstancesResult.class);
        TerminateInstancesRequest request = new TerminateInstancesRequest().withInstanceIds(instanceIds);
        PowerMockito.when(ec2Delegator.getEc2().terminateInstances(request)).thenReturn(response);

        ec2RaiderImplUnderTest.terminateEc2InstancesById(instanceIds);

    }

    /**
     * Test terminate ec 2 instances by invalid id.
     *
     * @throws Exception the exception
     */
    @Test (expected = AmazonEC2Exception.class)
    public void testTerminateEc2InstancesByInvalidId() throws Exception{

        TerminateInstancesResult response = PowerMockito.mock(TerminateInstancesResult.class);
        TerminateInstancesRequest request = new TerminateInstancesRequest().withInstanceIds("i-999");

        PowerMockito.when(ec2Delegator.getEc2().terminateInstances(request)).thenThrow(new AmazonEC2Exception("The instance ID 'i-999' does not exist"));
        ec2RaiderImplUnderTest.terminateEc2InstancesById("i-999");

    }

    /**
     * Test terminate ec 2 instances by missing id.
     *
     * @throws Exception the exception
     */
    @Test (expected = InvalidInputDataException.class)
    public void testTerminateEc2InstancesByMissingId() throws Exception{
        ec2RaiderImplUnderTest.terminateEc2InstancesById("");
    }


    /**
     * Test terminate ec 2 instances by tags.
     *
     * @throws Exception the exception
     */
    @Test
    public void testTerminateEc2InstancesByTags() throws Exception{

        TerminateInstancesResult response = PowerMockito.mock(TerminateInstancesResult.class);

        DescribeInstancesResult describeInstancesResult = PowerMockito.mock(DescribeInstancesResult.class);
        PowerMockito.when(ec2Delegator.getEc2().describeInstances(Mockito.anyObject())).thenReturn(describeInstancesResult);
        Reservation reservation = new Reservation();

        ArrayList<Instance> instances = new ArrayList();
        instances.add(instance);
        reservation.setInstances(instances);

        ArrayList<Reservation> reservations = new ArrayList<>();
        reservations.add(reservation);

        PowerMockito.when(ec2Delegator.getEc2().describeInstances(Mockito.anyObject()).getReservations()).thenReturn(reservations);
        PowerMockito.when(ec2Delegator.getEc2().terminateInstances(Mockito.anyObject())).thenReturn(response);

        ec2RaiderImplUnderTest.terminateEc2InstancesByTags("service1", "service2");

    }

    /**
     * Test terminate ec 2 instances by wrong tags.
     *
     * @throws Exception the exception
     */
    @Test (expected = ResourceNotFoundException.class)
    public void testTerminateEc2InstancesByWrongTags() throws Exception{

        TerminateInstancesResult response = PowerMockito.mock(TerminateInstancesResult.class);

        DescribeInstancesResult describeInstancesResult = PowerMockito.mock(DescribeInstancesResult.class);
        PowerMockito.when(ec2Delegator.getEc2().describeInstances(Mockito.anyObject())).thenReturn(describeInstancesResult);

        ArrayList<Reservation> reservations = new ArrayList<>();

        PowerMockito.when(ec2Delegator.getEc2().describeInstances(Mockito.anyObject()).getReservations()).thenReturn(reservations);
        PowerMockito.when(ec2Delegator.getEc2().terminateInstances(Mockito.anyObject())).thenReturn(response);

        ec2RaiderImplUnderTest.terminateEc2InstancesByTags("error");

    }

    /**
     * Test terminate ec 2 instances by tags and instance counts.
     *
     * @throws Exception the exception
     */
    @Test
    public void testTerminateEc2InstancesByTagsAndInstanceCounts() throws Exception{

        TerminateInstancesResult response = PowerMockito.mock(TerminateInstancesResult.class);
        PowerMockito.when(ec2Delegator.getEc2().terminateInstances(Mockito.anyObject())).thenReturn(response);

        ec2RaiderImplUnderTest.terminateEc2InstancesByTags("service1",1);
        ec2RaiderImplUnderTest.terminateEc2InstancesByTags("service1",2);

    }

    /**
     * Test get instances by name.
     *
     * @throws Exception the exception
     */
    @Test
    public void testGetInstancesByName() throws Exception {

        List<EC2InstanceTO> ec2InstanceTOList = ec2RaiderImplUnderTest.getInstancesByName("tag1");
        Assert.assertFalse(ec2InstanceTOList.isEmpty());
        Assert.assertEquals(ec2InstanceTOList.get(0).getInstanceId(), "1234");
        Assert.assertEquals(ec2InstanceTOList.get(0).getPrivateIpAddress(), "10.1.1.1");
        Assert.assertEquals(ec2InstanceTOList.get(0).getAvailabilityZone(), "us-west-2c");

    }

    /**
     * Test get instances ids for one tag.
     *
     * @throws Exception the exception
     */
    @Test
    public void testGetInstancesIdsForOneTag() throws Exception{

        List<EC2InstanceTO> ec2InstanceTOList = ec2RaiderImplUnderTest.getInstancesIdsForOneTag("tag1");
        Assert.assertFalse(ec2InstanceTOList.isEmpty());
        Assert.assertEquals(ec2InstanceTOList.get(0).getInstanceId(), "1234");
        Assert.assertEquals(ec2InstanceTOList.get(0).getPrivateIpAddress(), "10.1.1.1");
        Assert.assertEquals(ec2InstanceTOList.get(0).getAvailabilityZone(), "us-west-2c");

    }

    /**
     * Test get ec 2 status by ids.
     *
     * @throws Exception the exception
     */
    @Test
    public void testGetEC2StatusByIds() throws Exception{

        List<String> instanceIds = new ArrayList<>();
        instanceIds.add("1234");
        List<EC2InstanceTO> ec2InstanceTOList = ec2RaiderImplUnderTest.getEC2InstancesByIds(instanceIds);
        Assert.assertFalse(ec2InstanceTOList.isEmpty());
        Assert.assertEquals(ec2InstanceTOList.get(0).getInstanceId(), "1234");
        Assert.assertEquals(ec2InstanceTOList.get(0).getPrivateIpAddress(), "10.1.1.1");
        Assert.assertEquals(ec2InstanceTOList.get(0).getAvailabilityZone(), "us-west-2c");

    }

    /**
     * Test get ec 2 instance ids with compulsory tags for availability zone.
     *
     * @throws Exception the exception
     */
    @Test
    public void testGetEc2InstanceIdsWithCompulsoryTagsForAvailabilityZone() throws Exception{

        Tag tag = new Tag().withKey("Name").withValue("tag1");
        List<Tag> tags = new ArrayList<>();
        tags.add(tag);

        List<EC2InstanceTO> EC2InstanceTOList = ec2RaiderImplUnderTest.getEc2InstanceIdsWithCompulsoryTagsForAvailabilityZone("us-west-2c",null,tags);
        Assert.assertFalse(EC2InstanceTOList.isEmpty());
        Assert.assertEquals(EC2InstanceTOList.get(0).getInstanceId(), "1234");
        Assert.assertEquals(EC2InstanceTOList.get(0).getPrivateIpAddress(), "10.1.1.1");
        Assert.assertEquals(EC2InstanceTOList.get(0).getAvailabilityZone(), "us-west-2c");

    }

    /**
     * Test get ec 2 instance ids withcompulsory tags for availability zone.
     *
     * @throws Exception the exception
     */
    @Test
    public void testGetEc2InstanceIdsWithcompulsoryTagsForAvailabilityZone() throws Exception{

        Tag tag = new Tag().withKey("Name").withValue("tag1");
        List<Tag> tags = new ArrayList<>();
        tags.add(tag);
        
        List<EC2InstanceTO> EC2InstanceTOList = ec2RaiderImplUnderTest.getEc2InstanceIdsWithCompulsoryTagsForAvailabilityZone("us-west-2c",null,tags);
        Assert.assertFalse(EC2InstanceTOList.isEmpty());
        Assert.assertEquals(EC2InstanceTOList.get(0).getInstanceId(), "1234");
        Assert.assertEquals(EC2InstanceTOList.get(0).getPrivateIpAddress(), "10.1.1.1");
        Assert.assertEquals(EC2InstanceTOList.get(0).getAvailabilityZone(), "us-west-2c");

    }

    /**
     * Test get ec 2 instance ids for availability zone.
     *
     * @throws Exception the exception
     */
    @Test
    public void testGetEc2InstanceIdsForAvailabilityZone() throws Exception{

        List<EC2InstanceTO> ec2InstanceTOList = ec2RaiderImplUnderTest.getEc2InstancesForAvailabilityZone("us-west-2c",null);
        Assert.assertFalse(ec2InstanceTOList.isEmpty());
        Assert.assertEquals(ec2InstanceTOList.get(0).getInstanceId(), "1234");
        Assert.assertEquals(ec2InstanceTOList.get(0).getPrivateIpAddress(), "10.1.1.1");
        Assert.assertEquals(ec2InstanceTOList.get(0).getAvailabilityZone(), "us-west-2c");

    }

    /**
     * Test get intances for az.
     *
     * @throws Exception the exception
     */
    @Test
    public void testGetIntancesForAZ() throws Exception{

        List<EC2InstanceTO> ec2InstanceTOList = ec2RaiderImplUnderTest.getInstancesForAZ("tag1", "us-west-2c");
        Assert.assertFalse(ec2InstanceTOList.isEmpty());
        Assert.assertEquals(ec2InstanceTOList.get(0).getInstanceId(), "1234");
        Assert.assertEquals(ec2InstanceTOList.get(0).getPrivateIpAddress(), "10.1.1.1");
        Assert.assertEquals(ec2InstanceTOList.get(0).getAvailabilityZone(), "us-west-2c");

    }

    /**
     * Test get ec 2 instance i ps for availability zone.
     *
     * @throws Exception the exception
     */
    @Test
    public void testGetEc2InstanceIPsForAvailabilityZone() throws Exception{

        List<String> ec2IPList = ec2RaiderImplUnderTest.getEc2InstanceIPsForAvailabilityZone("us-west-2c",null);
        Assert.assertFalse(ec2IPList.isEmpty());
        Assert.assertEquals(ec2IPList.get(0), "10.1.1.1");

    }

    /**
     * Test get intances ips for az.
     *
     * @throws Exception the exception
     */
    @Test
    public void testGetIntancesIpsForAZ() throws Exception{

        List<String> ec2IPList = ec2RaiderImplUnderTest.getInstancesIpsForAZ("tag1","us-west-2c");
        Assert.assertFalse(ec2IPList.isEmpty());
        Assert.assertEquals(ec2IPList.get(0), "10.1.1.1");

    }


    /**
     * Test get instance status object.
     *
     * @throws Exception the exception
     */
    @Test
    public void testGetInstanceStatusObject() throws Exception{

        EC2Status ec2Status = ec2RaiderImplUnderTest.getInstanceStatus("tag1");
        Assert.assertNotNull(ec2Status);
        Assert.assertEquals(ec2Status.getTagName(),"tag1");
        Assert.assertEquals(ec2Status.getStatus().get("1234"),"Running");

    }

    /**
     * Test get instance status.
     *
     * @throws Exception the exception
     */
    @Test
    public void testGetInstanceStatus() throws Exception{

        String ec2Status = ec2RaiderImplUnderTest.getInstanceStatusById("1234");
        Assert.assertEquals(ec2Status,"Running");

    }

    /**
     * Test get ec 2 instance state.
     *
     * @throws Exception the exception
     */
    @Test
    public void testGetEC2InstanceState() throws Exception{

        Map ec2Status = ec2RaiderImplUnderTest.getEC2InstanceState("tag1");
        Assert.assertNotNull(ec2Status);
        Assert.assertEquals(ec2Status.get("1234"),"Running");

    }


    /**
     * Test get security groups.
     *
     * @throws Exception the exception
     */
    @Test
    public void testGetSecurityGroups() throws Exception{

        List<String> securityGroups = ec2RaiderImplUnderTest.getSecurityGroups("1234");
        Assert.assertEquals(securityGroups.get(0),"sg-123");
        Assert.assertNotEquals(securityGroups.get(0),"sg-13");

    }

    /**
     * Test stop ec 2 instances.
     *
     * @throws Exception the exception
     */
    @Test
    public void testStopEc2Instances() throws Exception{
        ec2RaiderImplUnderTest.stopEc2Instances("1234");

    }

    /**
     * Test stop invalid ec 2 instances.
     *
     * @throws Exception the exception
     */
    @Test (expected = InvalidInputDataException.class)
    public void testStopInvalidEc2Instances() throws Exception{
        ec2RaiderImplUnderTest.stopEc2Instances("");

    }

    /**
     * Test restart ec 2 instances.
     *
     * @throws Exception the exception
     */
    @Test
    public void testRestartEc2Instances() throws Exception{
        ec2RaiderImplUnderTest.restartEc2Instances("1234");

    }

    /**
     * Test restart invalid ec 2 instances.
     *
     * @throws Exception the exception
     */
    @Test (expected = InvalidInputDataException.class)
    public void testRestartInvalidEc2Instances() throws Exception{
        ec2RaiderImplUnderTest.restartEc2Instances("");

    }

    /**
     * Test detach security group.
     *
     * @throws Exception the exception
     */
    @Test
    public void testDetachSecurityGroup() throws Exception{

        ec2RaiderImplUnderTest.detachSecurityGroup("1234","sg-123");

    }

    /**
     * Test detach invalid security group.
     *
     * @throws Exception the exception
     */
    @Test (expected = InvalidInputDataException.class)
    public void testDetachInvalidSecurityGroup() throws Exception{

        ec2RaiderImplUnderTest.detachSecurityGroup("1234","sg-99");

    }

    /**
     * Test attach security group.
     *
     * @throws Exception the exception
     */
    @Test
    public void testAttachSecurityGroup() throws Exception{

        ec2RaiderImplUnderTest.attachSecurityGroup("1234","sg-99");

    }

    /**
     * Test attach security grosups.
     *
     * @throws Exception the exception
     */
    @Test
    public void testAttachSecurityGrosups() throws Exception{

        ec2RaiderImplUnderTest.attachSecurityGroups("1234","sg-99", "sg-88");

    }

    /**
     * Test detach security groups.
     *
     * @throws Exception the exception
     */
    @Test
    public void testDetachSecurityGroups() throws Exception{

        ec2RaiderImplUnderTest.detachSecurityGroups("1234","sg-123");

    }

    /**
     * Test detach invalid security groups.
     *
     * @throws Exception the exception
     */
    @Test (expected = InvalidInputDataException.class)
    public void testDetachInvalidSecurityGroups() throws Exception{

        ec2RaiderImplUnderTest.detachSecurityGroups("1234","sg-99","sg-88");

    }

    /**
     * Test get non filtered active instances.
     */
    @Test
    public void testGetNonFilteredActiveInstances() {
        List<EC2InstanceTO> ec2InstanceTOList = ec2RaiderImplUnderTest.getFilteredActiveInstances(new ArrayList<>());
        Assert.assertFalse(ec2InstanceTOList.isEmpty());
        Assert.assertEquals(ec2InstanceTOList.get(0).getInstanceId(), "1234");
        Assert.assertEquals(ec2InstanceTOList.get(0).getPrivateIpAddress(), "10.1.1.1");
        Assert.assertEquals(ec2InstanceTOList.get(0).getAvailabilityZone(), "us-west-2c");
    }


    /**
     * The type Ec 2 raider impl test context configuration.
     */
    @Configuration
    protected static class EC2RaiderImplTestContextConfiguration {

        /**
         * Ec 2 delegator ec 2 delegator.
         *
         * @return the ec 2 delegator
         */
        @Bean
        public EC2Delegator ec2Delegator() {
            return Mockito.mock(EC2Delegator.class);
        }

        /**
         * Ec 2 raider impl under test ec 2 raider.
         *
         * @return the ec 2 raider
         */
        @Bean
        public EC2RaiderImpl ec2RaiderImplUnderTest() {
            return new EC2RaiderImpl();
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
