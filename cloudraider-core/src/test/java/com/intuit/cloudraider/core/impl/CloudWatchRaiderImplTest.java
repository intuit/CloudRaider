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

import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.model.*;
import com.intuit.cloudraider.commons.CloudWatchDelegator;
import com.intuit.cloudraider.model.AlarmStateValue;
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

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@PowerMockIgnore({"javax.management.*","javax.net.ssl.*"})
@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@PrepareForTest({CloudWatchRaiderImpl.class, Credentials.class, CloudWatchDelegator.class})
public class CloudWatchRaiderImplTest {

    @Autowired
    private CloudWatchRaiderImpl cwRaiderImplUnderTest;

    @Autowired
    private CloudWatchDelegator cwDelegator;


    private  AmazonCloudWatch amazonCloudWatch;

    /**
     * Sets method.
     *
     * @throws Exception the exception
     */
    @Before
    public void setupMethod() throws Exception{


        amazonCloudWatch = PowerMockito.mock(AmazonCloudWatch.class);
        PowerMockito.when(cwDelegator.getAmazonCloudWatch()).thenReturn(amazonCloudWatch);

        MetricAlarm metricAlarm = new MetricAlarm();
        metricAlarm.setAlarmName("test-alarm");
        metricAlarm.setStateValue("OK");


        LocalDate localDate = LocalDate.now().plusDays(1l);
        Date date = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        metricAlarm.setStateUpdatedTimestamp(date);


        List<MetricAlarm> metricAlarms = new ArrayList<>();
        metricAlarms.add(metricAlarm);
        PowerMockito.when(amazonCloudWatch.describeAlarms(Mockito.anyObject())).thenReturn(PowerMockito.mock(DescribeAlarmsResult.class));
        PowerMockito.when(amazonCloudWatch.describeAlarms(Mockito.anyObject()).getMetricAlarms()).thenReturn(metricAlarms);

        PowerMockito.when(amazonCloudWatch.describeAlarms()).thenReturn(PowerMockito.mock(DescribeAlarmsResult.class));
        PowerMockito.when(amazonCloudWatch.describeAlarms().getMetricAlarms()).thenReturn(metricAlarms);

        GetMetricStatisticsResult result = PowerMockito.mock(GetMetricStatisticsResult.class);;
        PowerMockito.when(amazonCloudWatch.getMetricStatistics(Mockito.anyObject())).thenReturn(result);

        Datapoint datapoint = new Datapoint().withAverage(1.0);
        List<Datapoint> datapoints = new ArrayList<>();
        datapoints.add(datapoint);
        PowerMockito.when(result.getDatapoints()).thenReturn( datapoints);

        List<AlarmHistoryItem> alarmHistoryItems = new ArrayList<>();
        AlarmHistoryItem alarmHistoryItem = new AlarmHistoryItem();
        alarmHistoryItem.setAlarmName("test");
        alarmHistoryItem.setHistorySummary("test summary");

        alarmHistoryItems.add(alarmHistoryItem);

        DescribeAlarmHistoryResult describeAlarmHistoryResult = PowerMockito.mock(DescribeAlarmHistoryResult.class);
        PowerMockito.when(amazonCloudWatch.describeAlarmHistory(Mockito.anyObject())).thenReturn(describeAlarmHistoryResult);
        PowerMockito.when(describeAlarmHistoryResult.getAlarmHistoryItems()).thenReturn(alarmHistoryItems);


    }


    /**
     * Test get cloud watch alarm by name.
     */
    @Test
    public void testGetCloudWatchAlarmByName() {

        List<MetricAlarm> alarms = cwRaiderImplUnderTest.getCloudWatchAlarmByName("test-alarm");
        Assert.assertEquals(alarms.get(0).getAlarmName(),"test-alarm");
    }

    /**
     * Test get cloud watch alarm by name and date.
     */
    @Test
    public void testGetCloudWatchAlarmByNameAndDate() {

        List<MetricAlarm> alarms = cwRaiderImplUnderTest.getCloudWatchAlarmByNameAndDate("test-alarm", new Date());
        Assert.assertEquals(alarms.get(0).getAlarmName(),"test-alarm");
    }


    /**
     * Test is state in alarm.
     */
    @Test
    public void testIsStateInAlarm()
    {
        boolean alarmStatus = cwRaiderImplUnderTest.isStateInAlarm("test-alarm");
        Assert.assertFalse(alarmStatus);

    }

    /**
     * Test get alarm state.
     */
    @Test
    public void testGetAlarmState()
    {
        String alarmState = cwRaiderImplUnderTest.getAlarmState("test-alarm");
        Assert.assertEquals(alarmState, "OK");

    }

    /**
     * Test get elb un healthy host metric.
     */
    @Test
    public void testGetELBUnHealthyHostMetric()
    {
        Double metricValue = cwRaiderImplUnderTest.getELBUnHealthyHostMetric("test-lb");
        Assert.assertEquals(metricValue, new Double (1.0));

    }

    /**
     * Test get elb metric.
     */
    @Test
    public void testGetELBMetric()
    {
        Double metricValue = cwRaiderImplUnderTest.getELBMetric("test-lb", "UnHealthyHost");
        Assert.assertEquals(metricValue, new Double (1.0));

    }

    /**
     * Test get ec 2 metric.
     */
    @Test
    public void testGetEC2Metric()
    {

        Double metricValue = cwRaiderImplUnderTest.getEC2Metric("i-123", "CPUUtilization");
        Assert.assertEquals(metricValue, new Double (1.0));

    }

    /**
     * Test get simple cloud watch metric alarm status.
     */
    @Test
    public void testGetSimpleCloudWatchMetricAlarmStatus()
    {
        AlarmStateValue alarmState = cwRaiderImplUnderTest.getSimpleCloudWatchMetricAlarmStatus("test-alarm");
        Assert.assertEquals(alarmState.name(), "OK");
    }

    /**
     * Test get cloud watch alarm history.
     */
    @Test
    public void testGetCloudWatchAlarmHistory()
    {
        List<AlarmHistoryItem> alarmHistoryItems = cwRaiderImplUnderTest.getCloudWatchAlarmHistory("test-alarm");
        Assert.assertFalse(alarmHistoryItems.isEmpty());
    }

    /**
     * The type Cw balancer impl test context configuration.
     */
    @Configuration
    protected static class CWBalancerImplTestContextConfiguration {

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
        @Bean
        public CloudWatchRaiderImpl cloudWatchRaiderImpl() {
            return new CloudWatchRaiderImpl();
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
