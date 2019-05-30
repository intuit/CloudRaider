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

import com.intuit.cloudraider.commons.DynamoDBDelegator;
import com.intuit.cloudraider.core.interfaces.SystemRaider;
import com.intuit.cloudraider.model.BasicCredentials;
import com.intuit.cloudraider.model.Credentials;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.UUID;


/**
 * The type Dynamo db raider impl test.
 */
@PowerMockIgnore({"javax.management.*","javax.net.ssl.*"})
@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@PrepareForTest({DynamoDBRaiderImpl.class, Credentials.class, DynamoDBDelegator.class, SystemRaiderImpl.class })
public class DynamoDBRaiderImplTest {

    @Autowired
    private DynamoDBRaiderImpl dynamoDBRaider;

    /**
     * The Rule.
     */
    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Autowired
    private SystemRaider systemRaider;

    /**
     * Sets test method.
     */
    @Before
    public void setupTestMethod() {

    }

    /**
     * Test dynamo db down.
     */
    @Test
    public void testDynamoDBDown() {
        final String ip = UUID.randomUUID().toString();
        final String result = UUID.randomUUID().toString();

        Mockito.when(systemRaider.executeScript(ip, ClassLoader.getSystemResource("blockdynamo.sh").getPath())).thenReturn(result);

        Assert.assertEquals("Unexpected result: ", result, dynamoDBRaider.blockDynamoDB(ip));
    }

    /**
     * Test dynamo db enable.
     */
    @Test
    public void testDynamoDBEnable() {
        final String ip = UUID.randomUUID().toString();
        final String result = UUID.randomUUID().toString();

        Mockito.when(systemRaider.executeScript(ip, ClassLoader.getSystemResource("unblockdynamo.sh").getPath())).thenReturn(result);

        Assert.assertEquals("Unexpected result: ", result, dynamoDBRaider.unblockDynamoDB(ip));
    }

    /**
     * The type Dynamo raider impl test context configuration.
     */
    @Configuration
    protected static class DynamoRaiderImplTestContextConfiguration {

        /**
         * Dynamo db delegator dynamo db delegator.
         *
         * @return the dynamo db delegator
         */
        @Bean
        public DynamoDBDelegator dynamoDBDelegator() {
            return Mockito.mock(DynamoDBDelegator.class);
        }

        /**
         * System raider system raider.
         *
         * @return the system raider
         */
        @Bean (name={"systemRaiderBean"})
        public SystemRaider systemRaider() {
            return Mockito.mock(SystemRaider.class);

        }

        /**
         * Dynamo raider under test dynamo db raider.
         *
         * @return the dynamo db raider
         */
        @Bean
        public DynamoDBRaiderImpl dynamoRaiderUnderTest() {
            return new DynamoDBRaiderImpl();
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
