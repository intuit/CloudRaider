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
import com.amazonaws.services.ec2.model.Placement;
import com.amazonaws.services.elasticloadbalancing.model.Instance;
import com.amazonaws.services.elasticloadbalancing.model.Listener;
import com.amazonaws.services.elasticloadbalancing.model.ListenerDescription;
import com.amazonaws.services.elasticloadbalancing.model.LoadBalancerDescription;
import com.amazonaws.services.elasticloadbalancingv2.AmazonElasticLoadBalancing;
import com.amazonaws.services.elasticloadbalancingv2.model.*;
import com.intuit.cloudraider.commons.ApplicationLoadBalancerDelegator;
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
import java.util.List;
import java.util.Map;


@PowerMockIgnore({"javax.management.*","javax.net.ssl.*"})
@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@PrepareForTest({ApplicationLoadBalancerRaiderImpl.class, Credentials.class, ApplicationLoadBalancerDelegator.class, AmazonElasticLoadBalancing.class })

public class ApplicationLoadBalancerImplTest {

    @Autowired
    private   ApplicationLoadBalancerRaiderImpl applicationLoadBalancerRaider;

    @Autowired
    private ApplicationLoadBalancerDelegator applicationLoadBalancerDelegator;



    private AmazonElasticLoadBalancing amazonElasticLoadBalancing;

    private LoadBalancerDescription loadBalancerDescription;
    private DescribeLoadBalancersResult describeLoadBalancersResult1;
    private DescribeLoadBalancersRequest describeLoadBalancersRequest;

    /**
     * Sets test.
     *
     * @throws Exception the exception
     */
    @Before
    public void setupTest() throws Exception {

        PowerMockito.whenNew(ApplicationLoadBalancerDelegator.class).withNoArguments().thenReturn(applicationLoadBalancerDelegator);

        amazonElasticLoadBalancing =PowerMockito.mock(AmazonElasticLoadBalancing.class);


        PowerMockito.when(applicationLoadBalancerDelegator.getAmazonApplicationLoadBalancing()).thenReturn(amazonElasticLoadBalancing);

        AvailabilityZone az= new AvailabilityZone().withZoneName("Az-a").withZoneName("Az-b");
        List<AvailabilityZone> availablityZone=new ArrayList<>();
        availablityZone.add(az);

        Listener listener=new Listener();
        listener.setProtocol("ssl");


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

        List<String> ELBs=new ArrayList<>();
        ELBs.add("helloTest");

        List<String> arns=new ArrayList<>();
        arns.add("arn:helloTest1");
        arns.add("arn:helloTest2");

        com.amazonaws.services.ec2.model.Tag tag = new com.amazonaws.services.ec2.model.Tag().withKey("Name").withValue("tag2");
        List<com.amazonaws.services.ec2.model.Tag> tags = new ArrayList<>();
        tags.add(tag);

        com.amazonaws.services.ec2.model.Instance inst=new com.amazonaws.services.ec2.model.Instance();
        inst.setInstanceId("99999");
        inst.setTags(tags);
        inst.setState(new com.amazonaws.services.ec2.model.InstanceState().withName("Running"));
        inst.setPrivateIpAddress("10.1.1.1");
        inst.setPlacement(new Placement().withAvailabilityZone("us-west-2c"));
        GroupIdentifier sg = new GroupIdentifier();
        sg.setGroupId("sg-123");
        ArrayList securityGroups = new ArrayList();
        securityGroups.add(sg);
        inst.setSecurityGroups(securityGroups);

        describeLoadBalancersRequest=new DescribeLoadBalancersRequest();
        describeLoadBalancersRequest.setNames(ELBs);
        describeLoadBalancersRequest.setPageSize(10);
        describeLoadBalancersRequest.setLoadBalancerArns(arns);


        List<LoadBalancerDescription> mylist=new ArrayList<>();
        mylist.add(loadBalancerDescription);



        describeLoadBalancersResult1=PowerMockito.mock(DescribeLoadBalancersResult.class);
        PowerMockito.when(applicationLoadBalancerDelegator.getAmazonApplicationLoadBalancing().describeLoadBalancers(describeLoadBalancersRequest)).thenReturn(describeLoadBalancersResult1);
        PowerMockito.when(applicationLoadBalancerDelegator.getAmazonApplicationLoadBalancing().describeLoadBalancers(Mockito.anyObject())).thenReturn(describeLoadBalancersResult1);

        PowerMockito.when(amazonElasticLoadBalancing.describeLoadBalancers(Mockito.anyObject())).thenReturn(describeLoadBalancersResult1);



        com.amazonaws.services.elasticloadbalancingv2.model.LoadBalancer loadBalancer =new com.amazonaws.services.elasticloadbalancingv2.model.LoadBalancer();
        loadBalancer.setLoadBalancerName("helloTest");
        loadBalancer.setAvailabilityZones(availablityZone);
        loadBalancer.setDNSName("Sameer.com");
        loadBalancer.setIpAddressType("1.1.1.1");
        loadBalancer.setLoadBalancerArn("arn:helloTest1");
        loadBalancer.setSecurityGroups(SG);




        List<com.amazonaws.services.elasticloadbalancingv2.model.LoadBalancer> loadBalancers=new ArrayList<>();
        loadBalancers.add(loadBalancer);

        List<LoadBalancerDescription> loadBalancerDescriptions= new ArrayList<>();
        loadBalancerDescriptions.add(loadBalancerDescription);

        PowerMockito.when(applicationLoadBalancerDelegator.getAmazonApplicationLoadBalancing().describeLoadBalancers(describeLoadBalancersRequest).getLoadBalancers()).thenReturn(loadBalancers);

        TargetDescription targetDescription=new TargetDescription();
        targetDescription.setAvailabilityZone("az-a");
        targetDescription.setId("99999");
        targetDescription.setPort(2323);

//        DescribeTargetGroupsRequest describeTargetGroupsRequest=new DescribeTargetGroupsRequest().withTargetGroupArns("arn:myarn");


        List<TargetDescription> targets=new ArrayList<>();
        targets.add(targetDescription);

        RegisterTargetsRequest registerTargetsRequest=new RegisterTargetsRequest();
        registerTargetsRequest.setTargetGroupArn("arn:myarn");
        registerTargetsRequest.setTargets(targets);


        RegisterTargetsResult registerTargetsResult =PowerMockito.mock(RegisterTargetsResult.class);
        PowerMockito.when(applicationLoadBalancerDelegator.getAmazonApplicationLoadBalancing().registerTargets(registerTargetsRequest)).thenReturn(registerTargetsResult);
        PowerMockito.when(applicationLoadBalancerDelegator.getAmazonApplicationLoadBalancing().registerTargets(Mockito.anyObject())).thenReturn(registerTargetsResult);


       // applicationLoadBalancerRaider = new ApplicationLoadBalancerRaiderImpl();

    }


    /**
     * Delete load balancer test.
     */
    @Test
    public void deleteLoadBalancerTest(){
        
        applicationLoadBalancerRaider.deleteLoadBalancer("helloTest");
    }

    /**
     * Add security group test.
     */
    @Test
    public void addSecurityGroupTest(){

         applicationLoadBalancerRaider.addSecurityGroup("helloTest","mysec");
//        Assert.assertTrue(applicationLoadBalancerRaider.getSecurityGroups("helloTest").contains("mysec"));

    }

    /**
     * Add security groups test.
     */
    @Test
    public void addSecurityGroupsTest(){


        applicationLoadBalancerRaider.addSecurityGroups("helloTest","group1");
//        Assert.assertTrue(applicationLoadBalancerRaider.getSecurityGroups("helloTest").contains("group1"));

    }

    /**
     * Delete security group test.
     */
    @Test
    public void deleteSecurityGroupTest(){

        applicationLoadBalancerRaider.deleteSecurityGroup("helloTest","mysec");
        Assert.assertTrue(!applicationLoadBalancerRaider.getSecurityGroups("helloTest").contains("mysec"));
    }

    /**
     * Delete security groups test.
     */
    @Test
    public void deleteSecurityGroupsTest(){

        applicationLoadBalancerRaider.deleteSecurityGroups("helloTest","group1");
        Assert.assertTrue(!applicationLoadBalancerRaider.getSecurityGroups("helloTest").contains("group1"));
    }


    /**
     * Deleteunexisting load balancer.
     */
    @Test//(expected = ResourceNotFoundException.class)
    public void deleteunexistingLoadBalancer()
    {

        applicationLoadBalancerRaider.deleteLoadBalancer("invalidElb");
    }


    /**
     * Delete load balancer listener test.
     */
    @Test
    public void deleteLoadBalancerListenerTest()
    {
        applicationLoadBalancerRaider.deleteLoadBalancerListeners("helloTest",80);

    }

    /**
     * Update loadbalancer listner test.
     */
    @Test(expected = UnSupportedFeatureException.class)
    public void updateLoadbalancerListnerTest()
    {
        applicationLoadBalancerRaider.updateLoadbalancerListner("helloTest",80,81);
    }

    /**
     * Deregister instances from load balancer test.
     */
//@Test
    public void deregisterInstancesFromLoadBalancerTest()
    {
        applicationLoadBalancerRaider.deregisterInstancesFromLoadBalancer("helloTest",1);

    }

    /**
     * Register instances from load balancer test.
     */
//@Test
    public void registerInstancesFromLoadBalancerTest()
    {
        List<String> instances=new ArrayList<>();
        instances.add("tag2");

        applicationLoadBalancerRaider.registerInstancesFromLoadBalancer("helloTest",instances);
    }

    /**
     * Deregister instance from load balancer test.
     */
//@Test
    public void deregisterInstanceFromLoadBalancerTest()
    {
        applicationLoadBalancerRaider.deregisterInstancesFromLoadBalancer("helloTest");
    }

    /**
     * Attach load balancer to subnets test.
     */
    @Test
    public void attachLoadBalancerToSubnetsTest()
    {

        applicationLoadBalancerRaider.attachLoadBalancerToSubnets("helloTest","3-abcd");
//        Assert.assertTrue(describeLoadBalancersResult1.getLoadBalancerDescriptions().get(0).getSubnets().size()==2);

    }

    /**
     * Detach load balancer from subnets test.
     */
    @Test
    public void detachLoadBalancerFromSubnetsTest()
    {
        applicationLoadBalancerRaider.detachLoadBalancerFromSubnets("helloTest","2-abcd");
//        Assert.assertTrue(describeLoadBalancersResult1.getLoadBalancerDescriptions().get(0).getSubnets().size()==2);
    }

    /**
     * Delete load balancer policy test.
     */
//@Test(expected = ReflectiveOperationException.class)
    public void deleteLoadBalancerPolicyTest()
    {
        applicationLoadBalancerRaider.deleteLoadBalancerPolicy("helloTest","myPolicy");
    }

    /**
     * Disable availability zones for load balancer test.
     */
    @Test
    public void disableAvailabilityZonesForLoadBalancerTest()
    {
        applicationLoadBalancerRaider.disableAvailabilityZonesForLoadBalancer("helloTest","us-west-c2");

    }

    /**
     * Enable availability zones for load balancer test.
     */
    @Test(expected = UnSupportedFeatureException.class)
    public void enableAvailabilityZonesForLoadBalancerTest()
    {
        applicationLoadBalancerRaider.enableAvailabilityZonesForLoadBalancer("helloTest","US-EAST-2");
    }

    /**
     * Gets load balancer instances test.
     */
//@Test
    public void getLoadBalancerInstancesTest()
    {
        List<Instance> instances=applicationLoadBalancerRaider.getLoadBalancerInstances("helloTest");
//        Assert.assertTrue(describeLoadBalancersResult1.getLoadBalancerDescriptions().get(0).getInstances().containsAll(instances));
    }

    /**
     * Change load balancer name test.
     */
    @Test(expected = UnSupportedFeatureException.class)
    public void changeLoadBalancerNameTest()
    {
        applicationLoadBalancerRaider.changeLoadBalancerName("helloTest","MyTest");
    }

    /**
     * Gets load balancer names test.
     */
    @Test
    public void getLoadBalancerNamesTest() {
        Map<String, String> map = applicationLoadBalancerRaider.getLoadBalancerNames();
        Assert.assertEquals(1, map.size());
    }

    /**
     * The type Application load balancer impl test context configuration.
     */
    @Configuration
    protected static class ApplicationLoadBalancerImplTestContextConfiguration {

        /**
         * Application load balancer delegator application load balancer delegator.
         *
         * @return the application load balancer delegator
         */
        @Bean
        public ApplicationLoadBalancerDelegator applicationLoadBalancerDelegator() {
            return Mockito.mock(ApplicationLoadBalancerDelegator.class);
        }

        /**
         * Application load balancer raider application load balancer raider.
         *
         * @return the application load balancer raider
         */
        @Bean
        public ApplicationLoadBalancerRaiderImpl applicationLoadBalancerRaiderImpl() {
            return new ApplicationLoadBalancerRaiderImpl();
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
