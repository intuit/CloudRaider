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

import org.junit.Assert;
import org.junit.Test;

/**
 * The type Health check target test.
 */
public class HealthCheckTargetTest {
    /**
     * The Object under test.
     */
    HealthCheckTarget objectUnderTest = new HealthCheckTarget();

    /**
     * Test get target.
     */
    @Test
    public void testGetTarget() {
        objectUnderTest.setPingPath("/ping/path");
        objectUnderTest.setPingPort(2000);
        objectUnderTest.setPingProtocol(HealthCheckTarget.PingProtocol.HTTPS);

        Assert.assertEquals(objectUnderTest.getTarget(), "HTTPS:2000/ping/path");
    }

    /**
     * Test get target with no ping path.
     */
    @Test
    public void testGetTarget_WithNoPingPath() {
        objectUnderTest.setPingPort(2000);
        objectUnderTest.setPingProtocol(HealthCheckTarget.PingProtocol.HTTPS);

        Assert.assertEquals(objectUnderTest.getTarget(), "HTTPS:2000");
    }

    /**
     * Test get target with ping port out of range.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testGetTarget_WithPingPortOutOfRange() {
        objectUnderTest.setPingPort(65535 + 1);
    }
}
