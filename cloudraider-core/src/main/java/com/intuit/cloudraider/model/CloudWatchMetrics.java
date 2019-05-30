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

package com.intuit.cloudraider.model;

/**
 * Possible values for CloudWatch metrics.
 */
public enum CloudWatchMetrics {

    /**
     * Unhealthy host count cloud watch metrics.
     */
    UNHEALTHY_HOST_COUNT ("UnHealthyHostCount"),
    /**
     * Healthy host count cloud watch metrics.
     */
    HEALTHY_HOST_COUNT ("HealthyHostCount"),
    /**
     * Cpu utilization cloud watch metrics.
     */
    CPU_UTILIZATION("CPUUtilization");

    private final String metricString;

    private CloudWatchMetrics(String metricString) {
        this.metricString = metricString;
    }

    /**
     * Gets metric name.
     *
     * @return the metric name
     */
    public String getMetricName() {
        return this.metricString;
    }
}
