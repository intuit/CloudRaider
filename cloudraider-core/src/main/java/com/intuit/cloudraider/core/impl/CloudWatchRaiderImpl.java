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


import com.amazonaws.services.cloudwatch.model.*;
import com.intuit.cloudraider.commons.CloudWatchDelegator;
import com.intuit.cloudraider.core.interfaces.CloudWatchRaider;
import com.intuit.cloudraider.model.AlarmStateValue;
import com.intuit.cloudraider.model.CloudWatchMetricNamespace;
import com.intuit.cloudraider.model.CloudWatchMetrics;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * AWS Cloud Watch functionality.
 */
@Component(value="cwRaiderBean")
public class CloudWatchRaiderImpl implements CloudWatchRaider {

    @Autowired
    private CloudWatchDelegator cloudWatchDelegator;


    /**
     * The Logger.
     */
    Logger logger = LoggerFactory.getLogger(this.getClass());
    /**
     * Instantiates a new Cloud watch raider.
     */
    public CloudWatchRaiderImpl() {
    }

    /**
     * Get all alarms that match the provided alarm name prefix.
     *
     * @param alarmName alarm name prefix
     * @return list of alarms
     */
    @Override
    public List<MetricAlarm> getCloudWatchAlarmByName(String alarmName) {

        return cloudWatchDelegator.getAmazonCloudWatch().describeAlarms(new DescribeAlarmsRequest().withAlarmNamePrefix(alarmName))
                .getMetricAlarms();

    }

    /**
     * Checks if any alarms with the matching name are currenlty in "ALARM" state.
     *
     * @param alarmName alarm name
     * @return true if any of the matching alarms are in "ALARM" state; false if no alarms are active
     */
    @Override
    public boolean isStateInAlarm(String alarmName) {
        return !getCloudWatchAlarmByName(alarmName)
                .stream()
                .filter(x -> x.getStateValue().equalsIgnoreCase("ALARM"))
                .collect(Collectors.toList())
                .isEmpty();
    }

    /**
     * Get all alarms that contain the alarm name and were last updated after the provided date.
     *
     * @param alarmName alarm name
     * @param afterDate earliest creation date
     * @return list of alarms
     */
    @Override
    public List<MetricAlarm> getCloudWatchAlarmByNameAndDate(String alarmName, Date afterDate) {

        return cloudWatchDelegator.getAmazonCloudWatch().describeAlarms()
                .getMetricAlarms()
                .stream()
                .filter(x -> x.getAlarmName().toLowerCase().contains(alarmName.toLowerCase()) && x.getStateUpdatedTimestamp().after(afterDate))
                .collect(Collectors.toList());
    }

    /**
     * Get the number of unhealhty hosts attached to the provided ELB.
     *
     * @param loadBalancerName load balancer name
     * @return number of unhealthy ELB hosts
     */
    @Override
    public Double getELBUnHealthyHostMetric(String loadBalancerName) {

        return getELBMetric(loadBalancerName, CloudWatchMetrics.UNHEALTHY_HOST_COUNT.getMetricName());

    }

    /**
     * Calculates the average metric for the provided load balancer.
     *
     * @param loadBalancerName name of load balancer
     * @param metricName metric to analyze
     * @return calculated metric
     */
    @Override
    public Double getELBMetric(String loadBalancerName, String metricName) {

        return this.getMetric("LoadBalancerName",
                loadBalancerName,
                "Average",
                CloudWatchMetricNamespace.ELB.getNamespace(),
                metricName);

    }

    /**
     * Calculates the average metric for the provided instance.
     *
     * @param instanceId instance id
     * @param metricName metric to analyze
     * @return calculated metric
     */
    @Override
    public Double getEC2Metric(String instanceId, String metricName) {

        return this.getMetric("InstanceId",
                instanceId,
                "Average",
                CloudWatchMetricNamespace.EC2.getNamespace(),
                metricName);

    }

    /**
     * Helper function to calculate metric given necessary informaiton.
     *
     * @param dimensionName dimension name
     * @param dimensionValue dimension value
     * @param statisticType type of mathematical analysis (average, etc.)
     * @param namespace namespace
     * @param metricName metric to analyze
     * @return calculated metric
     */
    private Double getMetric(String dimensionName, String dimensionValue, String statisticType, String namespace, String metricName) {

        Double result = 0.0;

        HashSet<Dimension> dimensions = new HashSet<Dimension>();
        Dimension loadBalancerDimension = new Dimension();
        loadBalancerDimension
                .setName(dimensionName);
        loadBalancerDimension.setValue(dimensionValue);
        dimensions.add(loadBalancerDimension);

        HashSet<String> statistics = new HashSet<String>();
        statistics.add(statisticType);

        Date currentTime = new DateTime(DateTimeZone.UTC).toDate();
        Date pastTime = new DateTime(DateTimeZone.UTC).minusSeconds(
                60).toDate();
        GetMetricStatisticsRequest request = new GetMetricStatisticsRequest();
        request.setStartTime(pastTime);
        request.setEndTime(currentTime);
        request.setPeriod(60);
        request.setMetricName(metricName);
        request.setNamespace(namespace);
        request.setStatistics(statistics);
        request.setDimensions(dimensions);

        GetMetricStatisticsResult response = cloudWatchDelegator.getAmazonCloudWatch().getMetricStatistics(request);

        if (!response.getDatapoints().isEmpty()) {
            result = response.getDatapoints().get(0).getAverage();
        }

        return result;
    }

    /**
     * Get the alarm history with the matching alarm name.
     *
     * @param alarmName alarm name
     * @return list of alarm history items
     */
    @Override
    public List<AlarmHistoryItem> getCloudWatchAlarmHistory(String alarmName) {

        return cloudWatchDelegator.getAmazonCloudWatch().describeAlarmHistory(new DescribeAlarmHistoryRequest().withAlarmName(alarmName))
                .getAlarmHistoryItems();

    }

    /**
     * Get the current status of the alarm with the matching name.
     *
     * @param alarmName alarm name
     * @return alarm status (AlarmStateValue)
     */
    @Override
    public AlarmStateValue getSimpleCloudWatchMetricAlarmStatus(String alarmName) {
        List<MetricAlarm> metricAlarms = getCloudWatchAlarmByName(alarmName);
        for (MetricAlarm metricAlarm : metricAlarms) {
            if (metricAlarm.getAlarmName().contains(alarmName)) {
                return AlarmStateValue.valueOf(metricAlarm.getStateValue());
            }
        }
        return AlarmStateValue.NON_EXISTENT;
    }

    /**
     * Get the current status of the alarm with the matching name.
     * @param alarmName alarm value
     * @return alarm state (String)
     */
    @Override
    public String getAlarmState(String alarmName) {
        return getCloudWatchAlarmByName(alarmName)
                .stream()
                .map(MetricAlarm::getStateValue)
                .findFirst()
                .get();
    }

    /**
     * Gets the number of alarms that are currently in the specified state.
     *
     * @param alarmState alarm state
     * @return number of alarms in the provided alarm state
     */
    @Override
    public Integer countAlarmsInState(String alarmState) {
        List<MetricAlarm> alarmMetrics = cloudWatchDelegator.getAmazonCloudWatch().describeAlarms()
                .getMetricAlarms()
                .stream()
                .filter(x -> x.getStateValue().equalsIgnoreCase(alarmState))
                .collect(Collectors.toList());

        return alarmMetrics.size();
    }

}
