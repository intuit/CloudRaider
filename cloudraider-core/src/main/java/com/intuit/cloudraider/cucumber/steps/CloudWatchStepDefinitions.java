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

import com.intuit.cloudraider.core.interfaces.CloudWatchRaider;
import com.intuit.cloudraider.cucumber.model.ExecutionStateCache;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.testng.Assert;

/**
 * Cucumber Step Definitions for CloudWatch functionality.
 */
public class CloudWatchStepDefinitions {

    @Autowired
    @Qualifier("cwRaiderBean")
    private CloudWatchRaider cloudWatchRaider;
    private String alarmName;

    /**
     * The Logger.
     */
    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ExecutionStateCache executionStateCache;

    public ExecutionStateCache getExecutionStateCache() {
        return executionStateCache;
    }

    public void setExecutionStateCache(ExecutionStateCache executionStateCache) {
        this.executionStateCache = executionStateCache;
    }

    public CloudWatchStepDefinitions() {

    }

    /**
     * Sets CloudWatch alarm to observe.
     *
     * @param alarmName String alarm name
     */
    @Given("^CloudWatch Alarm \"([^\"]*)\"$")
    public CloudWatchStepDefinitions givenAnAlarmName(String alarmName) {
        this.alarmName = alarmName;
        return this;
    }

    /**
     * Assert that alarm state for previously set alarm matches what is expected.
     *
     * @param expectedState String expected alarm state
     */
    @Then("^assertCW alarm = \"([^\"]*)\"$")
    public void assertCloudWatchAlarmState(String expectedState) {
        Assert.assertEquals(cloudWatchRaider.getAlarmState(alarmName), expectedState);
    }

    /**
     * Assert that alarm state for provided alarm matches what is expected.
     *
     * @param alarm         String alarm name
     * @param expectedState String expected alarm state
     */
    @Then("^assertCW alarm \"([^\"]*)\" = \"([^\"]*)\"$")
    public void assertCloudWatchAlarmState(String alarm, String expectedState) {
        Assert.assertEquals(cloudWatchRaider.getAlarmState(alarm), expectedState);
    }

    /**
     * Returns true if the previously set alarm is active.
     *
     * @return true if alarm is active; false otherwise
     */
    @Then("^assertCW alarm is on$")
    public boolean isAlarmOn() {
        if (alarmName != null) {
            return cloudWatchRaider.isStateInAlarm(alarmName);
        }

        throw new RuntimeException("Alarm Name is not set");
    }

    /**
     * Returns true if the previously set alarm is inactive.
     *
     * @return true if alarm is inactive; false otherwise
     */
    @Then("^assertCW alarm is off$")
    public boolean isAlarmOff() {
        if (alarmName != null) {
            return !cloudWatchRaider.isStateInAlarm(alarmName);
        }

        throw new RuntimeException("Alarm Name is not set");
    }


    /**
     * Makes sure that there are no alarms currently triggered ("ALARM" state.)
     */
    @Given("^check CloudWatch alarms state$")
    public CloudWatchStepDefinitions checkAlarmState() {
        Integer count = cloudWatchRaider.countAlarmsInState("ALARM");
        if (count != 0) {
            throw new RuntimeException("Some alarm is triggered and set in ALARM state.");
        }
        return this;
    }

}
