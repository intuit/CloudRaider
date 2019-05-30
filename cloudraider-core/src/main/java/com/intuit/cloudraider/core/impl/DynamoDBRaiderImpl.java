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

import com.amazonaws.services.dynamodbv2.model.DescribeTableRequest;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.UpdateTableRequest;
import com.intuit.cloudraider.commons.DynamoDBDelegator;
import com.intuit.cloudraider.core.interfaces.DynamoDBRaider;
import com.intuit.cloudraider.core.interfaces.SystemRaider;
import com.intuit.cloudraider.model.Actions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.net.URL;

/**
 * This class executes the FMEA for DynamoDB.
 * It updates the /etc/hosts for popular dynamoDB addresses to simulate failures.
 */
@Component(value="dynamoRaiderBean")
public class DynamoDBRaiderImpl implements DynamoDBRaider {

    @Autowired
    @Qualifier("systemRaiderBean")
    private SystemRaider systemRaider;

    /**
     * The Logger.
     */
    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private DynamoDBDelegator dynamoDBDelegator;

    /**
     * Instantiates a new Dynamo db raider.
     */
    public DynamoDBRaiderImpl() {
    }


    /**
     * Block the DynamoDB with the given ip address.
     *
     * @param ip private ip address
     * @return script execution status
     */
    @Override
    public String blockDynamoDB(String ip) {
        return executeScriptOnClasspath(ip, Actions.BLOCKDYNAMO.getActionName());
    }

    /**
     * Unblock the DynamoDB with the given ip address.
     *
     * @param ip private ip address
     * @return script execution status
     */
    @Override
    public String unblockDynamoDB(String ip) {
        return executeScriptOnClasspath(ip, Actions.UNBLOCKDYNAMO.getActionName());
    }

    /**
     * Executes the script given.
     *
     * @param ip         the ip to execute the given script on
     * @param scriptName the name of the script. expects this to be a shell script. will add the extension within this method.
     * @return script execution status
     */
    private String executeScriptOnClasspath(String ip, String scriptName) {
        URL url = ClassLoader.getSystemResource(scriptName + ".sh");
        return systemRaider.executeScript(ip, url.getPath());
    }

    @Override
    public long getDynamoDBReadIOPS(String tableName) {

       return dynamoDBDelegator.getAmazonDynamoDB().describeTable(new DescribeTableRequest().withTableName(tableName))
                .getTable().getProvisionedThroughput().getReadCapacityUnits();
    }

    @Override
    public void setDyanmoDBReadIOPS(String tableName,long readIops) {

        dynamoDBDelegator.getAmazonDynamoDB().updateTable(new UpdateTableRequest().withTableName(tableName).withProvisionedThroughput(new ProvisionedThroughput().withReadCapacityUnits(readIops).withWriteCapacityUnits(getDynamoDBWriteIOPS(tableName))));
    }

    @Override
    public long getDynamoDBWriteIOPS(String tableName) {
        return dynamoDBDelegator.getAmazonDynamoDB().describeTable(new DescribeTableRequest().withTableName(tableName))
                .getTable().getProvisionedThroughput().getWriteCapacityUnits();
    }

    @Override
    public void setDyanmoDBWriteIOPS(String tableName, long writeIops) {
        dynamoDBDelegator.getAmazonDynamoDB().updateTable(new UpdateTableRequest().withTableName(tableName).withProvisionedThroughput(new ProvisionedThroughput().withWriteCapacityUnits(writeIops).withReadCapacityUnits(getDynamoDBReadIOPS(tableName))));
    }

    @Override
    public void setDyanmoDBReadAndWriteIOPS(String tableName, long readIops, long writeIops) {

        dynamoDBDelegator.getAmazonDynamoDB().updateTable(new UpdateTableRequest().withTableName(tableName).withProvisionedThroughput(new ProvisionedThroughput().withWriteCapacityUnits(writeIops).withReadCapacityUnits(readIops)));

    }
}
