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
 * Object to track Load Balancer heath check path/port/protocol information.
 * <p>
  */
public class HealthCheckTarget {

    /**
     * The enum Ping protocol.
     */
    public enum PingProtocol {
        /**
         * Tcp ping protocol.
         */
        TCP,
        /**
         * Http ping protocol.
         */
        HTTP,
        /**
         * Https ping protocol.
         */
        HTTPS,
        /**
         * Ssl ping protocol.
         */
        SSL;
    }

    private PingProtocol pingProtocol;
    private String pingPath;
    private Integer pingPort;

    /**
     * Gets target.
     *
     * @return the target
     */
    public String getTarget() {
        return getPingProtocol() + ":" + pingPort + getPingPath();
    }


    /**
     * Gets ping path.
     *
     * @return the ping path
     */
    public String getPingPath() {
        return pingPath != null ? pingPath : "";
    }

    /**
     * Sets ping path.
     *
     * @param pingPath the ping path
     */
    public void setPingPath(String pingPath) {
        this.pingPath = pingPath;
    }

    /**
     * Gets ping protocol.
     *
     * @return the ping protocol
     */
    public PingProtocol getPingProtocol() {
        return pingProtocol;
    }

    /**
     * Sets ping protocol.
     *
     * @param pingProtocol the ping protocol
     */
    public void setPingProtocol(PingProtocol pingProtocol) {
        this.pingProtocol = pingProtocol;
    }

    /**
     * Gets ping port.
     *
     * @return the ping port
     */
    public Integer getPingPort() {
        return pingPort;
    }

    /**
     * Sets ping port.
     *
     * @param pingPort the ping port
     */
    public void setPingPort(Integer pingPort) {
        validatePingPort(pingPort);
        this.pingPort = pingPort;
    }

    /**
     * Checks if the port value specified is within the possible range of ports.
     *
     * @param pingPort port number
     */
    private void validatePingPort(int pingPort){
        validateNumRange("pingPort", pingPort, 1, 65535);
    }

    private void validateNumRange(String name, Integer num, int min, int max) {
        if ( num == null || num < min || num > max) throw new IllegalArgumentException( name + " is out of range " + min + " to " + max);
    }

    @Override
    public String toString() {
        return "HealthCheckTarget{" +
                "pingProtocol=" + pingProtocol +
                ", pingPath='" + pingPath + '\'' +
                ", pingPort=" + pingPort +
                '}' + System.lineSeparator() +
                "getTarget() " + getTarget();
    }
}
