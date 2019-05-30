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


import com.intuit.cloudraider.core.interfaces.DynamoDBRaider;
import com.intuit.cloudraider.cucumber.model.ExecutionStateCache;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;


/**
 * The type Dynamo db step definitions.
 */
public class DynamoDBStepDefinitions {


    @Autowired
    @Qualifier("dynamoRaiderBean")
    private DynamoDBRaider dynamoDBRaider;

    /**
     * The Logger.
     */
    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ExecutionStateCache executionStateCache;

    /**
     * Gets execution state cache.
     *
     * @return the execution state cache
     */
    public ExecutionStateCache getExecutionStateCache() {
        return executionStateCache;
    }

    /**
     * Sets execution state cache.
     *
     * @param executionStateCache the execution state cache
     */
    public void setExecutionStateCache(ExecutionStateCache executionStateCache) {
        this.executionStateCache = executionStateCache;
    }


    /**
     * Instantiates a new Dynamo db step definitions.
     */
    public DynamoDBStepDefinitions()
    {

    }

    /**
     * Given rds instances dynamo db step definitions.
     *
     * @param tableName the table name
     * @return the dynamo db step definitions
     */
    @Given("^DynamoDb table \"([^\"]*)\"$")
    public DynamoDBStepDefinitions givenRDSInstances(String tableName)
    {
        executionStateCache.setDynamoDBTable(tableName);
        return this;
    }

    /**
     * Change dynamo db read capacity dynamo db step definitions.
     *
     * @param readCapacity the read capacity
     * @return the dynamo db step definitions
     */
    @When("^DynamoDB set read capacity to (\\d+)$")
    public DynamoDBStepDefinitions changeDynamoDBReadCapacity(long readCapacity )
    {
        String tableName = executionStateCache.getDynamoDBTable();
        if (tableName == null  || tableName.isEmpty())
        {
            throw new RuntimeException("Missing DynamoDb table information");
        }

        if (executionStateCache.getDynamoReadCapacity() <= 0L)
        {
            executionStateCache.setDynamoReadCapacity(dynamoDBRaider.getDynamoDBReadIOPS(tableName));
        }

        dynamoDBRaider.setDyanmoDBReadIOPS(tableName, readCapacity);

        return this;
    }

    /**
     * Change dynamo db write capacity dynamo db step definitions.
     *
     * @param writeCapacity the write capacity
     * @return the dynamo db step definitions
     */
    @When("^DynamoDB set write capacity to (\\d+)$")
    public DynamoDBStepDefinitions changeDynamoDBWriteCapacity(long writeCapacity )
    {
        String tableName = executionStateCache.getDynamoDBTable();
        if (tableName == null  || tableName.isEmpty())
        {
            throw new RuntimeException("Missing DynamoDb table information");
        }

        if (executionStateCache.getDynamoWriteCapacity() <= 0L)
        {
            executionStateCache.setDynamoWriteCapacity(dynamoDBRaider.getDynamoDBWriteIOPS(tableName));
        }

        dynamoDBRaider.setDyanmoDBWriteIOPS(tableName, writeCapacity);


        return this;
    }


    /**
     * Change dynamo db read and write capacity dynamo db step definitions.
     *
     * @param readCapacity  the read capacity
     * @param writeCapacity the write capacity
     * @return the dynamo db step definitions
     */
    @When("^DynamoDB set read capacity to (\\d+) and write capacity to (\\d+)$")
    public DynamoDBStepDefinitions changeDynamoDBReadAndWriteCapacity(long readCapacity, long writeCapacity )
    {
        String tableName = executionStateCache.getDynamoDBTable();
        if (tableName == null  || tableName.isEmpty())
        {
            throw new RuntimeException("Missing DynamoDb table information");
        }


        if (executionStateCache.getDynamoWriteCapacity() <= 0L)
        {
            executionStateCache.setDynamoWriteCapacity(dynamoDBRaider.getDynamoDBWriteIOPS(tableName));
        }


        if (executionStateCache.getDynamoReadCapacity() <= 0L)
        {
            executionStateCache.setDynamoReadCapacity(dynamoDBRaider.getDynamoDBReadIOPS(tableName));
        }

        dynamoDBRaider.setDyanmoDBReadAndWriteIOPS(tableName, readCapacity, writeCapacity);


        return this;
    }

    /**
     * Assert dynamo read capacity.
     *
     * @param expectedStatus the expected status
     */
    @Then("^assertDynamoDB read capacity = (\\d+)$")
    public void assertDynamoReadCapacity(long expectedStatus){
        String tableName = executionStateCache.getDynamoDBTable();
        if (tableName == null  || tableName.isEmpty())
        {
            throw new RuntimeException("Missing DynamoDb table information");
        }
        org.testng.Assert.assertEquals(dynamoDBRaider.getDynamoDBReadIOPS(tableName), expectedStatus);
    }


    /**
     * Assert dynamo write capacity.
     *
     * @param expectedStatus the expected status
     */
    @Then("^assertDynamoDB write capacity = (\\d+)$")
    public void assertDynamoWriteCapacity(long expectedStatus){
        String tableName = executionStateCache.getDynamoDBTable();
        if (tableName == null  || tableName.isEmpty())
        {
            throw new RuntimeException("Missing DynamoDb table information");
        }
        org.testng.Assert.assertEquals(dynamoDBRaider.getDynamoDBWriteIOPS(tableName), expectedStatus);
    }


    /**
     * Assert dynamo read capacity.
     *
     * @param readCapacityStatus  the read capacity status
     * @param writeCapacityStatus the write capacity status
     */
    @Then("^assertDynamoDB read capacity = (\\d+) and write capacity = (\\d+)$")
    public void assertDynamoReadCapacity(long readCapacityStatus, long writeCapacityStatus){
        assertDynamoReadCapacity(readCapacityStatus);
        assertDynamoWriteCapacity(writeCapacityStatus);
    }


    /**
     * Rever dynamo capacity.
     */
    @Then("^DynamoDB revert capacity$")
    public void reverDynamoCapacity(){
        String tableName = executionStateCache.getDynamoDBTable();
        if (tableName == null  || tableName.isEmpty())
        {
            throw new RuntimeException("Missing DynamoDb table information");
        }

        if (executionStateCache.getDynamoReadCapacity() > 0 && executionStateCache.getDynamoWriteCapacity() > 0 )
        {
            logger.info("reverting dynamo throughtput for " + executionStateCache.getDynamoDBTable()
                    + " to  read:"+executionStateCache.getDynamoReadCapacity()+ "  write:" + executionStateCache.getDynamoWriteCapacity());
            changeDynamoDBReadAndWriteCapacity(executionStateCache.getDynamoReadCapacity(),executionStateCache.getDynamoWriteCapacity() );
        }
        else if (executionStateCache.getDynamoReadCapacity() > 0)
        {
            logger.info("reverting dynamo throughtput for " + executionStateCache.getDynamoDBTable()
                    + " to  read:"+executionStateCache.getDynamoReadCapacity());
            changeDynamoDBReadCapacity(executionStateCache.getDynamoReadCapacity());
        }
        else if (executionStateCache.getDynamoWriteCapacity() > 0)
        {
            logger.info("reverting dynamo throughtput for " + executionStateCache.getDynamoDBTable()
                    + " to write:" + executionStateCache.getDynamoWriteCapacity());
            changeDynamoDBWriteCapacity(executionStateCache.getDynamoWriteCapacity());
        }

    }

}
