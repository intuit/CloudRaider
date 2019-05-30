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

import com.intuit.cloudraider.commons.CloudWatchDelegator;
import com.intuit.cloudraider.core.impl.CloudWatchRaiderImpl;
import com.intuit.cloudraider.cucumber.model.ExecutionStateCache;
import com.intuit.cloudraider.model.BasicCredentials;
import com.intuit.cloudraider.model.Credentials;
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


@PowerMockIgnore({"javax.management.*","javax.net.ssl.*"})
@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@PrepareForTest({CloudWatchRaiderImpl.class, CloudWatchStepDefinitions.class})
public class CloudWatchStepDefinitionsTest {


    @Autowired
    private CloudWatchStepDefinitions cloudWatchStepDefinitions;

    @Autowired
    private CloudWatchRaiderImpl cloudWatchRaider;

    @Autowired
    private ExecutionStateCache executionStateCache;

    /**
     * Sets method.
     *
     * @throws Exception the exception
     */
    @Before
    public void setupMethod() throws Exception{

       executionStateCache.clear();
    }

    /**
     * Test given an alarm name.
     */
    @Test
    public void testGivenAnAlarmName()
    {
        cloudWatchStepDefinitions.givenAnAlarmName("alarmName");
    }

    /**
     * Test assert cloud watch alarm state no name.
     */
    @Test
    public void testAssertCloudWatchAlarmStateNoName()
    {
        cloudWatchStepDefinitions.givenAnAlarmName("alarmName");
        PowerMockito.when(cloudWatchRaider.getAlarmState("alarmName")).thenReturn("OK");
        cloudWatchStepDefinitions.assertCloudWatchAlarmState("OK");
    }

    /**
     * Test assert cloud watch alarm state.
     */
    @Test
    public void testAssertCloudWatchAlarmState()
    {
        PowerMockito.when(cloudWatchRaider.getAlarmState("alarmName")).thenReturn("OK");
        cloudWatchStepDefinitions.assertCloudWatchAlarmState("alarmName", "OK");
    }

    /**
     * Test is alarm on.
     */
    @Test
    public void testIsAlarmOn()
    {
        cloudWatchStepDefinitions.givenAnAlarmName("alarmName");
        PowerMockito.when(cloudWatchRaider.getAlarmState("alarmName")).thenReturn("OK");
        cloudWatchStepDefinitions.isAlarmOn();
    }

    /**
     * Test is alarm on exception.
     */
    @Test (expected = RuntimeException.class)
    public void testIsAlarmOnException()
    {
        cloudWatchStepDefinitions.givenAnAlarmName(null);
        PowerMockito.when(cloudWatchRaider.getAlarmState("alarmName")).thenReturn("OK");
        cloudWatchStepDefinitions.isAlarmOn();
    }

    /**
     * Test is alarm off.
     */
    @Test
    public void testIsAlarmOff()
    {
        PowerMockito.when(cloudWatchRaider.getAlarmState(Mockito.anyString())).thenReturn("OFF");
        cloudWatchStepDefinitions.isAlarmOff();
    }

    /**
     * Test is alarm off exception.
     */
    @Test (expected = RuntimeException.class)
    public void testIsAlarmOffException()
    {
        cloudWatchStepDefinitions.givenAnAlarmName(null);
        PowerMockito.when(cloudWatchRaider.getAlarmState("alarmName")).thenThrow(new RuntimeException());
        cloudWatchStepDefinitions.isAlarmOff();
    }


    /**
     * The type Cw step definition test context configuration.
     */
    @Configuration
    protected static class CWStepDefinitionTestContextConfiguration {

        /**
         * Cloud watch delegator cloud watch delegator.
         *
         * @return the cloud watch delegator
         */
        @Bean
        public CloudWatchDelegator cloudWatchDelegator() {
            return Mockito.mock(CloudWatchDelegator.class);
        }

        /**
         * Cloud watch raider cloud watch raider.
         *
         * @return the cloud watch raider
         */
        @Bean (name={"cwRaiderBean"})
        public CloudWatchRaiderImpl cloudWatchRaider() {
            return  Mockito.mock(CloudWatchRaiderImpl.class);
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
         * Cloud watch step definitions cloud watch step definitions.
         *
         * @return the cloud watch step definitions
         */
        @Bean
        public CloudWatchStepDefinitions cloudWatchStepDefinitions() {
            return new CloudWatchStepDefinitions();
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
