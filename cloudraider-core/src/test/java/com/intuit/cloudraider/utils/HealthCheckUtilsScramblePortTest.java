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

package com.intuit.cloudraider.utils;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.runners.Parameterized.Parameters;

/**
 * The type Health check utils scramble port test.
 */
@RunWith(Parameterized.class)
public class HealthCheckUtilsScramblePortTest {

    private int pingPort;

    /**
     * Instantiates a new Health check utils scramble port test.
     *
     * @param pingPort the ping port
     */
    public HealthCheckUtilsScramblePortTest(int pingPort) {
        this.pingPort = pingPort;
    }

    /**
     * Common port numbers collection.
     *
     * @return the collection
     */
    @Parameters
    public static Collection<Object[]> commonPortNumbers() {
        return Arrays.asList(443, 80, 8080, 8081, 8000, 9000).stream()
                .map(port -> new Object[]{port})
                .collect(Collectors.toList());
    }

    /**
     * Test scramble ping port.
     */
    @Test
    public void testScramblePingPort() {
        Assert.assertFalse(HealthCheckUtils.isPingPortScrambled(pingPort));
        int scrambledPingPort = HealthCheckUtils.scramblePingPort(pingPort);
        Assert.assertTrue(HealthCheckUtils.isPingPortScrambled(scrambledPingPort));

        int unscrambledPingPort = HealthCheckUtils.unscramblePingPort(scrambledPingPort);
        Assert.assertFalse(HealthCheckUtils.isPingPortScrambled(unscrambledPingPort));
        Assert.assertEquals(pingPort, unscrambledPingPort);
    }
}
