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

import com.intuit.cloudraider.core.interfaces.RDSRaider;
import com.intuit.cloudraider.cucumber.model.ExecutionStateCache;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.List;
import java.util.stream.IntStream;

/**
 * Cucumber Step Definitions for AWS Relational Database Service functionality.
 */
public class RDSFailureStepDefinitions {


    @Autowired
    @Qualifier("rdsRaiderBean")
    private RDSRaider rdsRaider;

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

    public RDSFailureStepDefinitions() {
    }

    /**
     * Sets the name of the database.
     *
     * @param dbName database name
     */
    @Given("^dBInstance \"([^\"]*)\"$")
    public RDSFailureStepDefinitions givenRDSInstances(String dbName) {
        logger.info("RDS instances " + rdsRaider.getAllDbInstanceNames());
        executionStateCache.addDBInstances(rdsRaider.getAllDbInstanceNames());
        if (executionStateCache.getDBInstances().contains(dbName)) {
            executionStateCache.setDbName(dbName);
        } else {
            throw new RuntimeException("Unable to set the db name, no db instances available");
        }

        return this;
    }

    /**
     * Reboot the given number of instances.
     *
     * @param numDbInstances number of instances
     */
    @When("^reboot (\\d+) DbInstance$")
    public RDSFailureStepDefinitions rebootInstanceOnNumInstances(int numDbInstances) {
        List<String> dbInstances = executionStateCache.getDBInstances();
        if (dbInstances == null || dbInstances.isEmpty()) {
            throw new RuntimeException("Unable to reboot db instance, no db instances available");
        }

        if (numDbInstances > dbInstances.size()) {
            numDbInstances = dbInstances.size();
        }

        IntStream.range(0, numDbInstances)
                .parallel()
                .forEach(
                        i ->
                        {
                            rdsRaider.rebootDbInstance(dbInstances.get(i));
                        });

        return this;
    }

    /**
     * Assert that the expected status of the RDS matches the given status.
     *
     * @param expectedStatus expected status
     */
    @Then("^assertRDS instance status \"([^\"]*)\"$")
    public void assertDbInstanceStatus(String expectedStatus) {
        String dbName = executionStateCache.getDbName();
        if (dbName == null || dbName.isEmpty()) {
            throw new RuntimeException("Unable to reboot db instance, no db instance available");
        }
        org.testng.Assert.assertEquals(rdsRaider.getDBInstanceStatus(dbName), expectedStatus);
    }

    /**
     * Reboot the current database instance.
     */
    @When("^reboot DbInstance$")
    public RDSFailureStepDefinitions rebootDBInstance() {
        String dbName = executionStateCache.getDbName();
        if (dbName == null || dbName.isEmpty()) {
            throw new RuntimeException("Unable to reboot db instance, no db instance available");
        }

        rdsRaider.rebootDbInstance(dbName);
        return this;
    }

    /**
     * Detach the security group from the database instance. If the number of security groups currently attached is less than 2 and
     * the database instance currently does not have the default security group id, add the default.
     *
     * @param groupId security group id
     * @param defaultGroupId default security group id
     */
    @When("^detach DBSecurityGroup \"([^\"]*)\" with \"([^\"]*)\"$")
    public RDSFailureStepDefinitions detachDBSecurityGroup(String groupId, String defaultGroupId) {
        String dbName = executionStateCache.getDbName();

        if (dbName == null || dbName.isEmpty()) {
            throw new RuntimeException("Unable to detach security group on db instance, no db instance available");
        }
        if (groupId == null || groupId.isEmpty()) {
            throw new RuntimeException("Unable to detach security group on db instance, no security group available");
        }
        if (rdsRaider.getSecurityGroups(dbName).size() < 2 && !rdsRaider.getSecurityGroups(dbName).contains(defaultGroupId)) {
            rdsRaider.attachSecurityGroups(dbName, defaultGroupId);
        }
        rdsRaider.detachSecurityGroups(dbName, groupId);
        return this;
    }

    /**
     * Attach the security group from the database instance. If the database instance has the default security group id,
     * detach the default.
     *
     * @param groupId security group id
     * @param defaultGroupId default security group id
     */
    @Then("^attach DBSecurityGroup \"([^\"]*)\" with \"([^\"]*)\"$")
    public RDSFailureStepDefinitions attachDBSecurityGroup(String groupId, String defaultGroupId) {
        String dbName = executionStateCache.getDbName();
        if (dbName == null || dbName.isEmpty()) {
            throw new RuntimeException("Unable to detach security group on db instance, no db instance available");
        }
        if (groupId == null || groupId.isEmpty()) {
            throw new RuntimeException("Unable to detach security group on db instance, no security group available");
        }
        rdsRaider.attachSecurityGroups(dbName, groupId);

        if (rdsRaider.getSecurityGroups(dbName).contains(defaultGroupId)) {
            rdsRaider.detachSecurityGroups(dbName, defaultGroupId);
        }
        return this;
    }

    /**
     * Detach subnet with matching id to the current database instance.
     *
     * @param subnetId subnet id
     */
    @When("^detach DB subnet \"([^\"]*)\"$")
    public RDSFailureStepDefinitions detachDBSubnet(String subnetId) throws Throwable {
        // Write code here that turns the phrase above into concrete actions
        String dbName = executionStateCache.getDbName();
        if (dbName == null || dbName.isEmpty()) {
            throw new RuntimeException("Unable to detach subnet from db instance, no db instance available");
        }
        if (subnetId == null || subnetId.isEmpty()) {
            throw new RuntimeException("Subnet is null or empty");
        }
        rdsRaider.detachSubnet(dbName, subnetId);
        return this;
    }

    /**
     * Attach subnet with matching id to the current database instance.
     *
     * @param subnetId subnet id
     */
    @Then("^attach DB subnet \"([^\"]*)\"$")
    public RDSFailureStepDefinitions attachDBSubnet(String subnetId) throws Throwable {
        // Write code here that turns the phrase above into concrete actions
        String dbName = executionStateCache.getDbName();
        if (dbName == null || dbName.isEmpty()) {
            throw new RuntimeException("Unable to attach subnet from db instance, no db instance available");
        }
        if (subnetId == null || subnetId.isEmpty()) {
            throw new RuntimeException("Subnet is null or empty");
        }
        rdsRaider.attachSubnet(dbName, subnetId);
        return this;
    }

    /**
     * Create database snapshot with the given name.
     *
     * @param snapshotName name of snapshot to be created
     */
    @When("^create DB snapshot with name \"([^\"]*)\"$")
    public RDSFailureStepDefinitions createDBSnapshot(String snapshotName) throws Throwable {
        // Write code here that turns the phrase above into concrete actions
        rdsRaider.generateSnapshot(executionStateCache.getDbName(), snapshotName);

        return this;
    }

    /**
     * Restore the database from the snapshot with the given name.
     *
     * @param snapshotName String snapshot name
     */
    @When("^restore DB from snapshot with name \"([^\"]*)\"$")
    public RDSFailureStepDefinitions restoreDBFromSnapshot(String snapshotName) throws Throwable {
        // Write code here that turns the phrase above into concrete actions
        rdsRaider.restoreDBInstanceFromSnapshot(executionStateCache.getDbName(), snapshotName);
        return this;
    }

    /**
     * Change the database storage size to the one provided.
     *
     * @param storageSize storage size in GB
     */
    @When("^change DB storage size to (\\d+) GB$")
    public RDSFailureStepDefinitions changeDBStorageSize(int storageSize) throws Throwable {
        // Write code here that turns the phrase above into concrete actions
        rdsRaider.modifyDbStorageSize(executionStateCache.getDbName(), storageSize);
        return this;
    }

    /**
     * Change the database iops size to the one provided.
     *
     * @param iops integer iops size
     */
    @When("^change DB iops size to (\\d+)$")
    public void changeDBIops(int iops) throws Throwable {
        // Write code here that turns the phrase above into concrete actions
        rdsRaider.modifyDbIops(executionStateCache.getDbName(), iops);
    }

    /**
     * Change the database instance class to the one provided.
     *
     * @param instanceClass database instance class
     */
    @When("^change DB InstanceClass \"([^\"]*)\"$")
    public void changeDBInstanceClass(String instanceClass) throws Throwable {
        // Write code here that turns the phrase above into concrete actions
        rdsRaider.modifyDbInstanceClass(executionStateCache.getDbName(), instanceClass);
    }
}
