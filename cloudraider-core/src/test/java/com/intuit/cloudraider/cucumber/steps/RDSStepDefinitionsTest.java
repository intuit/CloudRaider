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

import com.intuit.cloudraider.commons.RDSDelegator;
import com.intuit.cloudraider.core.impl.RDSRaiderImpl;
import com.intuit.cloudraider.cucumber.model.ExecutionStateCache;
import com.intuit.cloudraider.cucumber.util.ScriptExecutor;
import com.intuit.cloudraider.model.BasicCredentials;
import com.intuit.cloudraider.model.Credentials;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;


@PowerMockIgnore({"javax.management.*","javax.net.ssl.*"})
@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@PrepareForTest({RDSRaiderImpl.class, ScriptExecutor.class, RDSFailureStepDefinitions.class})
public class RDSStepDefinitionsTest {


    @Autowired
    private RDSFailureStepDefinitions rdsFailureStepDefinitions;

    @Autowired
    private RDSRaiderImpl rdsRaider;

//    @Autowired
//    private ScriptExecutor scriptExecutor;

    @Autowired
    private ExecutionStateCache executionStateCache;

    /**
     * Sets method.
     *
     * @throws Exception the exception
     */
    @Before
    public void setupMethod() throws Exception{

        executionStateCache.clear();

    }

    /**
     * Test given rds instances.
     *
     * @throws Throwable the throwable
     */
    @Test
    public void testGivenRDSInstances() throws Throwable
    {
        PowerMockito.when(rdsRaider.getAllDbInstanceNames()).thenReturn(Arrays.asList("db-123", "db-456"));
        rdsFailureStepDefinitions.givenRDSInstances("db-123");
        Assert.assertEquals(executionStateCache.getDbName(), "db-123");
    }

    /**
     * Test given rds instances missing db.
     *
     * @throws Throwable the throwable
     */
    @Test (expected = RuntimeException.class)
    public void testGivenRDSInstancesMissingDb() throws Throwable
    {
        PowerMockito.when(rdsRaider.getAllDbInstanceNames()).thenReturn(Arrays.asList("db-456"));
        rdsFailureStepDefinitions.givenRDSInstances("db-123");
    }

    /**
     * Test reboot instance on num instances.
     *
     * @throws Throwable the throwable
     */
    @Test
    public void testRebootInstanceOnNumInstances() throws Throwable
    {
        executionStateCache.setDBInstances(Arrays.asList("db-123", "db-456"));
        rdsFailureStepDefinitions.rebootInstanceOnNumInstances(1);
    }

    /**
     * Test reboot instance on wrong num instances.
     *
     * @throws Throwable the throwable
     */
    @Test (expected = RuntimeException.class)
    public void testRebootInstanceOnWrongNumInstances() throws Throwable
    {
        rdsFailureStepDefinitions.rebootInstanceOnNumInstances(3);
    }

    /**
     * Test assert db instance status.
     *
     * @throws Throwable the throwable
     */
    @Test
    public void testAssertDbInstanceStatus() throws Throwable
    {
        PowerMockito.when(rdsRaider.getDBInstanceStatus("db-123")).thenReturn("available");
        executionStateCache.setDbName("db-123");
        rdsFailureStepDefinitions.assertDbInstanceStatus("available");
    }

    /**
     * Test assert db instance status empty name.
     *
     * @throws Throwable the throwable
     */
    @Test (expected = RuntimeException.class)
    public void testAssertDbInstanceStatusEmptyName() throws Throwable
    {
        PowerMockito.when(rdsRaider.getDBInstanceStatus("db-123")).thenReturn("available");
        executionStateCache.setDbName("");
        rdsFailureStepDefinitions.assertDbInstanceStatus("available");
    }

    /**
     * Test reboot db instance.
     *
     * @throws Throwable the throwable
     */
    @Test
    public void testRebootDBInstance() throws Throwable
    {
        executionStateCache.setDbName("db-123");
        rdsFailureStepDefinitions.rebootDBInstance();
    }

    /**
     * Test reboot db instance empty name.
     *
     * @throws Throwable the throwable
     */
    @Test (expected = RuntimeException.class)
    public void testRebootDBInstanceEmptyName() throws Throwable
    {
        executionStateCache.setDbName("");
        rdsFailureStepDefinitions.rebootDBInstance();
    }

    /**
     * Test detach db security group.
     *
     * @throws Throwable the throwable
     */
    @Test
    public void testDetachDBSecurityGroup() throws Throwable
    {
        PowerMockito.when(rdsRaider.getSecurityGroups("db-123")).thenReturn(Arrays.asList("sg-123", "sg-456"));
        executionStateCache.setDbName("db-123");
        rdsFailureStepDefinitions.detachDBSecurityGroup("sg-123", "def-789");
    }

    /**
     * Test detach db security group empty db name.
     *
     * @throws Throwable the throwable
     */
    @Test (expected = RuntimeException.class)
    public void testDetachDBSecurityGroupEmptyDbName() throws Throwable
    {
        PowerMockito.when(rdsRaider.getSecurityGroups("db-123")).thenReturn(Arrays.asList("sg-123", "sg-456"));
        executionStateCache.setDbName("");
        rdsFailureStepDefinitions.detachDBSecurityGroup("sg-123", "def-789");
    }

    /**
     * Test detach db security group empty group id.
     *
     * @throws Throwable the throwable
     */
    @Test (expected = RuntimeException.class)
    public void testDetachDBSecurityGroupEmptyGroupId() throws Throwable
    {
        PowerMockito.when(rdsRaider.getSecurityGroups("db-123")).thenReturn(Arrays.asList("sg-123", "sg-456"));
        executionStateCache.setDbName("db-123");
        rdsFailureStepDefinitions.detachDBSecurityGroup("", "def-789");
    }

    /**
     * Test detach db security group empty no group.
     *
     * @throws Throwable the throwable
     */
    @Test
    public void testDetachDBSecurityGroupEmptyNoGroup() throws Throwable
    {
        PowerMockito.when(rdsRaider.getSecurityGroups("db-123")).thenReturn(Arrays.asList("sg-123"));
        executionStateCache.setDbName("db-123");
        rdsFailureStepDefinitions.detachDBSecurityGroup("sg-123", "def-789");
    }

    /**
     * Test attach db security group.
     *
     * @throws Throwable the throwable
     */
    @Test
    public void testAttachDBSecurityGroup() throws Throwable
    {
        PowerMockito.when(rdsRaider.getSecurityGroups("db-123")).thenReturn(Arrays.asList("sg-123", "sg-456"));
        executionStateCache.setDbName("db-123");
        rdsFailureStepDefinitions.attachDBSecurityGroup("sg-123", "def-789");
    }

    /**
     * Test attach db security group empty name.
     *
     * @throws Throwable the throwable
     */
    @Test (expected = RuntimeException.class)
    public void testAttachDBSecurityGroupEmptyName() throws Throwable
    {
        PowerMockito.when(rdsRaider.getSecurityGroups("db-123")).thenReturn(Arrays.asList("sg-123", "sg-456"));
        executionStateCache.setDbName("");
        rdsFailureStepDefinitions.attachDBSecurityGroup("sg-123", "def-789");
    }

    /**
     * Test attach db security group empty group id.
     *
     * @throws Throwable the throwable
     */
    @Test (expected = RuntimeException.class)
    public void testAttachDBSecurityGroupEmptyGroupId() throws Throwable
    {
        PowerMockito.when(rdsRaider.getSecurityGroups("db-123")).thenReturn(Arrays.asList("sg-123", "sg-456"));
        executionStateCache.setDbName("db-123");
        rdsFailureStepDefinitions.attachDBSecurityGroup("", "def-789");
    }

    /**
     * Test attach db security group no group.
     *
     * @throws Throwable the throwable
     */
    @Test
    public void testAttachDBSecurityGroupNoGroup() throws Throwable
    {
        PowerMockito.when(rdsRaider.getSecurityGroups("db-123")).thenReturn(Arrays.asList("sg-123"));
        executionStateCache.setDbName("db-123");
        rdsFailureStepDefinitions.attachDBSecurityGroup("sg-123", "def-789");
    }

    /**
     * Test attach db security group def group.
     *
     * @throws Throwable the throwable
     */
    @Test
    public void testAttachDBSecurityGroupDefGroup() throws Throwable
    {
        PowerMockito.when(rdsRaider.getSecurityGroups("db-123")).thenReturn(Arrays.asList("sg-123", "def-789"));
        executionStateCache.setDbName("db-123");
        rdsFailureStepDefinitions.attachDBSecurityGroup("sg-123", "def-789");
    }

    /**
     * Test detach db subnet.
     *
     * @throws Throwable the throwable
     */
    @Test
    public void testDetachDBSubnet() throws Throwable
    {
         executionStateCache.setDbName("db-123");
        rdsFailureStepDefinitions.detachDBSubnet("subnet-123");
    }

    /**
     * Test detach db subnet empty name.
     *
     * @throws Throwable the throwable
     */
    @Test (expected = RuntimeException.class)
    public void testDetachDBSubnetEmptyName() throws Throwable
    {
        executionStateCache.setDbName("");
        rdsFailureStepDefinitions.detachDBSubnet("subnet-123");
    }

    /**
     * Test detach db subnet empty id.
     *
     * @throws Throwable the throwable
     */
    @Test (expected = RuntimeException.class)
    public void testDetachDBSubnetEmptyId() throws Throwable
    {
        executionStateCache.setDbName("db-123");
        rdsFailureStepDefinitions.detachDBSubnet("");
    }

    /**
     * Test attach db subne empty namet.
     *
     * @throws Throwable the throwable
     */
    @Test (expected = RuntimeException.class)
    public void testAttachDBSubneEmptyNamet() throws Throwable
    {
        executionStateCache.setDbName("");
        rdsFailureStepDefinitions.attachDBSubnet("subnet-123");
    }

    /**
     * Test attach db subnet empty id.
     *
     * @throws Throwable the throwable
     */
    @Test (expected = RuntimeException.class)
    public void testAttachDBSubnetEmptyId() throws Throwable
    {
        executionStateCache.setDbName("db-123");
        rdsFailureStepDefinitions.attachDBSubnet("");
    }

    /**
     * Test attach db subnet.
     *
     * @throws Throwable the throwable
     */
    @Test
    public void testAttachDBSubnet() throws Throwable
    {
        executionStateCache.setDbName("db-123");
        rdsFailureStepDefinitions.attachDBSubnet("subnet-123");
    }

    /**
     * Test create db snapshot.
     *
     * @throws Throwable the throwable
     */
    @Test
    public void testCreateDBSnapshot() throws Throwable
    {
        executionStateCache.setDbName("db-123");
        rdsFailureStepDefinitions.createDBSnapshot("snapshot-123");
    }

    /**
     * Test restore db from snapshot.
     *
     * @throws Throwable the throwable
     */
    @Test
    public void testRestoreDBFromSnapshot() throws Throwable
    {
        executionStateCache.setDbName("db-123");
        rdsFailureStepDefinitions.restoreDBFromSnapshot("snapshot-123");
    }

    /**
     * Test change db storage size.
     *
     * @throws Throwable the throwable
     */
    @Test
    public void testChangeDBStorageSize() throws Throwable
    {
        executionStateCache.setDbName("db-123");
        rdsFailureStepDefinitions.changeDBStorageSize(5);
    }

    /**
     * Test change db iops.
     *
     * @throws Throwable the throwable
     */
    @Test
    public void testChangeDBIops() throws Throwable
    {
        executionStateCache.setDbName("db-123");
        rdsFailureStepDefinitions.changeDBIops(67);
    }

    /**
     * Testchange db instance class.
     *
     * @throws Throwable the throwable
     */
    @Test
    public void testchangeDBInstanceClass() throws Throwable
    {
        executionStateCache.setDbName("db-123");
        rdsFailureStepDefinitions.changeDBInstanceClass("className");
    }

    /**
     * The type Rds step definition test context configuration.
     */
    @Configuration
    protected static class RDSStepDefinitionTestContextConfiguration {


        /**
         * Rds delegator rds delegator.
         *
         * @return the rds delegator
         */
        @Bean
        public RDSDelegator rdsDelegator() {
            return Mockito.mock(RDSDelegator.class);
        }

        /**
         * Rds raider rds raider.
         *
         * @return the rds raider
         */
        @Bean (name={"rdsRaiderBean"})
        public RDSRaiderImpl rdsRaider() {
            return  Mockito.mock(RDSRaiderImpl.class);
        }


        /**
         * Credentials credentials.
         *
         * @return the credentials
         */
        @Bean
        public Credentials credentials() {
            return new BasicCredentials();
        }

        /**
         * Rds failure step definitions rds failure step definitions.
         *
         * @return the rds failure step definitions
         */
        @Bean
        public RDSFailureStepDefinitions rdsFailureStepDefinitions() {
            return new RDSFailureStepDefinitions();
        }

        /**
         * Execution state cache execution state cache.
         *
         * @return the execution state cache
         */
        @Bean
        public ExecutionStateCache executionStateCache() {
            return new ExecutionStateCache();
        }


    }

}
