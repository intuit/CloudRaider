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

import cucumber.api.java.en.Then;

/**
 * Cucumber Step Definitions for establishing a waiting period during test execution.
 */
public class DelayStepDefinitions {

    /**
     * Waits a certain number of minutes.
     *
     * @param minutes number of minutes to wait
     * @throws Throwable the throwable
     */
    @Then("^wait for (\\d+) minute$")
    public static void waitInMinutes(int minutes) throws Throwable {
        Thread.sleep(minutes * 60 * 1000);
    }

    /**
     * Waits a certain number of seconds.
     *
     * @param seconds number of seconds
     * @throws Throwable the throwable
     */
    @Then("^wait for (\\d+) seconds$")
    public static void waitInSeconds(long seconds) throws Throwable {
        Thread.sleep(seconds * 1000);
    }


    /**
     * Wait in minutes and seconds.
     *
     * @param minutes the minutes
     * @param seconds the seconds
     * @throws Throwable the throwable
     */
    @Then("^wait for (\\d+) minutes & (\\d+) seconds")
    public static void waitInMinutesAndSeconds(long minutes, long seconds) throws Throwable {
        Thread.sleep(minutes * 60 * 1000);
        Thread.sleep(seconds * 1000);
    }
}
