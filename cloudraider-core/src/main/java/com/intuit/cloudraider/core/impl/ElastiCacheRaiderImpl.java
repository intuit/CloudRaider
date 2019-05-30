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

import com.amazonaws.services.elasticache.model.*;
import com.intuit.cloudraider.commons.ElastiCacheDelegator;
import com.intuit.cloudraider.core.interfaces.ElastiCacheRaider;
import com.intuit.cloudraider.exceptions.InvalidInputDataException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * AWS ElastiCache functionality.
 * <p>
  * The type Elasti cache raider.
 */
@Component(value="elastiCacheRaiderBean")
public class ElastiCacheRaiderImpl implements ElastiCacheRaider {

    /**
     * The Logger.
     */
    Logger logger = LoggerFactory.getLogger(this.getClass());
     
     @Autowired
     private ElastiCacheDelegator elastiCacheDelegator;

    /**
     * Instantiates a new Elasti cache raider.
     */
    public ElastiCacheRaiderImpl() {

    }

    /**
     * Reboot the given number of nodes on the cluster.
     *
     * @param clusterName cluster name
     * @param numNodes number of nodes
     */
    @Override
    public void rebootElastiCacheCluster(String clusterName, Integer numNodes) {
        RebootCacheClusterRequest rebootCacheClusterRequest = new RebootCacheClusterRequest()
                .withCacheClusterId(clusterName)
                .withCacheNodeIdsToReboot(this.getCachedNodeId(numNodes));

        try {
            elastiCacheDelegator.getAmazonElastiCache().rebootCacheCluster(rebootCacheClusterRequest);
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }

    /**
     * Detach the given subnet from the cluster.
     *
     * @param clusterName cluster name
     * @param subnetId subnet id
     */
    @Override
    public void detachSubnet(String clusterName, String subnetId) {

        if (clusterName.isEmpty() || clusterName == null) {
            throw new InvalidInputDataException("Empty/Null clusterName provided in request");
        } else if (subnetId.isEmpty() || subnetId == null) {
            throw new InvalidInputDataException("Empty/Null subnetId provided in request");
        }

        List<String> subnetIds = this.getSubnetIds(clusterName);

        if (subnetIds != null && subnetIds.contains(subnetId)) {
            subnetIds.remove(subnetId);


            ModifyCacheSubnetGroupRequest modifyCacheSubnetGroupRequest = new ModifyCacheSubnetGroupRequest()
                    .withCacheSubnetGroupName(getSubnetGroupName(clusterName))
                    .withSubnetIds(subnetIds);

            elastiCacheDelegator.getAmazonElastiCache().modifyCacheSubnetGroup(modifyCacheSubnetGroupRequest);

        } else {
            throw new com.intuit.cloudraider.exceptions.ResourceNotFoundException("Unable to detach Subnet: " + subnetId);
        }
    }

    /**
     * Attach the given subnet to the cluster.
     *
     * @param clusterName cluster name
     * @param subnetId subnet id
     */
    @Override
    public void attachSubnet(String clusterName, String subnetId) {

        if (clusterName.isEmpty() || clusterName == null) {
            throw new InvalidInputDataException("Empty/Null clusterName provided in request");
        } else if (subnetId.isEmpty() || subnetId == null) {
            throw new InvalidInputDataException("Empty/Null subnetId provided in request");
        }

        List<String> subnetIds = this.getSubnetIds(clusterName);


        if (subnetIds != null && !subnetIds.contains(subnetId)) {
            subnetIds.add(subnetId);


            ModifyCacheSubnetGroupRequest modifyCacheSubnetGroupRequest = new ModifyCacheSubnetGroupRequest()
                    .withCacheSubnetGroupName(getSubnetGroupName(clusterName))
                    .withSubnetIds(subnetIds);

            elastiCacheDelegator.getAmazonElastiCache().modifyCacheSubnetGroup(modifyCacheSubnetGroupRequest);
        } else {
            throw new com.intuit.cloudraider.exceptions.ResourceNotFoundException("Unable to attach Subnet: " + subnetId);
        }

    }

    /**
     * Get the status of the cluster.
     *
     * @param clusterName cluster name
     * @return cluster status
     */
    @Override
    public String getElastiCacheClusterStatus(String clusterName) {

        DescribeCacheClustersRequest describeCacheClustersRequest = new DescribeCacheClustersRequest()
                .withCacheClusterId(clusterName);

        return elastiCacheDelegator.getAmazonElastiCache().describeCacheClusters(describeCacheClustersRequest)
                .getCacheClusters()
                .get(0)
                .getCacheClusterStatus();
    }

    /**
     * Generate node ids for the cluster, with a 4 digit format.
     *
     * @param numNodes number of nodes
     * @return list of node ids
     */
    private List<String> getCachedNodeId(Integer numNodes) {
        List<String> nodeIds = new ArrayList<String>();
        IntStream.range(1, numNodes + 1)
                .sequential()
                .forEach(
                        i -> nodeIds.add(String.format("%04d", i))
                );
        return nodeIds;
    }

    /**
     * Add the given number of nodes to the cluster.
     *
     * @param clusterName cluster name
     * @param numNodes number of nodes
     */
    @Override
    public void addNodesToCluster(String clusterName, Integer numNodes) {

        ModifyCacheClusterRequest modifyCacheClusterRequest = new ModifyCacheClusterRequest()
                .withCacheClusterId(clusterName)
                .withNumCacheNodes(numNodes + getNumNodes(clusterName))
                .withApplyImmediately(true);

        try {
            elastiCacheDelegator.getAmazonElastiCache().modifyCacheCluster(modifyCacheClusterRequest);

        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage());
        }

    }

    /**
     * Get the number of nodes attached to the given cluster.
     *
     * @param clusterName cluster name
     * @return number of nodes
     */
    public Integer getNumNodes(String clusterName) {

        DescribeCacheClustersRequest describeCacheClustersRequest = new DescribeCacheClustersRequest()
                .withCacheClusterId(clusterName);

        return elastiCacheDelegator.getAmazonElastiCache().describeCacheClusters(describeCacheClustersRequest)
                .getCacheClusters()
                .get(0)
                .getNumCacheNodes();
    }

    /**
     * Remove the given number of nodes from the cluster.
     *
     * @param clusterName cluster name
     * @param numNodes number of nodes
     */
    @Override
    public void removeNodesFromCluster(String clusterName, Integer numNodes) {

        ModifyCacheClusterRequest modifyCacheClusterRequest = new ModifyCacheClusterRequest()
                .withCacheClusterId(clusterName)
                .withNumCacheNodes(getNumNodes(clusterName) - numNodes)
                .withCacheNodeIdsToRemove(getCachedNodeId(numNodes))
                .withApplyImmediately(true);

        try {
            elastiCacheDelegator.getAmazonElastiCache().modifyCacheCluster(modifyCacheClusterRequest);
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }

    /**
     * Gets the subnets attached to the given cluster.
     *
     * @param clusterName cluster name
     * @return list of subnet ids
     */
    public List<String> getSubnetIds(String clusterName) {
        List<String> subnetIds = new ArrayList<String>();
        List<Subnet> subnets = elastiCacheDelegator.getAmazonElastiCache().describeCacheSubnetGroups(new DescribeCacheSubnetGroupsRequest()
                .withCacheSubnetGroupName(getSubnetGroupName(clusterName)))
                .getCacheSubnetGroups()
                .get(0)
                .getSubnets();
        subnets.forEach(subnet -> subnetIds.add(subnet.getSubnetIdentifier()));
        return subnetIds;

    }

    /**
     * Gets the name of the subnet group given the cluster name.
     *
     * @param clusterName cluster name
     * @return name of subnet group
     */
    private String getSubnetGroupName(String clusterName) {
        return elastiCacheDelegator.getAmazonElastiCache().describeCacheClusters(new DescribeCacheClustersRequest().withCacheClusterId(clusterName))
                .getCacheClusters()
                .get(0)
                .getCacheSubnetGroupName();
    }

    /**
     * Change the cluster's security group from "fromSecurityGroupId" to "toSecurityGroupId"
     *
     * @param clusterName cluster name
     * @param fromSecurityGroupId old security group id
     * @param toSecurityGroupId new security group id
     */
    @Override
    public void changeSecurityGroupId(String clusterName, String fromSecurityGroupId, String toSecurityGroupId) {

        List<String> securityGroupIds = this.getSecurityGroups(clusterName);
        securityGroupIds.add(toSecurityGroupId);
        securityGroupIds.remove(fromSecurityGroupId);

        ModifyCacheClusterRequest modifyCacheClusterRequest = new ModifyCacheClusterRequest()
                .withCacheClusterId(clusterName)
                .withSecurityGroupIds(securityGroupIds)
                .withApplyImmediately(true);

        try {
            elastiCacheDelegator.getAmazonElastiCache().modifyCacheCluster(modifyCacheClusterRequest);
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage());
        }

    }

    /**
     * Gets the security groups attached to the given cluster.
     *
     * @param clusterName cluster name
     * @return list of security group ids
     */
    @Override
    public List<String> getSecurityGroups(String clusterName) {
        List<String> secGroupIds = elastiCacheDelegator.getAmazonElastiCache().describeCacheClusters(new DescribeCacheClustersRequest()
                .withCacheClusterId(clusterName))
                .getCacheClusters()
                .get(0)
                .getSecurityGroups()
                .stream()
                .map(SecurityGroupMembership::getSecurityGroupId)
                .collect(Collectors.toList());

        return secGroupIds;

    }


    public List<CacheNode> getAllElasticCacheNodes() {

        DescribeCacheClustersRequest describeCacheClustersRequest = new DescribeCacheClustersRequest();
        describeCacheClustersRequest.setShowCacheNodeInfo(true);

        DescribeCacheClustersResult describeCacheClustersResult = elastiCacheDelegator.getAmazonElastiCache().describeCacheClusters(describeCacheClustersRequest);

        List<CacheNode> elasticCacheNodes = describeCacheClustersResult.getCacheClusters()
                .parallelStream()
                .map( x -> x.getCacheNodes())
                .flatMap(x -> x.stream())
                .collect(Collectors.toList());
        return elasticCacheNodes;
    }


    public List<CacheNode> getAllElasticCacheNodesForGivenCluster(String clusterName) {

        DescribeCacheClustersRequest describeCacheClustersRequest = new DescribeCacheClustersRequest().withCacheClusterId(clusterName);
        describeCacheClustersRequest.setShowCacheNodeInfo(true);

        DescribeCacheClustersResult describeCacheClustersResult = elastiCacheDelegator.getAmazonElastiCache().describeCacheClusters(describeCacheClustersRequest);

        List<CacheNode> elasticCacheNodes = describeCacheClustersResult.getCacheClusters()
                .parallelStream()
                .map( x -> x.getCacheNodes())
                .flatMap(x -> x.stream())
                .collect(Collectors.toList());
        return elasticCacheNodes;
    }

    @Override
    public List<String> getAllElastiCacheNodeNames() {

        List<String> elasticCacheNodeNames = getAllElasticCacheNodes()
                .parallelStream()
                .map( i -> i.getEndpoint().getAddress())
                .collect(Collectors.toList());

       logger.info("ElastiCacherRaider - Cached node names:" + elasticCacheNodeNames);

        return elasticCacheNodeNames;

    }

    @Override
    public List<String> getAllElastiCacheNodeNames(String clusterName) {

        List<String> elasticCacheNodeNames = getAllElasticCacheNodesForGivenCluster(clusterName)
                .parallelStream()
                .map( i -> i.getEndpoint().getAddress())
                .collect(Collectors.toList());

        logger.info("ElastiCacherRaider - Cached node names:" + elasticCacheNodeNames);


        return elasticCacheNodeNames;
    }

    @Override
    public String getElastiCacheNodeStatus(String nodeName) {

        String nodeStatus = getAllElasticCacheNodes()
                .parallelStream()
                .filter( x -> x.getEndpoint().getAddress().equalsIgnoreCase(nodeName))
                .findFirst()
                .get()
                .getCacheNodeStatus();

        return nodeStatus;
    }

    @Override
    public void rebootElasticCacheNode(String nodeName) {

        if (nodeName == null || nodeName.isEmpty() )
        {
            throw new InvalidInputDataException("Null/Empty node name");
        }

        DescribeCacheClustersRequest describeCacheClustersRequest = new DescribeCacheClustersRequest();
        describeCacheClustersRequest.setShowCacheNodeInfo(true);

        DescribeCacheClustersResult clusterResult = elastiCacheDelegator.getAmazonElastiCache().describeCacheClusters(describeCacheClustersRequest);

        List<CacheCluster> cacheClusters = clusterResult.getCacheClusters();

        for (CacheCluster cacheCluster : cacheClusters) {
            List<CacheNode> cacheNodes = cacheCluster.getCacheNodes();

            for (CacheNode cacheNode : cacheNodes) {
                if ( cacheNode.getEndpoint().getAddress().equals(nodeName) ) {

                    RebootCacheClusterRequest request = new RebootCacheClusterRequest().withCacheClusterId(cacheCluster.getCacheClusterId()).withCacheNodeIdsToReboot(cacheNode.getCacheNodeId());
                    CacheCluster response = elastiCacheDelegator.getAmazonElastiCache().rebootCacheCluster(request);
                    if (response == null)
                    {
                        throw new com.intuit.cloudraider.exceptions.ResourceNotFoundException("Unable to reboot Ec instance: "+nodeName);
                    }
                    else
                    {
                        break;
                    }


                }
            }
        }
    }
}
