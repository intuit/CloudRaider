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

import com.amazonaws.services.cloudwatch.model.AlarmHistoryItem;
import com.amazonaws.services.cloudwatch.model.MetricAlarm;
import com.intuit.cloudraider.model.AlarmStateValue;

import java.util.Date;
import java.util.List;

/**
 * AWS Cloud Watch functionality.
 */
public interface CloudWatchRaider {

    /**
     * Get all alarms that match the provided alarm name prefix.
     *
     * @param alarmName alarm name prefix
     * @return list of alarms
     */
    public List<MetricAlarm> getCloudWatchAlarmByName(String alarmName);

    /**
     * Get all alarms that contain the alarm name and were last updated after the provided date.
     *
     * @param alarmName alarm name
     * @param afterDate earliest creation date
     * @return list of alarms
     */
    public List<MetricAlarm> getCloudWatchAlarmByNameAndDate(String alarmName, Date afterDate);

    /**
     * Get the alarm history with the matching alarm name.
     *
     * @param alarmName alarm name
     * @return list of alarm history items
     */
    public List<AlarmHistoryItem> getCloudWatchAlarmHistory(String alarmName);

    /**
     * Get the number of unhealhty hosts attached to the provided ELB.
     *
     * @param loadBalancerName load balancer name
     * @return number of unhealthy ELB hosts
     */
    public Double getELBUnHealthyHostMetric(String loadBalancerName);

    /**
     * Calculates the average metric for the provided load balancer.
     *
     * @param loadBalancerName name of load balancer
     * @param metricName       metric to analyze
     * @return calculated metric
     */
    public Double getELBMetric(String loadBalancerName, String metricName);

    /**
     * Calculates the average metric for the provided instance.
     *
     * @param instanceId instance id
     * @param metricName metric to analyze
     * @return calculated metric
     */
    public Double getEC2Metric(String instanceId, String metricName);

    /**
     * Checks if any alarms with the matching name are currenlty in "ALARM" state.
     *
     * @param alarmName alarm name
     * @return true if any of the matching alarms are in "ALARM" state; false if no alarms are active
     */
    public boolean isStateInAlarm(String alarmName);

    /**
     * Get the current status of the alarm with the matching name.
     *
     * @param alarmName alarm value
     * @return alarm state (String)
     */
    public String getAlarmState(String alarmName);

    /**
     * Get the current status of the alarm with the matching name.
     *
     * @param alarmName alarm name
     * @return alarm status (AlarmStateValue)
     */
    public AlarmStateValue getSimpleCloudWatchMetricAlarmStatus(String alarmName);

    /**
     * Gets the number of alarms that are currently in the specified state.
     *
     * @param alarmState alarm state
     * @return number of alarms in the provided alarm state
     */
    public Integer countAlarmsInState(String alarmState);


}
