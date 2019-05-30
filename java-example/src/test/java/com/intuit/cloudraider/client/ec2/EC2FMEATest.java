package com.intuit.cloudraider.client.ec2;


import com.amazonaws.services.ec2.AmazonEC2;

import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;


import com.intuit.cloudraider.commons.EC2Delegator;
import com.intuit.cloudraider.core.impl.CloudWatchRaiderImpl;
import com.intuit.cloudraider.core.impl.EC2RaiderImpl;
import com.intuit.cloudraider.core.impl.LoadBalancerRaiderImpl;
import com.intuit.cloudraider.core.interfaces.CloudWatchRaider;
import com.intuit.cloudraider.core.interfaces.EC2Raider;
import com.intuit.cloudraider.core.interfaces.LoadBalancerRaider;
import com.intuit.cloudraider.model.EC2InstanceTO;
import com.intuit.cloudraider.client.component.PropertiesHolder;
import com.intuit.cloudraider.client.dataProviderSource.DataProviderSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.*;

/**
 * Created by aalmekhlafi on 9/19/17.
 */

@Test
@ContextConfiguration(locations = { "classpath:spring/mini-xml-config-context.xml" })
public class EC2FMEATest extends AbstractTestNGSpringContextTests {

    Logger logger = LoggerFactory.getLogger(this.getClass());

    EC2Raider ec2Raider;
    private LoadBalancerRaider elbRaider;
    CloudWatchRaider cloudWatchRaider;
    AmazonEC2 ec2;
    final String tag1 = "testInstance2";
    final String tag2 = "terraform 2";
    final String tag3 = "test3-node-qal-asg";
    final String tag4 = "test4-node-qal-asg";
    final String tag7 = "test7-node-qal-asg";


    @Autowired
    PropertiesHolder propertiesHolder;


    @BeforeTest
    public void setup() {
        ec2Raider = new EC2RaiderImpl();
         ec2 = (new EC2Delegator()).getEc2();
        elbRaider = new LoadBalancerRaiderImpl();
        cloudWatchRaider=new CloudWatchRaiderImpl();

    }


    @Test(enabled = false)
    public void testRDSInstances() throws Exception
    {

       List rdsInstanceIds = cloudWatchRaider.getCloudWatchAlarmByName("");//.describeDBInstances().getDBInstances().stream().map(x -> x.getDBInstanceIdentifier()).collect(Collectors.toList());
        logger.info("--- " + rdsInstanceIds  );
    }

    @Test(enabled = false)
    public void stopEC2Intance() throws Exception
    {

        String tag = "tag";

        List<EC2InstanceTO> instanceList = ec2Raider.getInstancesIdsForOneTag(tag);
        List<String> instanceIds = retrieveInstanceIds(instanceList);

        int runningInstancesBefore = instanceIds.size();

        String instanceId = instanceIds.get(0);

        ec2Raider.stopEc2Instances(instanceId);


        Thread.sleep(210000);

        String actual = ec2Raider.getInstanceStatusById(instanceId);

        Assert.assertEquals(actual,"terminated");
        Assert.assertEquals(ec2Raider.getInstancesIdsForOneTag(tag).size(), runningInstancesBefore);
    }


    @Test(enabled = false)
    public void terminateEC2Intance() throws Exception
    {
        String tag = "tag";

        List<EC2InstanceTO> instanceList = ec2Raider.getInstancesIdsForOneTag(tag);
        List<String> instanceIds = retrieveInstanceIds(instanceList);

        int runningInstancesBefore =  instanceIds.size();

        String instanceId = instanceIds.get(0);

        ec2Raider.terminateEc2InstancesById(instanceId);


        Thread.sleep(210000);

        String actual = ec2Raider.getInstanceStatusById(instanceId);

        Assert.assertEquals(actual,"terminated");
        Assert.assertEquals(ec2Raider.getInstancesIdsForOneTag(tag).size(), runningInstancesBefore);

    }



    @Test(enabled=false)
    public void terminateAllEC2sUnderOneTagName() throws InterruptedException {
        ec2Raider.terminateEc2InstancesByTags(tag1);
        Thread.sleep(300);
        List<EC2InstanceTO> allInstances = ec2Raider.getInstancesIdsForOneTag(tag1);
        List<String> instanceIds = retrieveInstanceIds(allInstances);

        for(String instance: instanceIds) {
            Assert.assertTrue(!ec2Raider.getEC2InstanceState(tag1).get(instance).equals("running"));
        }

    }


    /**
     * Terminate All instances by Tag Using Data Provider Example
     * @throws InterruptedException
     */
    @Test(dataProvider="Termination", dataProviderClass=DataProviderSource.class,enabled=false)
    public void terminateAllEC2sUnderOneTagNameDP(String tag) throws InterruptedException {
        logger.info("Terminating all the instances with tag "+tag);
        ec2Raider.terminateEc2InstancesByTags(tag);
        Thread.sleep(300);

        List<EC2InstanceTO> allInstances = ec2Raider.getInstancesIdsForOneTag(tag);
        List<String> instanceIds = retrieveInstanceIds(allInstances);

        for(String instance: instanceIds) {
            Assert.assertTrue(!ec2Raider.getEC2InstanceState(tag).get(instance).equals("running"));
        }
        logger.info("All Instances with Tag "+tag+" is Terminated ");

    }

    /**
     * Terminate All instances by Tag Using Spring Profile  Example
     */
    @Test(enabled = false)
    public void terminateAllEC2sUnderOneTagNameSP() throws InterruptedException {
        String tag=propertiesHolder.getMyservice1_stack();
        List<String> instances= Arrays.asList(tag);
        for(String instance : instances) {
            logger.info("Terminating all the instances with tag " + instance);
            ec2Raider.terminateEc2InstancesByTags(instance);
            Thread.sleep(300);

            List<EC2InstanceTO> allInstances = ec2Raider.getInstancesIdsForOneTag(instance);
            List<String> instanceIds = retrieveInstanceIds(allInstances);

            for (String instance1 : instanceIds) {
                Assert.assertTrue(!ec2Raider.getEC2InstanceState(tag).get(instance1).equals("running"));
            }
        }
        logger.info("All Instances with Tag "+tag+" is Terminated ");
    }
    @Test(enabled=false)
    public void terminatePartialInstancesUnderOneTagName() throws InterruptedException{
        int numberOfInstancesToTerminate = 1;
        ec2Raider.terminateEc2InstancesByTags(tag2,numberOfInstancesToTerminate);
        Thread.sleep(300);
        List<EC2InstanceTO> allInstances =  ec2Raider.getInstancesIdsForOneTag(tag2);
        List<String> instanceIds = retrieveInstanceIds(allInstances);

        int counter = 0;
        for(String instance: instanceIds){
            if(ec2Raider.getEC2InstanceState(tag2).get(instance).equals("shutting-down")) counter++;
        }

        Assert.assertTrue(counter==numberOfInstancesToTerminate);
    }


    @Test(enabled=false)
    public void stopInstancesUnderOneTagName() throws InterruptedException{
        int numberOfInstancesToTerminate = 1;

        String instanceId = ec2Raider.getInstancesIdsForOneTag(tag1).get(0).toString();
        System.out.println("instance id " + instanceId);
        ec2Raider.stopEc2Instances( instanceId );
        Thread.sleep(120000);
        List<String> instances = new ArrayList();

        int counter = 0;;
        AmazonEC2 ec2 = (new EC2Delegator()).getEc2();
        for(Reservation res : ec2.describeInstances().getReservations())
        {
            for(Instance i : res.getInstances())
            {
                System.out.println(i.getState() + " " + i.getInstanceId());
                if (i.getInstanceId().equalsIgnoreCase(instanceId))
                {
                    if(i.getState().getName().equalsIgnoreCase("stopped")) {
                        counter++;
                    }
                }
            }
        }

       Assert.assertTrue(counter>0);
    }


    @Test(enabled=false)
    public void terminateAllEC2sUnderMultipleTagNames() throws InterruptedException {
        String[] tags = {tag3, tag4};
        ec2Raider.terminateEc2InstancesByTags(tags);
        Thread.sleep(300);
        List<EC2InstanceTO> allInstances = ec2Raider.getInstancesIdsForOneTag(tag3);
        List<String> instanceIds = retrieveInstanceIds(allInstances);

        for(String instance: instanceIds) {
            Assert.assertTrue(!ec2Raider.getEC2InstanceState(tag3).get(instance).equals("running"));
        }

        allInstances=ec2Raider.getInstancesIdsForOneTag(tag4);
        for(String instance: instanceIds) {
            Assert.assertTrue(!ec2Raider.getEC2InstanceState(tag4).get(instance).equals("running"));
        }

    }



    @Test(enabled=false)
    public void terminateRandomInstanceUnderOneTage()
    {
        List<EC2InstanceTO> allInstances= ec2Raider.getInstancesIdsForOneTag(tag7);
        List<String> instanceIds = retrieveInstanceIds(allInstances);

        Random rand = new Random();
        String randomElement = instanceIds.get(rand.nextInt(allInstances.size()));
        ec2Raider.terminateEc2InstancesById(randomElement);
        System.out.println("Instance Terminated is "+randomElement);

        Assert.assertTrue(!ec2Raider.getEC2InstanceState(tag7).get(randomElement).equals("running"));

    }

    @Test(enabled=false)
    public void terminateInstancesForAZ() throws InterruptedException
    {
        String tag = "tag";
        String zone = "us-west-2c";


        List<EC2InstanceTO> inServiceEC2Intances = ec2Raider.getInstancesForAZ(tag, zone);
        List<String> instanceIds = retrieveInstanceIds(inServiceEC2Intances);

        System.out.println("Currently running instances: " + inServiceEC2Intances);

        instanceIds.stream().forEach( ec2 -> ec2Raider.terminateEc2InstancesById(ec2));

        Thread.sleep(30000);

        Assert.assertEquals(ec2Raider.getInstancesForAZ(tag, zone).size(),0);

        Thread.sleep(300000);

        Assert.assertEquals(ec2Raider.getInstancesForAZ(tag, zone).size(),inServiceEC2Intances.size());

    }


    @Test(enabled=false)
    public void terminateAllInstancesForELB() throws InterruptedException
    {
        String elbName = "elbName";


        List<String> inServiceEC2Intances = elbRaider.getInServiceInstances(elbName);
        System.out.println("Currently running instances: " + inServiceEC2Intances);

        inServiceEC2Intances.stream().parallel().forEach( ec2 -> ec2Raider.terminateEc2InstancesById(ec2));

        Thread.sleep(30000);

        Assert.assertEquals(elbRaider.getInServiceInstances(elbName).size(),0);


        Thread.sleep(300000);

        Assert.assertEquals(elbRaider.getInServiceInstances(elbName).size(),inServiceEC2Intances.size());
    }


    private List<String> retrieveInstanceIds(List<EC2InstanceTO> instanceList) {
        List<String> instanceIds = new ArrayList<>();
        try {
            for (int i = 0; i < instanceList.size(); i++) {
                EC2InstanceTO currInstance = instanceList.get(i);
                if (currInstance != null) {
                    instanceIds.add(currInstance.getInstanceId());
                }
            }
        }
        catch (Exception e) {
            System.out.println(e);
        }
        return instanceIds;
    }

}
