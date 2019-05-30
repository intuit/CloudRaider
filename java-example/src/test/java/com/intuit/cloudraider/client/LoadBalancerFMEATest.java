package com.intuit.cloudraider.client;

import  com.amazonaws.services.elasticloadbalancingv2.AmazonElasticLoadBalancing;
import com.amazonaws.services.elasticloadbalancingv2.model.DeregisterTargetsRequest;

import com.intuit.cloudraider.core.impl.EC2RaiderImpl;
import com.intuit.cloudraider.core.impl.LoadBalancerRaiderImpl;
import com.intuit.cloudraider.core.interfaces.EC2Raider;
import com.intuit.cloudraider.core.interfaces.LoadBalancerRaider;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import java.util.List;

/**
 * Created by aalmekhlafi on 9/22/17.
 */
public class LoadBalancerFMEATest {

    LoadBalancerRaider loadBalancerRaider;

    EC2Raider ec2Raider;
    AmazonElasticLoadBalancing elasticLoadBalancing;

    String loadBalancerName0 = "demoloadbalancer0";
    String loadBalancerName1 = "demoloadbalancer1";
    String loadBalancerName2 = "demoloadbalancer2";
    private final String elbName = "elbName";
    int listener = 443;
    SoftAssert softAssert;

    @BeforeClass
    public void setup() {
        loadBalancerRaider = new LoadBalancerRaiderImpl();

        ec2Raider = new EC2RaiderImpl();
        softAssert = new SoftAssert();
    }


    @Test (enabled=false)
    public void deleteLoadBalancerTest() {
        loadBalancerRaider.deleteLoadBalancer(loadBalancerName0);
    }


    @Test (enabled=false)
    public void deregisterInstancesFromELB() {
       // System.out.println(loadBalancerRaider.getLoadBalancerInstances("hello-fe-learning"));
        com.amazonaws.services.elasticloadbalancingv2.model.DescribeLoadBalancersResult result =   elasticLoadBalancing.describeLoadBalancers(new com.amazonaws.services.elasticloadbalancingv2.model.DescribeLoadBalancersRequest().withNames("hello-fe-learning"));
        for (com.amazonaws.services.elasticloadbalancingv2.model.LoadBalancer alb : result.getLoadBalancers())
        {
            System.out.println("--- " + alb);
            elasticLoadBalancing.deregisterTargets(new DeregisterTargetsRequest().withTargetGroupArn("vpc-fb12479d"));
        }
       // loadBalancerRaider.deregisterInstancesFromLoadBalancer("hello-fe-learning", 3);
    }

    @Test (enabled=false)
    public void deleteLoadBalancerListenersTest() {
        loadBalancerRaider.deleteLoadBalancerListeners(loadBalancerName2, listener);
    }

    @Test (enabled=false)
    public void deleteSecurityGroupsTest() {
        String securityGroup = "sg-b55e57cf";
        loadBalancerRaider.deleteSecurityGroup(elbName, securityGroup);

        //now add it back
        loadBalancerRaider.addSecurityGroup(elbName, securityGroup);

        List<String> securityGroups = loadBalancerRaider.getSecurityGroups(elbName);
        Assert.assertTrue(securityGroups.contains(securityGroup));
    }

    @Test (enabled=false)
    public void simulateAZFailureTest() throws InterruptedException {
        String availabilityZone = "us-west-2c";

        int inServiceHostCount = loadBalancerRaider.getInServiceInstances(elbName).size();

        loadBalancerRaider.disableAvailabilityZonesForLoadBalancer(elbName, availabilityZone);

        Thread.sleep(60000);

        int inServiceHostAfterAZFailure = loadBalancerRaider.getInServiceInstances(elbName).size();

        softAssert.assertTrue(  inServiceHostCount >  inServiceHostAfterAZFailure);


        loadBalancerRaider.enableAvailabilityZonesForLoadBalancer(elbName, availabilityZone);

        Thread.sleep(1800000);

        int inServiceHostAsEnd = loadBalancerRaider.getInServiceInstances(elbName).size();

        softAssert.assertTrue(  inServiceHostCount ==  inServiceHostAsEnd);
        softAssert.assertAll();
    }

    /*
       Simulating a failure when one of the Availability Zone goes down
       For this test we are removing one of the subnet in an AZ
     */
    @Test (enabled=false)
    public void simulateAZFailureBySubnet() throws InterruptedException {
        String subnet = "subnet-eda038b6";

        int inServiceHostCountAtStart = loadBalancerRaider.getInServiceInstances(elbName).size();

       loadBalancerRaider.detachLoadBalancerFromSubnets(elbName, subnet);

        Thread.sleep(30000);

        int inServiceHostAfterAZFailure = loadBalancerRaider.getInServiceInstances(elbName).size();

        softAssert.assertTrue(  inServiceHostCountAtStart >  inServiceHostAfterAZFailure);


        loadBalancerRaider.attachLoadBalancerToSubnets(elbName, subnet);

        Thread.sleep(60000);

        int inServiceHostAtEnd = loadBalancerRaider.getInServiceInstances(elbName).size();

        softAssert.assertTrue(  inServiceHostCountAtStart ==  inServiceHostAtEnd);
        softAssert.assertAll();

    }

    /*
      Terminating all the hosts under an Elastic Load Balancer.
      All the in service EC2 instances will be marked as out of service.
      Auto Scaling Group will terminate and spin off new instances.
      At the end new instances will be up.
     */
    @Test (enabled=false)
    public void terminateAllInstancesForGivenLoadBalancer() throws InterruptedException {
      List<String> instances = loadBalancerRaider.getOutOfServiceInstances(elbName);
      instances.addAll(loadBalancerRaider.getInServiceInstances(elbName));

      instances.stream().forEach(

              id ->
              {
                  ec2Raider.terminateEc2InstancesById(id);
              }
      );

        Thread.sleep(700000);

        instances.stream().forEach(

                id ->
                {
                    String status = ec2Raider.getInstanceStatusById(id);
                    Assert.assertNotEquals(status, "running");

                }
        );

        List<String> restackedInstances = loadBalancerRaider.getOutOfServiceInstances(elbName);
        restackedInstances.addAll(loadBalancerRaider.getInServiceInstances(elbName));

        // old instances shouldn't be in ELB
        Assert.assertFalse(restackedInstances.containsAll(instances));

        Assert.assertEquals(restackedInstances.size(),instances.size());
    }

}
