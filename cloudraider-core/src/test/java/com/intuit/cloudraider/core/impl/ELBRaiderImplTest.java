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

import com.amazonaws.services.ec2.model.GroupIdentifier;
import com.amazonaws.services.ec2.model.InstanceState;
import com.amazonaws.services.ec2.model.Placement;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancing;
import com.amazonaws.services.elasticloadbalancing.model.*;
import com.intuit.cloudraider.commons.LoadBalancerDelegator;
import com.intuit.cloudraider.exceptions.UnSupportedFeatureException;
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
import java.util.Date;
import java.util.List;
import java.util.Map;


@PowerMockIgnore({"javax.management.*","javax.net.ssl.*"})
@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@PrepareForTest({LoadBalancerRaiderImpl.class, Credentials.class, LoadBalancerDelegator.class})
public class ELBRaiderImplTest {

    @Autowired
    private  LoadBalancerRaiderImpl loadBalancerRaider;

    @Autowired
    private LoadBalancerDelegator loadBalancerDelegator;

    private AmazonElasticLoadBalancing amazonElasticLoadBalancing;
    private LoadBalancerDescription loadBalancerDescription;
    private DescribeLoadBalancersResult describeLoadBalancersResult1;
    private DescribeLoadBalancersRequest describeLoadBalancersRequest;
    private RegisterInstancesWithLoadBalancerResult registerInstancesWithLoadBalancerResult;

    /**
     * Sets test.
     *
     * @throws Exception the exception
     */
    @Before
    public void setupTest() throws Exception {

        amazonElasticLoadBalancing = PowerMockito.mock(AmazonElasticLoadBalancing.class);
        PowerMockito.when(loadBalancerDelegator.getAmazonElasticLoadBalancing()).thenReturn(amazonElasticLoadBalancing);

        List<String> availablityZone=new ArrayList<>();
        availablityZone.add("US-WEST-2");
        availablityZone.add("US-EAST-2");

        Listener listener=new Listener();
        listener.setInstancePort(8080);
        listener.setLoadBalancerPort(80);
        listener.setProtocol("ssl");

        HealthCheck healthCheck=new HealthCheck();
        healthCheck.setInterval(5);
        healthCheck.setTarget("mytarget");
        healthCheck.setTimeout(3);
        healthCheck.setHealthyThreshold(2);
        healthCheck.setUnhealthyThreshold(2);

        List<Listener> createListener=new ArrayList<>();
        createListener.add(listener);
        ListenerDescription listenerDescription=new ListenerDescription();
        listenerDescription.setListener(listener);

        List<ListenerDescription> listeners=new ArrayList<>();
        listeners.add(listenerDescription);

        List<String> SG=new ArrayList<>();
        SG.add("SG1");
        SG.add("SG2");
        SG.add("SG3");

        List<String> subnet=new ArrayList<>();
        subnet.add("1-abcd");
        subnet.add("2-abcd");

        com.amazonaws.services.ec2.model.Tag tag = new com.amazonaws.services.ec2.model.Tag().withKey("Name").withValue("tag2");
        List<com.amazonaws.services.ec2.model.Tag> tags = new ArrayList<>();
        tags.add(tag);

        com.amazonaws.services.ec2.model.Instance inst=new com.amazonaws.services.ec2.model.Instance();
        inst.setInstanceId("99999");
        inst.setTags(tags);
        inst.setState(new InstanceState().withName("Running"));
        inst.setPrivateIpAddress("10.1.1.1");
        inst.setPlacement(new Placement().withAvailabilityZone("us-west-2c"));
        GroupIdentifier sg = new GroupIdentifier();
        sg.setGroupId("sg-123");
        ArrayList securityGroups = new ArrayList();
        securityGroups.add(sg);
        inst.setSecurityGroups(securityGroups);

        DescribeLoadBalancerAttributesRequest describeLoadBalancerAttributesRequest=new DescribeLoadBalancerAttributesRequest();
        describeLoadBalancerAttributesRequest.setLoadBalancerName("helloTest");
        describeLoadBalancerAttributesRequest.setSdkRequestTimeout(5);

        Policies policies=new Policies();

        SourceSecurityGroup sourceSecurityGroup=new SourceSecurityGroup();

        Instance myInstance=new Instance();
        myInstance.setInstanceId("99999");

        List<Instance> instances=new ArrayList<>();
        instances.add(myInstance);

        loadBalancerDescription = new LoadBalancerDescription();
        loadBalancerDescription.setLoadBalancerName("helloTest");
        loadBalancerDescription.setAvailabilityZones(availablityZone);
        loadBalancerDescription.setListenerDescriptions(listeners);
//        loadBalancerDescription.setSecurityGroups(SG);
        loadBalancerDescription.setSubnets(subnet);
        loadBalancerDescription.setScheme("anything");
        loadBalancerDescription.setDNSName("hello.com");
        loadBalancerDescription.setPolicies(policies);
        loadBalancerDescription.setVPCId("12344");
        loadBalancerDescription.setHealthCheck(healthCheck);
        loadBalancerDescription.setCanonicalHostedZoneName("none");
        loadBalancerDescription.setCanonicalHostedZoneNameID("33333");
        loadBalancerDescription.setCreatedTime(new Date());
        loadBalancerDescription.setSourceSecurityGroup(sourceSecurityGroup);
        loadBalancerDescription.setInstances(instances);

        List<String> ELBs=new ArrayList<>();
        ELBs.add("helloTest");

        describeLoadBalancersRequest = new DescribeLoadBalancersRequest();
        describeLoadBalancersRequest.setLoadBalancerNames(ELBs);

        RegisterInstancesWithLoadBalancerRequest registerInstancesWithLoadBalancerRequest=new RegisterInstancesWithLoadBalancerRequest();
        registerInstancesWithLoadBalancerRequest.setInstances(instances);
        registerInstancesWithLoadBalancerRequest.setLoadBalancerName("helloTest");

        registerInstancesWithLoadBalancerResult=PowerMockito.mock(RegisterInstancesWithLoadBalancerResult.class);
        PowerMockito.when(loadBalancerDelegator.getAmazonElasticLoadBalancing().registerInstancesWithLoadBalancer(registerInstancesWithLoadBalancerRequest)).thenReturn(registerInstancesWithLoadBalancerResult);
        PowerMockito.when(loadBalancerDelegator.getAmazonElasticLoadBalancing().registerInstancesWithLoadBalancer(Mockito.anyObject())).thenReturn(registerInstancesWithLoadBalancerResult);

        describeLoadBalancersResult1=PowerMockito.mock(DescribeLoadBalancersResult.class);
        PowerMockito.when(loadBalancerDelegator.getAmazonElasticLoadBalancing().describeLoadBalancers(describeLoadBalancersRequest)).thenReturn(describeLoadBalancersResult1);
        PowerMockito.when(loadBalancerDelegator.getAmazonElasticLoadBalancing().describeLoadBalancers(Mockito.anyObject())).thenReturn(describeLoadBalancersResult1);

//        LoadBalancer loadBalancer=new LoadBalancer();
//        loadBalancer.setLoadBalancerName("helloTest");
//        loadBalancer.setContainerName("tomcat");
//        loadBalancer.setContainerPort(8080);
//        loadBalancer.setTargetGroupArn("arn:hello:arn");

        List<LoadBalancerDescription> loadBalancerDescriptions= new ArrayList<>();
        loadBalancerDescriptions.add(loadBalancerDescription);
        PowerMockito.when(loadBalancerDelegator.getAmazonElasticLoadBalancing().describeLoadBalancers(describeLoadBalancersRequest).getLoadBalancerDescriptions()).thenReturn(loadBalancerDescriptions);
        PowerMockito.when(loadBalancerDelegator.getAmazonElasticLoadBalancing().describeLoadBalancers(Mockito.anyObject()).getLoadBalancerDescriptions()).thenReturn(loadBalancerDescriptions);

        DescribeInstanceHealthRequest describeInstanceHealthRequest=new DescribeInstanceHealthRequest();
        describeInstanceHealthRequest.setInstances(instances);
        describeInstanceHealthRequest.setLoadBalancerName("helloTest");

        DescribeInstanceHealthResult describeInstanceHealthResult=PowerMockito.mock(DescribeInstanceHealthResult.class);
        PowerMockito.when(loadBalancerDelegator.getAmazonElasticLoadBalancing().describeInstanceHealth(describeInstanceHealthRequest)).thenReturn(describeInstanceHealthResult);
        PowerMockito.when(loadBalancerDelegator.getAmazonElasticLoadBalancing().describeInstanceHealth(Mockito.anyObject())).thenReturn(describeInstanceHealthResult);

    }


    /**
     * Delete load balancer test.
     */
    @Test
    public void deleteLoadBalancerTest(){
        
        loadBalancerRaider.deleteLoadBalancer("helloTest");
    }

    /**
     * Add security group test.
     */
    @Test
    public void addSecurityGroupTest(){

        loadBalancerRaider.addSecurityGroup("helloTest","mysec");
        Assert.assertTrue(loadBalancerRaider.getSecurityGroups("helloTest").contains("mysec"));

    }

    /**
     * Add security groups test.
     */
    @Test
    public void addSecurityGroupsTest(){


        loadBalancerRaider.addSecurityGroups("helloTest","group1");
        Assert.assertTrue(loadBalancerRaider.getSecurityGroups("helloTest").contains("group1"));

    }

    /**
     * Delete security group test.
     */
    @Test
    public void deleteSecurityGroupTest(){

        loadBalancerRaider.deleteSecurityGroup("helloTest","mysec");
        Assert.assertTrue(!loadBalancerRaider.getSecurityGroups("helloTest").contains("mysec"));
    }

    /**
     * Delete security groups test.
     */
    @Test
    public void deleteSecurityGroupsTest(){

        loadBalancerRaider.deleteSecurityGroups("helloTest","group1");
        Assert.assertTrue(!loadBalancerRaider.getSecurityGroups("helloTest").contains("group1"));
    }


    /**
     * Deleteunexisting load balancer.
     */
    @Test//(expected = ResourceNotFoundException.class)
    public void deleteunexistingLoadBalancer()
    {

        loadBalancerRaider.deleteLoadBalancer("invalidElb");
    }


    /**
     * Delete load balancer listener test.
     */
    @Test
    public void deleteLoadBalancerListenerTest()
    {
        loadBalancerRaider.deleteLoadBalancerListeners("helloTest",80);

    }

    /**
     * Update loadbalancer listner test.
     */
    @Test(expected = UnSupportedFeatureException.class)
    public void updateLoadbalancerListnerTest()
    {
        loadBalancerRaider.updateLoadbalancerListner("helloTest",80,81);
    }

    /**
     * Deregister instances from load balancer test.
     */
    @Test
    public void deregisterInstancesFromLoadBalancerTest()
    {
        loadBalancerRaider.deregisterInstancesFromLoadBalancer("helloTest",1);

    }

    /**
     * Deregistermultiple instance from load balancer test.
     */
    @Test
    public void deregistermultipleInstanceFromLoadBalancerTest()
    {
        List<String> instances=new ArrayList<>();
        instances.add("tag2");

        loadBalancerRaider.deregisterInstancesFromLoadBalancer("helloTest",instances);

    }

    /**
     * Register instances from load balancer test.
     */
    @Test
    public void registerInstancesFromLoadBalancerTest()
    {
        List<String> instances=new ArrayList<>();
        instances.add("tag2");

        loadBalancerRaider.registerInstancesFromLoadBalancer("helloTest",instances);
    }

    /**
     * Deregister instance from load balancer test.
     */
    @Test
    public void deregisterInstanceFromLoadBalancerTest()
    {
        loadBalancerRaider.deregisterInstancesFromLoadBalancer("helloTest");
    }

    /**
     * Attach load balancer to subnets test.
     */
    @Test
    public void attachLoadBalancerToSubnetsTest()
    {

        loadBalancerRaider.attachLoadBalancerToSubnets("helloTest","3-abcd");
        Assert.assertTrue(describeLoadBalancersResult1.getLoadBalancerDescriptions().get(0).getSubnets().size()==2);

    }

    /**
     * Detach load balancer from subnets test.
     */
    @Test
    public void detachLoadBalancerFromSubnetsTest()
    {
        loadBalancerRaider.detachLoadBalancerFromSubnets("helloTest","2-abcd");
        Assert.assertTrue(describeLoadBalancersResult1.getLoadBalancerDescriptions().get(0).getSubnets().size()==2);
    }

    /**
     * Delete load balancer policy test.
     */
    @Test
    public void deleteLoadBalancerPolicyTest()
    {
        loadBalancerRaider.deleteLoadBalancerPolicy("helloTest","myPolicy");
    }

    /**
     * Disable availability zones for load balancer test.
     */
    @Test
    public void disableAvailabilityZonesForLoadBalancerTest()
    {
        loadBalancerRaider.disableAvailabilityZonesForLoadBalancer("helloTest","us-west-c2");

    }

    /**
     * Enable availability zones for load balancer test.
     */
    @Test
    public void enableAvailabilityZonesForLoadBalancerTest()
    {
        loadBalancerRaider.enableAvailabilityZonesForLoadBalancer("helloTest","US-EAST-2");
    }

    /**
     * Gets load balancer instances test.
     */
    @Test
    public void getLoadBalancerInstancesTest()
    {
        List<Instance> instances=loadBalancerRaider.getLoadBalancerInstances("helloTest");
        Assert.assertTrue(describeLoadBalancersResult1.getLoadBalancerDescriptions().get(0).getInstances().containsAll(instances));
    }

    /**
     * Change load balancer name test.
     */
    @Test(expected = UnSupportedFeatureException.class)
    public void changeLoadBalancerNameTest()
    {
        loadBalancerRaider.changeLoadBalancerName("helloTest","MyTest");
    }

    /**
     * Gets in service instances test.
     */
    @Test
    public void getInServiceInstancesTest()
    {
        loadBalancerRaider.getInServiceInstances("helloTest");
    }

    /**
     * Gets out service instances test.
     */
    @Test
    public void getOutServiceInstancesTest()
    {
        loadBalancerRaider.getOutOfServiceInstances("helloTest");
    }

    /**
     * Gets load balancer names test.
     */
    @Test
    public void getLoadBalancerNamesTest() {
        Map<String, String> map = loadBalancerRaider.getLoadBalancerNames();
        Assert.assertEquals(1, map.size());
    }


    /**
     * The type Elb raider impl test context configuration.
     */
    @Configuration
    protected static class ELBRaiderImplTestContextConfiguration {

        /**
         * Load balancer delegator load balancer delegator.
         *
         * @return the load balancer delegator
         */
        @Bean
        public LoadBalancerDelegator loadBalancerDelegator() {
            return Mockito.mock(LoadBalancerDelegator.class);
        }

        /**
         * Load balancer raider load balancer raider.
         *
         * @return the load balancer raider
         */
        @Bean
        public LoadBalancerRaiderImpl loadBalancerRaiderImpl() {
            return new LoadBalancerRaiderImpl();
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
