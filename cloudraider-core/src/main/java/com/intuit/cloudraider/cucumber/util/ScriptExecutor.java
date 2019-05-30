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

package com.intuit.cloudraider.cucumber.util;

import com.intuit.cloudraider.core.interfaces.SystemRaider;
import com.intuit.cloudraider.model.Actions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * For a specific instance, execute EC2 failures by running a respective script.
 */
@Component(value="scriptExecutor")
public class ScriptExecutor {

    @Autowired
    private SystemRaider systemRaider;

    /**
     * Instantiates a new Script executor.
     */
    public ScriptExecutor() {

    }

    /**
     * Execute process termination.
     *
     * @param ip          the ip
     * @param processName the process name
     */
    public void executeProcessTermination(String ip, String processName) {
        this.executeScript(Actions.KILLPROCESS, ip, processName);
    }

    /**
     * Execute disk full.
     *
     * @param ip         the ip
     * @param volumeType the volume type
     * @param size       the size
     */
    public void executeDiskFull(String ip, String volumeType, int size) {
        this.executeScript(Actions.DISKFULL, ip, volumeType, String.valueOf(size));
    }

    /**
     * Execute cpu spike.
     *
     * @param ip    the ip
     * @param cores the cores
     */
    public void executeCPUSpike(String ip, int cores) {
        this.executeScript(Actions.SPIKECPU, ip, String.valueOf(cores), "3");
    }

    /**
     * Execute stop process.
     *
     * @param ip          the ip
     * @param processName the process name
     */
    public void executeStopProcess(String ip, String processName) {
        this.executeScript(Actions.STOPSERVICE, ip, processName);
    }

    /**
     * Execute start process.
     *
     * @param ip          the ip
     * @param processName the process name
     */
    public void executeStartProcess(String ip, String processName) {
        this.executeScript(Actions.STARTSERVICE, ip, processName);
    }

    /**
     * Execute random network latency.
     *
     * @param ip              the ip
     * @param upperRangeDelay the upper range delay
     * @param lowerRangeDelay the lower range delay
     */
    public void executeRandomNetworkLatency(String ip, String upperRangeDelay, String lowerRangeDelay) {
        this.executeScript(Actions.DELAYNETWORK, ip, upperRangeDelay, lowerRangeDelay);
    }

    /**
     * Execute network latency.
     *
     * @param ip    the ip
     * @param delay the delay
     */
    public void executeNetworkLatency(String ip, String delay) {
        this.executeScript(Actions.DELAYNETWORK, ip, delay);
    }

    /**
     * Execute random domain network latency.
     *
     * @param ip              the ip
     * @param upperRangeDelay the upper range delay
     * @param lowerRangeDelay the lower range delay
     * @param domainName      the domain name
     */
    public void executeRandomDomainNetworkLatency( String ip, String upperRangeDelay, String lowerRangeDelay, String domainName)
    {
        this.executeScript(Actions.DELAYDOMAINNETWORK, ip, upperRangeDelay, lowerRangeDelay, domainName);
    }


    /**
     * Execute network packet loss.
     *
     * @param ip             the ip
     * @param lossPercentage the loss percentage
     */
    public void executeNetworkPacketLoss(String ip, String lossPercentage) {
        this.executeScript(Actions.PACKETLOSS, ip, lossPercentage);
    }

    /**
     * Execute block domain.
     *
     * @param ip         the ip
     * @param domainName the domain name
     */
    public void executeBlockDomain(String ip, String domainName) {
        this.executeScript(Actions.BLOCKDOMAIN, ip, domainName);
    }

    /**
     * Execute block port.
     *
     * @param ip      the ip
     * @param portNum the port num
     */
    public void executeBlockPort(String ip, String portNum) {
        this.executeScript(Actions.BLOCKPORT, ip, portNum);
    }


    /**
     * Execute block dynamo db.
     *
     * @param ip the ip
     */
    public void executeBlockDynamoDB(String ip)
    {
        this.executeScript(Actions.BLOCKDYNAMO, ip, null);
    }

    /**
     * Execute block s 3.
     *
     * @param ip the ip
     */
    public void executeBlockS3 (String ip)
    {
        this.executeScript(Actions.BLOCKS3, ip, null);
    }

    /**
     * Execute un block domain.
     *
     * @param ip         the ip
     * @param domainName the domain name
     */
    public void executeUnBlockDomain( String ip, String domainName)
    {
        this.executeScript(Actions.UNBLOCKDOMAIN, ip, domainName);
    }

    /**
     * Executes the script matching the action specified with the given arguments on the instance with the provided ip address.
     *
     * @param action    ENUM action
     * @param ip        ip address of instance
     * @param arguments arguments for the given action
     */
    public void executeScript(Actions action, String ip, String... arguments) {
        String fileName = action.getActionName() + ".sh";
        File file = new File(ClassLoader.getSystemResource(fileName).getFile());

        systemRaider.executeScript(ip, file.getAbsolutePath(), arguments);
    }

    /**
     * Executes the script matching the action specified with the given arguments on the instance with the provided ip address.
     *
     * @param action    String name of the action
     * @param ip        ip address of instance
     * @param arguments arguments for the given action
     */
    public void executeScript(String action, String ip, String... arguments) {
        String fileName = action + ".sh";
        File file = new File(ClassLoader.getSystemResource(fileName).getFile());

        systemRaider.executeScript(ip, file.getAbsolutePath(), arguments);
    }
}