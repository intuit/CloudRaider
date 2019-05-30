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

import com.amazonaws.services.elasticache.AmazonElastiCache;
import com.amazonaws.services.elasticache.model.*;
import com.intuit.cloudraider.commons.ElastiCacheDelegator;
import com.intuit.cloudraider.core.interfaces.ElastiCacheRaider;
import com.intuit.cloudraider.exceptions.InvalidInputDataException;
import com.intuit.cloudraider.exceptions.ResourceNotFoundException;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The type Elasti cache raider impl test.
 */
@PowerMockIgnore({"javax.management.*","javax.net.ssl.*"})
@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@PrepareForTest({ElastiCacheRaiderImpl.class,Credentials.class, ElastiCacheDelegator.class})
public class ElastiCacheRaiderImplTest {

    @Autowired
    private  ElastiCacheRaider elastiCacheRaider;

    @Autowired
    private ElastiCacheDelegator elastiCacheDelegator;


    private  AmazonElastiCache amazonElastiCache;
    private CacheCluster cacheCluster;

    /**
     * Sets method.
     *
     * @throws Exception the exception
     */
    @Before
    public void setupMethod() throws Exception{

        amazonElastiCache = PowerMockito.mock(AmazonElastiCache.class);
        PowerMockito.when(elastiCacheDelegator.getAmazonElastiCache()).thenReturn(amazonElastiCache);

        DescribeCacheClustersResult describeCacheClustersResult = PowerMockito.mock(DescribeCacheClustersResult.class);
        PowerMockito.when(elastiCacheDelegator.getAmazonElastiCache().describeCacheClusters()).thenReturn(describeCacheClustersResult);
        PowerMockito.when(elastiCacheDelegator.getAmazonElastiCache().describeCacheClusters(Mockito.anyObject())).thenReturn(describeCacheClustersResult);

        cacheCluster = new CacheCluster();
        cacheCluster.setCacheClusterId("cacheCluster");
        cacheCluster.setCacheClusterStatus("available");

        CacheNode cacheNode = new CacheNode();
        cacheNode.setCacheNodeId("node-123");
        cacheCluster.setCacheNodes(Arrays.asList(cacheNode));
        cacheCluster.setNumCacheNodes(2);

        Subnet subnet = new Subnet().withSubnetIdentifier("subnet-123");

        CacheSubnetGroup cacheSubnetGroup = new CacheSubnetGroup();
        cacheSubnetGroup.setSubnets(Arrays.asList(subnet));


        DescribeCacheSubnetGroupsResult describeCacheSubnetGroupsResult = PowerMockito.mock(DescribeCacheSubnetGroupsResult.class);
        PowerMockito.when(amazonElastiCache.describeCacheSubnetGroups()).thenReturn(describeCacheSubnetGroupsResult);
        PowerMockito.when(amazonElastiCache.describeCacheSubnetGroups(Mockito.anyObject())).thenReturn(describeCacheSubnetGroupsResult);
        PowerMockito.when(describeCacheSubnetGroupsResult.getCacheSubnetGroups()).thenReturn(Arrays.asList(cacheSubnetGroup));


        SecurityGroupMembership securityGroupMembership = new SecurityGroupMembership();
        securityGroupMembership.setSecurityGroupId("sg-123");
        securityGroupMembership.setStatus("active");

        cacheCluster.setSecurityGroups(Arrays.asList(securityGroupMembership));

        List<CacheCluster> cacheClusterList = new ArrayList<>();
        cacheClusterList.add(cacheCluster);

        PowerMockito.when(describeCacheClustersResult.getCacheClusters()).thenReturn(cacheClusterList);

    }


    /**
     * Test reboot elasti cache cluster.
     */
    @Test
    public void testRebootElastiCacheCluster(){
        PowerMockito.when(amazonElastiCache.rebootCacheCluster(Mockito.anyObject())).thenReturn(cacheCluster);
        elastiCacheRaider.rebootElastiCacheCluster("cacheCluster", 1);
    }

    /**
     * Test detach subnet.
     */
    @Test
    public void testDetachSubnet(){
        elastiCacheRaider.detachSubnet("cacheCluster", "subnet-123");
    }

    /**
     * Test detach unknown subnet.
     */
    @Test (expected = ResourceNotFoundException.class)
    public void testDetachUnknownSubnet(){
        elastiCacheRaider.detachSubnet("cacheCluster", "cacheSubnet-123");
    }

    /**
     * Test detach missing subnet.
     */
    @Test (expected = InvalidInputDataException.class)
    public void testDetachMissingSubnet(){
        elastiCacheRaider.detachSubnet("cacheCluster", "");
    }

    /**
     * Test detach subnet missing cluster.
     */
    @Test (expected = InvalidInputDataException.class)
    public void testDetachSubnetMissingCluster(){
        elastiCacheRaider.detachSubnet("", "subnet-123");
    }

    /**
     * Test attach subnet.
     */
    @Test
    public void testAttachSubnet(){
        elastiCacheRaider.attachSubnet("cacheCluster", "subnet-1234");
    }

    /**
     * Test attach unknown subnet.
     */
    @Test (expected = ResourceNotFoundException.class)
    public void testAttachUnknownSubnet(){
        elastiCacheRaider.attachSubnet("cacheCluster", "subnet-123");
    }

    /**
     * Test attach missing subnet.
     */
    @Test (expected = InvalidInputDataException.class)
    public void testAttachMissingSubnet(){
        elastiCacheRaider.attachSubnet("cacheCluster", "");
    }

    /**
     * Test attach subnet missing cluster.
     */
    @Test (expected = InvalidInputDataException.class)
    public void testAttachSubnetMissingCluster(){
        elastiCacheRaider.attachSubnet("", "subnet-1234");
    }


    /**
     * Test elasti cache cluster status.
     */
    @Test
    public void testElastiCacheClusterStatus(){
        String status = elastiCacheRaider.getElastiCacheClusterStatus("cacheCluster");
        Assert.assertEquals(status, "available");
    }

    /**
     * Test get security groups.
     */
    @Test
    public void testGetSecurityGroups(){
        List<String> secGroupIds = elastiCacheRaider.getSecurityGroups("cacheCluster");
        Assert.assertEquals(secGroupIds.get(0), "sg-123");
    }

    /**
     * Test change security group id.
     */
    @Test
    public void testChangeSecurityGroupId(){
        elastiCacheRaider.changeSecurityGroupId("cacheCluster", "sg-123", "sg-456");
    }

    /**
     * Test add nodes to cluster.
     */
    @Test
    public void testAddNodesToCluster(){
        elastiCacheRaider.addNodesToCluster("cacheCluster", 1);
    }

    /**
     * Test remove nodes from cluster.
     */
    @Test
    public void testRemoveNodesFromCluster(){
        elastiCacheRaider.removeNodesFromCluster("cacheCluster", 1);
    }


    /**
     * The type Ec 2 raider impl test context configuration.
     */
    @Configuration
    protected static class EC2RaiderImplTestContextConfiguration {

        /**
         * Elasti cache delegator elasti cache delegator.
         *
         * @return the elasti cache delegator
         */
        @Bean
        public ElastiCacheDelegator elastiCacheDelegator() {
            return Mockito.mock(ElastiCacheDelegator.class);
        }

        /**
         * Elasti cache raider impl under test elasti cache raider.
         *
         * @return the elasti cache raider
         */
        @Bean
        public ElastiCacheRaiderImpl elastiCacheRaiderImplUnderTest() {
            return new ElastiCacheRaiderImpl();
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



    }

}
