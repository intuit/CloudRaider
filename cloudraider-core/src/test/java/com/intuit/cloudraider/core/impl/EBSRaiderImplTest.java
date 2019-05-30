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
import com.intuit.cloudraider.model.BasicCredentials;
import com.intuit.cloudraider.model.Credentials;
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
@PrepareForTest({EBSRaiderImpl.class,Credentials.class, EC2Delegator.class})
public class EBSRaiderImplTest {


    @Autowired
    private  EBSRaiderImpl ebsRaiderImplUnderTest;

    @Autowired
    private  EC2Delegator ec2Delegator;


    private  Instance instance;

    private DescribeVolumesResult describeVolumesResult;


    /**
     * Sets method.
     *
     * @throws Exception the exception
     */
    @Before
    public void setupMethod() throws Exception{

        AmazonEC2 amazonEC2 = PowerMockito.mock(AmazonEC2.class);
        PowerMockito.when(ec2Delegator.getEc2()).thenReturn(amazonEC2);


        DetachVolumeResult detachVolumeResult = PowerMockito.mock(DetachVolumeResult.class);
        PowerMockito.when(ec2Delegator.getEc2().detachVolume(Mockito.anyObject())).thenReturn(detachVolumeResult);

        AttachVolumeResult attachVolumeResult = PowerMockito.mock(AttachVolumeResult.class);
        PowerMockito.when(ec2Delegator.getEc2().attachVolume(Mockito.anyObject())).thenReturn(attachVolumeResult);

        describeVolumesResult = PowerMockito.mock(DescribeVolumesResult.class);
        PowerMockito.when(ec2Delegator.getEc2().describeVolumes(Mockito.anyObject())).thenReturn(describeVolumesResult);

        instance = new Instance();
        instance.setInstanceId("1234");
        instance.setState(new InstanceState().withName("Running"));

    }


    /**
     * Testdetach ebs volume.
     *
     * @throws Exception the exception
     */
    @Test
    public void testdetachEbsVolume() throws Exception{

        ebsRaiderImplUnderTest.detachEbsVolume("123");

    }


    /**
     * Testdetach ebs invalid volume.
     *
     * @throws Exception the exception
     */
    @Test(expected=InvalidInputDataException.class)
    public void testdetachEbsInvalidVolume() throws Exception{

        ebsRaiderImplUnderTest.detachEbsVolume("");

    }

    /**
     * Testattach ebs volume.
     *
     * @throws Exception the exception
     */
    @Test
    public void testattachEbsVolume() throws Exception{

        ebsRaiderImplUnderTest.attachEbsVolume("1234","root", "123");

    }


    /**
     * Testget volumes state.
     *
     * @throws Exception the exception
     */
    @Test
    public void testgetVolumesState() throws Exception{

        List<Volume> volumes = new ArrayList<Volume>();
        Volume volume = new Volume();
        volume.setVolumeId("123");
        volume.setState("attached");

        volumes.add(volume);

        PowerMockito.when(describeVolumesResult.getVolumes()).thenReturn(volumes);

        Map volumeState = ebsRaiderImplUnderTest.getVolumesState("123");

        Assert.assertNotNull(volumeState);
        Assert.assertEquals(volumeState.get("123"),"attached");

    }

    /**
     * Testget volumes for given instance id.
     *
     * @throws Exception the exception
     */
    @Test
    public void testgetVolumesForGivenInstanceId() throws Exception{

        List<Volume> volumes = ebsRaiderImplUnderTest.getVolumesForGivenInstanceId("1234");

        Assert.assertTrue(volumes.isEmpty());

        volumes = new ArrayList<Volume>();
        Volume volume = new Volume();
        volume.setVolumeId("123");
        volume.setState("attached");

        volumes.add(volume);

        PowerMockito.when(describeVolumesResult.getVolumes()).thenReturn(volumes);

        Assert.assertFalse(volumes.isEmpty());
        Assert.assertEquals(volumes.get(0).getVolumeId(), "123");
        Assert.assertEquals(volumes.get(0).getState(), "attached");

    }


    /**
     * The type Ebs raider impl test context configuration.
     */
    @Configuration
    protected static class EBSRaiderImplTestContextConfiguration {

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
         * Ebs raider impl under test ebs raider.
         *
         * @return the ebs raider
         */
        @Bean
        public EBSRaiderImpl ebsRaiderImplUnderTest() {
            return new EBSRaiderImpl();
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
