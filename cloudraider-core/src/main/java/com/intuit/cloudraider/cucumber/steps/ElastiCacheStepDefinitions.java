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

import com.intuit.cloudraider.core.impl.ElastiCacheRaiderImpl;
import com.intuit.cloudraider.core.interfaces.ElastiCacheRaider;
import com.intuit.cloudraider.cucumber.model.ExecutionStateCache;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * Cucumber Step Definitions for AWS ElastiCache functionality.
 */
public class ElastiCacheStepDefinitions {

    @Autowired
    @Qualifier("elastiCacheRaiderBean")
    private ElastiCacheRaider elastiCacheRaider;

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
     * Instantiates a new Elasti cache step definitions.
     */
    public ElastiCacheStepDefinitions() {
        elastiCacheRaider = new ElastiCacheRaiderImpl();

    }

    /**
     * Sets the ElastiCache cluster name.
     *
     * @param elasticCacheClusterName name of cluster
     * @return the elasti cache step definitions
     */
    @Given("^ElastiCache \"([^\"]*)\"$")
    public ElastiCacheStepDefinitions givenElastiCacheCluster(String elasticCacheClusterName) {
        executionStateCache.setElastiCacheClusterName(elasticCacheClusterName);
        return this;
    }

    /**
     * Reboot the provided number of the nodes on the current ElastiCache cluster.
     *
     * @param numNodes number of nodes to reboot
     * @return the elasti cache step definitions
     * @throws Throwable the throwable
     */
    @When("^reboot ElastiCache nodes (\\d+)$")
    public ElastiCacheStepDefinitions rebootElastiCacheCluster(int numNodes) throws Throwable {

        String clusterName = executionStateCache.getElastiCacheClusterName();
        if (clusterName == null || clusterName.isEmpty()) {
            throw new RuntimeException("Unable to reboot elastiCache cluster as no cluster information is available");
        }
        elastiCacheRaider.rebootElastiCacheCluster(clusterName, numNodes);
        return this;
    }

    /**
     * Asserts that ElastiCache state matches what is expected.
     *
     * @param expectedStatus expected status of ElastiCache cluster
     * @throws Throwable the throwable
     */
    @Then("^assertElastiCache cluster status \"([^\"]*)\"$")
    public void assertElastiCacheClusterStatus(String expectedStatus) throws Throwable {

        String clusterName = executionStateCache.getElastiCacheClusterName();
        if (clusterName == null || clusterName.isEmpty()) {
            throw new RuntimeException("Unable to reboot elastiCache cluster as no cluster information is available");
        }
        org.testng.Assert.assertEquals(elastiCacheRaider.getElastiCacheClusterStatus(clusterName), expectedStatus);
    }

    /**
     * Adds the provided number of nodes to the current ElastiCache cluster.
     *
     * @param numNodes number of nodes to add
     * @return the elasti cache step definitions
     * @throws Throwable the throwable
     */
    @When("^add ElastiCache nodes (\\d+)$")
    public ElastiCacheStepDefinitions addElastiCacheNodes(int numNodes) throws Throwable {

        String clusterName = executionStateCache.getElastiCacheClusterName();
        if (clusterName == null || clusterName.isEmpty()) {
            throw new RuntimeException("Unable to reboot elastiCache cluster as no cluster information is available");
        }

        elastiCacheRaider.addNodesToCluster(clusterName, numNodes);
        return this;
    }

    /**
     * Removes the provided number of nodes from the current ElastiCache cluster.
     *
     * @param numNodes number of nodes to remove
     * @return the elasti cache step definitions
     * @throws Throwable the throwable
     */
    @When("^remove ElastiCache nodes (\\d+)$")
    public ElastiCacheStepDefinitions removeElastiCacheNodes(int numNodes) throws Throwable {

        String clusterName = executionStateCache.getElastiCacheClusterName();
        if (clusterName == null || clusterName.isEmpty()) {
            throw new RuntimeException("Unable to reboot elastiCache cluster as no cluster information is available");
        }

        elastiCacheRaider.removeNodesFromCluster(clusterName, numNodes);
        return this;
    }

    /**
     * Detaches the provided subnet from the ElastiCache cluster.
     *
     * @param subnetId subnet id to detach
     * @return the elasti cache step definitions
     * @throws Throwable the throwable
     */
    @When("^detach ElastiCache subnet \"([^\"]*)\"$")
    public ElastiCacheStepDefinitions detachElastiCacheSubnet(String subnetId) throws Throwable {
        String clusterName = executionStateCache.getElastiCacheClusterName();
        if (clusterName == null || clusterName.isEmpty()) {
            throw new RuntimeException("Unable to reboot elastiCache cluster as no cluster information is available");
        }

        elastiCacheRaider.detachSubnet(clusterName, subnetId);
        return this;

    }

    /**
     * Attaches the provided subnet to the ElastiCache cluster.
     *
     * @param subnetId subnet id to atttach
     * @return the elasti cache step definitions
     * @throws Throwable the throwable
     */
    @Then("^attach ElastiCache subnet \"([^\"]*)\"$")
    public ElastiCacheStepDefinitions attachElastiCacheSubnet(String subnetId) throws Throwable {
        String clusterName = executionStateCache.getElastiCacheClusterName();
        if (clusterName == null || clusterName.isEmpty()) {
            throw new RuntimeException("Unable to reboot elastiCache cluster as no cluster information is available");
        }

        elastiCacheRaider.attachSubnet(clusterName, subnetId);
        return this;
    }

    /**
     * Updates the old security group id with the new one for the ElastiCache cluster.
     *
     * @param secGroupFrom old security group id
     * @param secGroupTo   new security group id
     * @return the elasti cache step definitions
     * @throws Throwable the throwable
     */
    @When("^change ElastiCache security group from \"([^\"]*)\" to \"([^\"]*)\"$")
    public ElastiCacheStepDefinitions changeElastiCacheSecurityGroup(String secGroupFrom, String secGroupTo) throws Throwable {
        ;
        String clusterName = executionStateCache.getElastiCacheClusterName();

        if (clusterName == null || clusterName.isEmpty()) {
            throw new RuntimeException("Unable to detach security group on ElastiCache cluster, cluster in not available");
        }
        if (secGroupFrom == null || secGroupFrom.isEmpty()) {
            throw new RuntimeException("Unable to detach security group on ElastiCache instance, no security group available");
        }

        elastiCacheRaider.changeSecurityGroupId(clusterName, secGroupFrom, secGroupTo);

        return this;
    }


    /**
     * Given elast cache node elasti cache step definitions.
     *
     * @param nodeName the node name
     * @return the elasti cache step definitions
     */
    @Given("^ElastiCache node \"([^\"]*)\"$")
    public ElastiCacheStepDefinitions givenElastCacheNode(String nodeName)
    {
        executionStateCache.setCacheNodes(elastiCacheRaider.getAllElastiCacheNodeNames());
        if (executionStateCache.getCacheNodes().contains(nodeName)) {
            executionStateCache.setCacheNodeName(nodeName);
        }
        else {
            throw new RuntimeException("Unable to set the elasticache name, no elasticache instances available");
        }
        return this;
    }

    /**
     * Reboot elasti cache node elasti cache step definitions.
     *
     * @return the elasti cache step definitions
     */
    @When("^reboot ElastiCache node")
    public ElastiCacheStepDefinitions rebootElastiCacheNode()
    {


        String cacheNodeName = executionStateCache.getCacheNodeName();
        if (cacheNodeName == null  || cacheNodeName.isEmpty())
        {
            throw new RuntimeException("Unable to reboot elasti cache cache node , no  instance available");
        }

        logger.info("ElasitCache - rebooting cache node: " + cacheNodeName );
        elastiCacheRaider.rebootElasticCacheNode(cacheNodeName);
        return this;

    }

    /**
     * Assert elasti cache node status.
     *
     * @param expectedStatus the expected status
     */
    @Then("^assertElastiCache node status \"([^\"]*)\"$")
    public void assertElastiCacheNodeStatus(String expectedStatus){
        String cacheNodeName = executionStateCache.getCacheNodeName();
        if (cacheNodeName == null  || cacheNodeName.isEmpty())
        {
            throw new RuntimeException("Unable to reboot elasti cache cache node , no  instance available");
        }
        org.testng.Assert.assertEquals(elastiCacheRaider.getElastiCacheNodeStatus(cacheNodeName), expectedStatus);
    }


}
