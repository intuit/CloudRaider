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

package com.intuit.cloudraider.cucumber.steps;

import com.amazonaws.services.ec2.model.Tag;
import com.intuit.cloudraider.commons.EC2Delegator;
import com.intuit.cloudraider.core.impl.EC2RaiderImpl;
import com.intuit.cloudraider.core.impl.SSMRaiderImpl;
import com.intuit.cloudraider.core.interfaces.EC2Raider;
import com.intuit.cloudraider.cucumber.model.ExecutionStateCache;
import com.intuit.cloudraider.cucumber.util.ScriptExecutor;
import com.intuit.cloudraider.model.BasicCredentials;
import com.intuit.cloudraider.model.Credentials;
import com.intuit.cloudraider.model.EC2InstanceTO;
import org.junit.Assert;
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
 * The type Availability zone step definitions test.
 */
@PowerMockIgnore({"javax.management.*","javax.net.ssl.*"})
@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@PrepareForTest({EC2RaiderImpl.class, ScriptExecutor.class, SSMRaiderImpl.class,
        AvailabilityZoneStepDefinitions.class, EnvironmentHealerStepDefinitions.class})
public class AvailabilityZoneStepDefinitionsTest {

    @Autowired
    private AvailabilityZoneStepDefinitions availabilityZoneStepDefinitions;

    @Autowired
    private EC2Raider ec2Raider;


    @Autowired
    private ExecutionStateCache executionStateCache;

//    @Before
//    public void setupMethod() throws Exception {
//
//        availabilityZoneStepDefinitions = new AvailabilityZoneStepDefinitions();
//        executionStateCache = new ExecutionStateCache();
//        availabilityZoneStepDefinitions.setExecutionStateCache(executionStateCache);
//
//    }

    /**
     * Test given az with ignore tags.
     */
    @Test
    public void testGivenAzWithIgnoreTags() {

        executionStateCache.clearInstances();
        PowerMockito.when(ec2Raider.getInstancesFromAnyTags(Mockito.anyList())).thenReturn(createInstances());
        PowerMockito.when(ec2Raider.getEc2InstancesForAvailabilityZone(Mockito.anyString(), Mockito.anyList())).thenReturn(createInstances());

        Assert.assertEquals(0, executionStateCache.getInstances().size());
        availabilityZoneStepDefinitions.givenAzWithIgnoreTags("us-west-2a, us-west-2b", "testkey:testvalue");

        Assert.assertTrue(executionStateCache.getInstances().size() > 0);
    }

    /**
     * Test given az with compulsory tags.
     */
    @Test
    public void testGivenAzWithCompulsoryTags() {
      //  availabilityZoneStepDefinitions.setExecutionStateCache(executionStateCache);

        List<Tag> tags = new ArrayList<>();
        Tag tag = new Tag();
        tag.setKey("testkey");
        tag.setValue("testvalue");
        tags.add(tag);

        List<Tag> ignoreTags = new ArrayList<>();
        tag = new Tag();
        tag.setKey("ignorekey");
        tag.setValue("ignorevalue");
        ignoreTags.add(tag);

        PowerMockito.when(ec2Raider.getEc2InstanceIdsWithCompulsoryTagsForAvailabilityZone("us-west-2a", ignoreTags, tags)).thenReturn(emptyList());
        PowerMockito.when(ec2Raider.getEc2InstanceIdsWithCompulsoryTagsForAvailabilityZone("us-west-2b", ignoreTags, tags)).thenReturn(createSpecificInstance1());

        availabilityZoneStepDefinitions.givenAzWithCompulsoryTags("us-west-2a, us-west-2b", "testkey:testvalue", "ignorekey:ignorevalue");
        Assert.assertEquals(1, executionStateCache.getInstances().size());
    }

    private List<EC2InstanceTO> createInstances() {
        List<Tag> tags = new ArrayList<>();
        Tag tag1 = new Tag();
        tag1.setKey("testkey");
        tag1.setValue("testvalue");
        tags.add(tag1);

        Tag tag2 = new Tag();
        EC2InstanceTO EC2InstanceTO = new EC2InstanceTO();
        EC2InstanceTO.setAvailabilityZone("us-west-2b");
        EC2InstanceTO.setInstanceId("i-1234");
        EC2InstanceTO.setPrivateIpAddress("10.1.1.1");
        EC2InstanceTO.setTags(tags);

        tag2.setKey("ignorekey");
        tag2.setValue("ignorevalue");
        tags.add(tag2);

        EC2InstanceTO EC2InstanceTO2 = new EC2InstanceTO();
        EC2InstanceTO2.setAvailabilityZone("us-west-2a");
        EC2InstanceTO2.setInstanceId("i-4567");
        EC2InstanceTO2.setPrivateIpAddress("10.1.1.2");
        EC2InstanceTO2.setTags(tags);

        return Arrays.asList(EC2InstanceTO, EC2InstanceTO2);
    }

    private List<EC2InstanceTO> createSpecificInstance1() {
        List<Tag> tags = new ArrayList<>();
        Tag tag = new Tag();
        tag.setKey("testkey");
        tag.setValue("testvalue");
        tags.add(tag);

        EC2InstanceTO ec2InstanceTO = new EC2InstanceTO();
        ec2InstanceTO.setAvailabilityZone("us-west-2b");
        ec2InstanceTO.setInstanceId("i-1234");
        ec2InstanceTO.setPrivateIpAddress("10.1.1.1");
        ec2InstanceTO.setTags(tags);

        return Arrays.asList(ec2InstanceTO);
    }

    private List<EC2InstanceTO> emptyList() {
        List<EC2InstanceTO> instances = new ArrayList<>();
        return instances;
    }

    /**
     * The type Az step definition test context configuration.
     */
    @Configuration
    protected static class AZStepDefinitionTestContextConfiguration {

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
         * Ec 2 raider ec 2 raider.
         *
         * @return the ec 2 raider
         */
        @Bean (name={"ec2raiderBean"})
        public EC2Raider ec2Raider() {
            return  Mockito.mock(EC2RaiderImpl.class);
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

        /**
         * Availability zone step definitions availability zone step definitions.
         *
         * @return the availability zone step definitions
         */
        @Bean
        public AvailabilityZoneStepDefinitions availabilityZoneStepDefinitions() {
            return new AvailabilityZoneStepDefinitions();
        }

        /**
         * Execution state cache execution state cache.
         *
         * @return the execution state cache
         */
        @Bean
        public ExecutionStateCache executionStateCache() {
            return new ExecutionStateCache();
        }

    }
}