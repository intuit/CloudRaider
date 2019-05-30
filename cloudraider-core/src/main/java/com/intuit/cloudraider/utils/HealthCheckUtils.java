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

import com.amazonaws.services.elasticloadbalancing.model.HealthCheck;
import com.intuit.cloudraider.model.HealthCheckTarget;

/**
 * Utilities class for load balancer health checks.
 */
public class HealthCheckUtils {
    private static final int PING_PORT_SCRAMBLE_MULTIPLIER = 7;
    /**
     * The constant MAX_TCPIP_PORT_NUMBER.
     */
    public static final int MAX_TCPIP_PORT_NUMBER = 65536;

    /**
     * Finds the port to ping from the given health check.
     *
     * @param originalHealthCheck HealthCheck
     * @return port to ping
     */
    public static int toPingPort(HealthCheck originalHealthCheck) {
        return Integer.parseInt(originalHealthCheck.getTarget().split(":")[1].split("/")[0]);
    }

    /**
     * Finds the protocol to ping from the given health check.
     *
     * @param originalHealthCheck HealthCheck
     * @return PingProtocol enum value
     */
    public static HealthCheckTarget.PingProtocol toPingProtocol(HealthCheck originalHealthCheck) {
        return HealthCheckTarget.PingProtocol.valueOf(originalHealthCheck.getTarget().split(":")[0]);
    }

    /**
     * Parses protocol to its corresponding PingProtocol.
     *
     * @param protocol protocol
     * @return PingProtocol enum value
     */
    public static HealthCheckTarget.PingProtocol toPingProtocol(String protocol) {
        return HealthCheckTarget.PingProtocol.valueOf(protocol);
    }

    /**
     * Finds the path to ping from the given health check.
     *
     * @param originalHealthCheck HealthCheck
     * @return path to ping
     */
    public static String toPingPath(HealthCheck originalHealthCheck) {
        return originalHealthCheck.getTarget().split(":")[1].replaceAll("[0-9]","");
    }

    /**
     * Deep copy of HealthCheckTarget object.
     *
     * @param healthCheckTarget HealthCheckTarget object to copy
     * @return deep copy of HealthCheckTarget object
     */
    public static HealthCheckTarget copy(HealthCheckTarget healthCheckTarget) {
        HealthCheckTarget copy = new HealthCheckTarget();
        copy.setPingPath(healthCheckTarget.getPingPath());
        copy.setPingProtocol(healthCheckTarget.getPingProtocol());
        copy.setPingPort(healthCheckTarget.getPingPort());
        return copy;
    }

    /**
     * scrambles ping port in a deterministic manner.
     *
     * @param pingPort port number
     * @return new port number
     */
    public static int scramblePingPort(int pingPort) {
        if ((pingPort * PING_PORT_SCRAMBLE_MULTIPLIER) > MAX_TCPIP_PORT_NUMBER) throw new IllegalArgumentException("Scrambled port number is too high for TCP/IP " + (pingPort * HealthCheckUtils.PING_PORT_SCRAMBLE_MULTIPLIER));
        return pingPort * PING_PORT_SCRAMBLE_MULTIPLIER;
    }

    /**
     * de-scrambles the ping port created by this.scramblePingPort.
     *
     * @param pingPort port number
     * @return new port number
     */
    public static int unscramblePingPort(int pingPort) {
        return pingPort / PING_PORT_SCRAMBLE_MULTIPLIER;
    }

    /**
     * determines if a ping port is scrambled, it is possible for this to be wrong, but unlikely.
     *
     * @param pingPort port number
     * @return true if port is scrambled; false otherwise
     */
    public static boolean isPingPortScrambled(int pingPort) {
        return pingPort % PING_PORT_SCRAMBLE_MULTIPLIER == 0;
    }
}
