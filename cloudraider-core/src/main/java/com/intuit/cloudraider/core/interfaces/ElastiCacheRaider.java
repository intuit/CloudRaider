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

import com.amazonaws.services.elasticache.model.CacheNode;

import java.util.List;

/**
 * AWS ElastiCache functionality.
 */
public interface ElastiCacheRaider {

    /**
     * Reboot the given number of nodes on the cluster.
     *
     * @param clusterName cluster name
     * @param numNodes    number of nodes
     */
    public void rebootElastiCacheCluster(String clusterName, Integer numNodes);

    /**
     * Detach the given subnet from the cluster.
     *
     * @param clusterName cluster name
     * @param subnetId    subnet id
     */
    public void detachSubnet(String clusterName, String subnetId);

    /**
     * Attach the given subnet to the cluster.
     *
     * @param clusterName cluster name
     * @param subnetId    subnet id
     */
    public void attachSubnet(String clusterName, String subnetId);

    /**
     * Get the status of the cluster.
     *
     * @param clusterName cluster name
     * @return cluster status
     */
    public String getElastiCacheClusterStatus(String clusterName);

    /**
     * Add the given number of nodes to the cluster.
     *
     * @param clusterName cluster name
     * @param numNodes    number of nodes
     */
    public void addNodesToCluster(String clusterName, Integer numNodes);

    /**
     * Remove the given number of nodes from the cluster.
     *
     * @param clusterName cluster name
     * @param numNodes    number of nodes
     */
    public void removeNodesFromCluster(String clusterName, Integer numNodes);

    /**
     * Gets the security groups attached to the given cluster.
     *
     * @param clusterName cluster name
     * @return list of security group ids
     */
    public List<String> getSecurityGroups(String clusterName);

    /**
     * Change the cluster's security group from "fromSecurityGroupId" to "toSecurityGroupId"
     *
     * @param clusterName         cluster name
     * @param fromSecurityGroupId old security group id
     * @param toSecurityGroupId   new security group id
     */
    public void changeSecurityGroupId(String clusterName, String fromSecurityGroupId, String toSecurityGroupId);


    /**
     * Gets all elasti cache node names.
     *
     * @return the all elasti cache node names
     */
    public List<String> getAllElastiCacheNodeNames();

    /**
     * Gets all elasti cache node names.
     *
     * @param clusterName the cluster name
     * @return the all elasti cache node names
     */
    public List<String> getAllElastiCacheNodeNames(String clusterName);

    /**
     * Gets elasti cache node status.
     *
     * @param nodeName the node name
     * @return the elasti cache node status
     */
    public String getElastiCacheNodeStatus(String nodeName);

    /**
     * Reboot elastic cache node.
     *
     * @param nodeName the node name
     */
    public void rebootElasticCacheNode(String nodeName);


    /**
     * Gets all elastic cache nodes.
     *
     * @return the all elastic cache nodes
     */
    public List<CacheNode> getAllElasticCacheNodes();

    /**
     * Get all elastic cache nodes for given cluster list.
     *
     * @param clusterName the cluster name
     * @return the list
     */
    public List<CacheNode> getAllElasticCacheNodesForGivenCluster(String clusterName);


}


