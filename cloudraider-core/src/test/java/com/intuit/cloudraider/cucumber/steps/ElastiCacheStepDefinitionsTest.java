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

import com.intuit.cloudraider.cucumber.model.ExecutionStateCache;
import com.intuit.cloudraider.core.impl.ElastiCacheRaiderImpl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;


@RunWith(PowerMockRunner.class)
@PrepareForTest({ElastiCacheRaiderImpl.class, ElastiCacheStepDefinitions.class})
public class ElastiCacheStepDefinitionsTest {


    private ElastiCacheStepDefinitions elastiCacheStepDefinitions;
    private ElastiCacheRaiderImpl elastiCacheRaider;

    private ExecutionStateCache executionStateCache;

    /**
     * Sets method.
     *
     * @throws Exception the exception
     */
    @Before
    public void setupMethod() throws Exception{

        elastiCacheRaider = PowerMockito.mock(ElastiCacheRaiderImpl.class);
        PowerMockito.whenNew(ElastiCacheRaiderImpl.class).withNoArguments().thenReturn(elastiCacheRaider);

        elastiCacheStepDefinitions = new ElastiCacheStepDefinitions();
        executionStateCache = new ExecutionStateCache();
        elastiCacheStepDefinitions.setExecutionStateCache(executionStateCache);

    }

    /**
     * Test given elasti cache cluster.
     *
     * @throws Exception the exception
     */
    @Test
    public void testGivenElastiCacheCluster() throws Exception
    {
        elastiCacheStepDefinitions.givenElastiCacheCluster("cacheCluster");
        Assert.assertEquals(executionStateCache.getElastiCacheClusterName(), "cacheCluster");

    }

    /**
     * Test reboot elasti cache cluster.
     *
     * @throws Throwable the throwable
     */
    @Test
    public void testRebootElastiCacheCluster() throws Throwable
    {
        executionStateCache.setElastiCacheClusterName("cacheCluster");
        elastiCacheStepDefinitions.rebootElastiCacheCluster(1);
        Assert.assertEquals(executionStateCache.getElastiCacheClusterName(), "cacheCluster");

    }

    /**
     * Test assert elasti cache cluster status.
     *
     * @throws Throwable the throwable
     */
    @Test
    public void testAssertElastiCacheClusterStatus() throws Throwable
    {
        executionStateCache.setElastiCacheClusterName("cacheCluster");
        PowerMockito.when(elastiCacheRaider.getElastiCacheClusterStatus("cacheCluster")).thenReturn("available");
        elastiCacheStepDefinitions.assertElastiCacheClusterStatus("available");
    }

    /**
     * Test assert elasti cache cluster exception status.
     *
     * @throws Throwable the throwable
     */
    @Test (expected = AssertionError.class)
    public void testAssertElastiCacheClusterExceptionStatus() throws Throwable
    {
        executionStateCache.setElastiCacheClusterName("cacheCluster");
        PowerMockito.when(elastiCacheRaider.getElastiCacheClusterStatus("cacheCluster")).thenReturn("available");
        elastiCacheStepDefinitions.assertElastiCacheClusterStatus("unavailable");

    }

    /**
     * Test add elasti cache nodes.
     *
     * @throws Throwable the throwable
     */
    @Test
    public void testAddElastiCacheNodes() throws Throwable
    {
        executionStateCache.setElastiCacheClusterName("cacheCluster");
        elastiCacheStepDefinitions.addElastiCacheNodes(1);
    }

    /**
     * Test remove elasti cache nodes.
     *
     * @throws Throwable the throwable
     */
    @Test
    public void testRemoveElastiCacheNodes() throws Throwable
    {
        executionStateCache.setElastiCacheClusterName("cacheCluster");
        elastiCacheStepDefinitions.removeElastiCacheNodes(1);
    }

    /**
     * Test detach elasti cache subnet.
     *
     * @throws Throwable the throwable
     */
    @Test
    public void testDetachElastiCacheSubnet() throws Throwable
    {
        executionStateCache.setElastiCacheClusterName("cacheCluster");
        elastiCacheStepDefinitions.detachElastiCacheSubnet("subnet-1234");
    }

    /**
     * Test attach elasti cache subnet.
     *
     * @throws Throwable the throwable
     */
    @Test
    public void testAttachElastiCacheSubnet() throws Throwable
    {
        executionStateCache.setElastiCacheClusterName("cacheCluster");
        elastiCacheStepDefinitions.attachElastiCacheSubnet("subnet-1234");
    }

    /**
     * Test change elasti cache security group.
     *
     * @throws Throwable the throwable
     */
    @Test
    public void testChangeElastiCacheSecurityGroup() throws Throwable
    {
        executionStateCache.setElastiCacheClusterName("cacheCluster");
        elastiCacheStepDefinitions.changeElastiCacheSecurityGroup("sg-123", "sg-456");
    }


}
