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

/**
 * AWS DynamoDB functionality.
 */
public interface DynamoDBRaider {

    /**
     * Block the DynamoDB with the given ip address.
     *
     * @param ip private ip address
     * @return script execution status
     */
    String blockDynamoDB(String ip);

    /**
     * Unblock the DynamoDB with the given ip address.
     *
     * @param ip private ip address
     * @return script execution status
     */
    String unblockDynamoDB(String ip);

    /**
     * Gets dynamo db read iops.
     *
     * @param tableName the table name
     * @return the dynamo db read iops
     */
    public long getDynamoDBReadIOPS(String tableName);

    /**
     * Sets dyanmo db read iops.
     *
     * @param tableName the table name
     * @param readIops  the read iops
     */
    public void setDyanmoDBReadIOPS(String tableName, long readIops);

    /**
     * Gets dynamo db write iops.
     *
     * @param tableName the table name
     * @return the dynamo db write iops
     */
    public long getDynamoDBWriteIOPS(String tableName);

    /**
     * Sets dyanmo db write iops.
     *
     * @param tableName the table name
     * @param writeIops the write iops
     */
    public void setDyanmoDBWriteIOPS(String tableName, long writeIops);

    /**
     * Sets dyanmo db read and write iops.
     *
     * @param tableName the table name
     * @param readIops  the read iops
     * @param writeIops the write iops
     */
    public void setDyanmoDBReadAndWriteIOPS(String tableName, long readIops, long writeIops);
}