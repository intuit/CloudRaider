package com.intuit.cloudraider.client;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.ec2.AmazonEC2;

import com.intuit.cloudraider.client.component.PropertiesHolder;
import com.intuit.cloudraider.client.dataProviderSource.DataProviderSource;
import com.intuit.cloudraider.commons.EC2Delegator;
import com.intuit.cloudraider.core.impl.CloudWatchRaiderImpl;
import com.intuit.cloudraider.core.impl.EC2RaiderImpl;
import com.intuit.cloudraider.core.impl.LoadBalancerRaiderImpl;
import com.intuit.cloudraider.core.impl.SystemRaiderImpl;
import com.intuit.cloudraider.core.interfaces.CloudWatchRaider;
import com.intuit.cloudraider.core.interfaces.EC2Raider;
import com.intuit.cloudraider.core.interfaces.LoadBalancerRaider;
import com.intuit.cloudraider.core.interfaces.SystemRaider;
import com.intuit.cloudraider.model.Actions;
import com.intuit.cloudraider.model.BasicCredentials;
import com.intuit.cloudraider.model.CloudWatchMetrics;
import com.intuit.cloudraider.model.EC2InstanceTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import org.springframework.test.context.ContextConfiguration;

/**
 * Created by aalmekhlafi on 9/28/17.
 */
@Test
@ContextConfiguration(locations = { "classpath:spring/mini-xml-config-context.xml" })
public class SystemFMEATest extends AbstractTestNGSpringContextTests {
    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    PropertiesHolder propertiesHolder;

//    @Autowired
//    GenericEnv env;

//    @Autowired
//    PropertyPlaceHolder propertyPlaceHolder;

    private SystemRaider systemRaider;
    private EC2Raider ec2Raider;
    private AmazonEC2 ec2;
    private CloudWatchRaider cloudWatchMoneky;
    private LoadBalancerRaider elbRaider;
    private BasicCredentials credentials;
    private AWSCredentials awsCredentials;
    private final String elbName = "elbName";
    private final String alarmName = "alarmName";
//    private String processName = "nginx";
    private SoftAssert softAssert;
    private String processTerminationFilePath;
    private String healIntancesFilePath;
    private String blockPortFilePath;
    private String unBlockPortFilePath;
    private String newtworkLatencyFilePath;
    private String networkCleanupFilePath;
    private String packetLossFilePath;

    private final String ALARM_STATE_MISMATCH = " Cloud watch alarm state is mismatched";
    private final String HEALTHY_HOST_MISMATCH = "Unhealthy host count mismatched";


    int executionId;
    int jobId = 76609;
    String ip = "10.0.2.129";


    @BeforeClass
    public void setup() throws Exception {


        ec2Raider = new EC2RaiderImpl();
        systemRaider = new SystemRaiderImpl();
        ec2= new EC2Delegator().getEc2();

        elbRaider = new LoadBalancerRaiderImpl();
        cloudWatchMoneky = new CloudWatchRaiderImpl();

        softAssert = new SoftAssert();


        String fileName = Actions.KILLPROCESS.getActionName() + ".sh";
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(fileName).getFile());
        processTerminationFilePath = file.getAbsolutePath();

        file = new File(classLoader.getResource("healInstance.sh").getFile());
        healIntancesFilePath = file.getAbsolutePath();

        file = new File(classLoader.getResource("blockport.sh").getFile());
        blockPortFilePath = file.getAbsolutePath();

        file = new File(classLoader.getResource("unblockport.sh").getFile());
        unBlockPortFilePath = file.getAbsolutePath();


        file = new File(classLoader.getResource("networklatency.sh").getFile());
        newtworkLatencyFilePath = file.getAbsolutePath();


        file = new File(classLoader.getResource("clear-networkfailures.sh").getFile());
        networkCleanupFilePath = file.getAbsolutePath();

        file = new File(classLoader.getResource("networkpacketloss.sh").getFile());
        packetLossFilePath = file.getAbsolutePath();

    }


    @Test(dataProvider="terminateInstances", dataProviderClass=DataProviderSource.class, enabled = false)
    public void terminateInstances(String tag,String instance) {
        System.out.println("Cloud Raider Can access this Instance (" + instance + ")");
    }

    @Test(dataProvider="elbs", dataProviderClass=DataProviderSource.class, enabled = false)
    public void changeELB(String port,String elbName,String operation,String target) {
        System.out.println("ELB Date "+ port +" "+ elbName +" "+ operation +" "+ target);
    }

    @Test (enabled = false)
    public void testNetworkLatency() throws Exception {
        //logger.info("Start spiking CPU in Cache Service node with tag: cacheservice-app-ssz-prf-cluster-node-0, for 1 minute");

        System.out.println("**************** "+ propertiesHolder.toString());

        List<String> instanceIdList = elbRaider.getInServiceInstances(elbName);


        if (instanceIdList.isEmpty()) {
            throw new SkipException("Skipping tests because  No instance is available.");
        }

        EC2InstanceTO ec2SInstance = ec2Raider.getEC2InstanceById(instanceIdList.get(0));
        ip = ec2SInstance.getPrivateIpAddress();
        System.out.println(ec2SInstance);
        
        systemRaider.executeScript(ip, newtworkLatencyFilePath, "10000","16000");

        logger.info("wait for health check to fail due to slow response time");
        Thread.sleep(90000);

        softAssert.assertFalse( elbRaider.getInServiceInstances(elbName).contains( ec2SInstance.getInstanceId()));

        logger.info("performing cleanup ");
        try {
            systemRaider.executeScript(ip, networkCleanupFilePath);

            Thread.sleep(220000);

            softAssert.assertTrue(elbRaider.getInServiceInstances(elbName).contains(ec2SInstance.getInstanceId()));
            logger.info("Finishing Network Latency");
        }
        catch (Exception ex) {
            logger.info("unable to cleanup ");
        }

        // check AppDynamics for CPU Spike alert.
    }


    @Test (enabled = false)
    public void testPacketLoss() throws Exception {
        //logger.info("Start spiking CPU in Cache Service node with tag: cacheservice-app-ssz-prf-cluster-node-0, for 1 minute");

        List<String> instanceIdList = elbRaider.getInServiceInstances(elbName);

        if (instanceIdList.isEmpty()) {
            throw new SkipException("Skipping tests because  No instance is available.");
        }

        EC2InstanceTO ec2SInstance = ec2Raider.getEC2InstanceById(instanceIdList.get(0));
        ip = ec2SInstance.getPrivateIpAddress();
        System.out.println(ec2SInstance);

        systemRaider.executeScript(ip, packetLossFilePath, "50");

        logger.info("wait for health check to fail due to slow response time");
        Thread.sleep(180000);

        softAssert.assertFalse( elbRaider.getInServiceInstances(elbName).contains( ec2SInstance.getInstanceId()));

        logger.info("performing cleanup ");
        systemRaider.executeScript(ip, networkCleanupFilePath);

        Thread.sleep(180000);

        softAssert.assertTrue( elbRaider.getInServiceInstances(elbName).contains( ec2SInstance.getInstanceId()));
        logger.info("Finishing Network Latency");

        softAssert.assertAll();

        // check AppDynamics for  CPU Spike alert.
    }


    /*
        This test will terminate an application process (NGINX, TOMCAT, JAVA) that will mark
        the instance unhealthy as health checks will fail.  Cloud watch alarm will trigger if unhealthy
        host threshold increases.  At the end heal those instances and everything should be back to normal again.
     */
    @Test (dataProvider = "processData", enabled = false)
    public void terminateProcessTest( String processName, int numHosts, String alarmName, String elbName) throws Exception {
        logger.info("Start process termination for :" + processName);

        //Check alarm state, should be off
        boolean isAlarmOn =  cloudWatchMoneky.isStateInAlarm(alarmName);
        logger.info("@Start Alarm On? : " + isAlarmOn);
        softAssert.assertFalse(isAlarmOn,ALARM_STATE_MISMATCH);

        // Retrieve in service instances for given Elastic Load-balancer
        final List<String> instanceIdList = elbRaider.getInServiceInstances(elbName);

        Double unHealthyHostStaticsBefore =  cloudWatchMoneky.getELBUnHealthyHostMetric(elbName);
        logger.info("unHealthyHostStaticsBefore " +  unHealthyHostStaticsBefore);

        if (instanceIdList.isEmpty())
        {
            throw new SkipException("Skipping tests because  No instance is available.");
        }

        // Storing instances
        List<String> effectedInstancesIpList = new ArrayList<>();
        List<String> effectedInstances = new ArrayList<>();


        /*
            Terminating process
         */
        IntStream.range(0, numHosts -1)
                .parallel()
                .forEach(
                        i ->
                        {
                            EC2InstanceTO ec2SInstance = ec2Raider.getEC2InstanceById(instanceIdList.get(i));
                            ip = ec2SInstance.getPrivateIpAddress();
                            //    System.out.println(ec2Status);
                            effectedInstancesIpList.add(ip);
                            effectedInstances.add(ec2SInstance.getInstanceId());


                            //Terminating process
                            systemRaider.executeScript(ip, processTerminationFilePath, processName);
                        } );


            logger.info("Finishing terminating process: " + processName);

            Thread.sleep(200000);

        /*
            Validating process termination by checking Alarm state and un healthy hosts metric.
        */
        isAlarmOn =  cloudWatchMoneky.isStateInAlarm(alarmName);
        logger.info("@fter Failure Alarm On: " + isAlarmOn);
        softAssert.assertTrue(isAlarmOn, ALARM_STATE_MISMATCH);

        // check un healthy host metric
        Double unHealthyHostStaticsAfter =  cloudWatchMoneky.getELBUnHealthyHostMetric(elbName);
        logger.info("unHealthyHostStaticsAfter " +  unHealthyHostStaticsAfter);
        softAssert.assertTrue(unHealthyHostStaticsAfter > unHealthyHostStaticsBefore, HEALTHY_HOST_MISMATCH);

        // Instances should not be in healthy state
        softAssert.assertFalse(elbRaider.getInServiceInstances(elbName).containsAll(effectedInstances));

        /*
            Healing Instances
         */
        effectedInstancesIpList.stream()
                .parallel()
                .forEach( ip  ->  systemRaider.executeScript(ip, healIntancesFilePath,processName));

        logger.info("wait for elb to be updated");
        Thread.sleep(240000);

        logger.info("at the end: inService instances " + instanceIdList);
        softAssert.assertTrue(elbRaider.getInServiceInstances(elbName)
                .containsAll(effectedInstances));

        // Alarm should be off now
        isAlarmOn = cloudWatchMoneky.isStateInAlarm(alarmName);
        logger.info("@End Alarm On: " + isAlarmOn);
        softAssert.assertFalse(isAlarmOn, ALARM_STATE_MISMATCH);

        softAssert.assertAll();
    }


    /*

     This test will block DNS port on ec2 intances.  Instacnes will become unhealthy as health checks will fail.  Cloud watch alarm will trigger if unhealthy
     host threshold increases.  At the end heal those instances and everything should be back to normal again.

  */
    @Test (dataProvider = "testData" , enabled = false)
    public void blockDNS(String elbName, int numHosts, String alarmName ) throws Exception {

        //Check alarm state, should be off
        boolean isAlarmOn =  cloudWatchMoneky.isStateInAlarm(alarmName);

        logger.info("@Start Alarm On? : " + isAlarmOn);

        softAssert.assertFalse(isAlarmOn);

        // Retrieve in service instances for ELB
        List<String> instanceIdList = elbRaider.getInServiceInstances(elbName);
        int countBefore = instanceIdList.size();

        Double unHealthyHostStaticsBefore =  cloudWatchMoneky.getELBUnHealthyHostMetric(elbName);
        logger.info("unHealthyHostStaticsBefore " +  unHealthyHostStaticsBefore);

        if (instanceIdList.isEmpty()) {
            throw new SkipException("Skipping tests because  No instance is available.");
        }

        List<String> effectedIntancesIpList = new ArrayList<>();
        List<String> effectedInstances = new ArrayList<>();

        for (int i=0; i<numHosts; i++) {
            EC2InstanceTO ec2SInstance = ec2Raider.getEC2InstanceById(instanceIdList.get(i));
            ip = ec2SInstance.getPrivateIpAddress();
            //    System.out.println(ec2Status);
            effectedIntancesIpList.add(ip);
            effectedInstances.add(ec2SInstance.getInstanceId());

            systemRaider.executeScript(ip, blockPortFilePath, "53");
        }

        Thread.sleep(240000);

        // check alarm after process termination
        isAlarmOn =  cloudWatchMoneky.isStateInAlarm(alarmName);
        logger.info("@fter Failure Alarm On: " + isAlarmOn);
        softAssert.assertTrue(isAlarmOn);

        // check un healthy host metric
        Double unHealthyHostStaticsAfter =  cloudWatchMoneky.getELBUnHealthyHostMetric(elbName);
        logger.info("unHealthyHostStaticsAfter " +  unHealthyHostStaticsAfter);
        softAssert.assertTrue(unHealthyHostStaticsAfter > unHealthyHostStaticsBefore);


        // Instances should not be in healthy state
        instanceIdList = elbRaider.getInServiceInstances(elbName);
        softAssert.assertFalse(instanceIdList.containsAll(effectedInstances));


        //heal instances
        effectedIntancesIpList.stream().forEach( ip  ->  systemRaider.executeScript(ip, unBlockPortFilePath, "53"));


        logger.info("wait for elb to be updated");
        Thread.sleep(300000);

        instanceIdList = elbRaider.getInServiceInstances(elbName);
        logger.info("at the end: inService instances " + instanceIdList);
        softAssert.assertTrue(instanceIdList.containsAll(effectedInstances));


        // Alarm should be off now
        isAlarmOn = cloudWatchMoneky.isStateInAlarm(alarmName);
        logger.info("@End Alarm On: " + isAlarmOn);
        softAssert.assertFalse(isAlarmOn);
        softAssert.assertAll();
    }

    @Test (enabled = false)
    public void rootVolumeStressTest() throws InterruptedException
    {
        List<String> instanceIdList = elbRaider.getInServiceInstances(elbName);
        EC2InstanceTO ec2SInstance = ec2Raider.getEC2InstanceById(instanceIdList.get(0));
        ip = ec2SInstance.getPrivateIpAddress();
        System.out.println(ec2SInstance);


        String fileName = Actions.STRESSDISK.getActionName()+".sh";
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(fileName).getFile());
        systemRaider.executeScript(ip,file.getAbsolutePath());

        Thread.sleep(300000);

        fileName = Actions.KILLPROCESS.getActionName() + ".sh";
        file = new File(classLoader.getResource(fileName).getFile());
        systemRaider.executeScript(ip, file.getAbsolutePath(), "dd");

        Thread.sleep(10000);
    }

    /*
       Fill up root disk, which will mark host unavailable.
     */
    @Test (enabled = false)
    public void rootVolumeFull() throws InterruptedException
    {
        List<String> instanceIdList = elbRaider.getInServiceInstances(elbName);
        EC2InstanceTO ec2SInstance = ec2Raider.getEC2InstanceById(instanceIdList.get(0));
        ip = ec2SInstance.getPrivateIpAddress();
        System.out.println(ec2SInstance);


        String fileName = Actions.DISKFULL.getActionName()+".sh";
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(fileName).getFile());
        systemRaider.executeScript(ip,file.getAbsolutePath());

        Thread.sleep(90000);

        instanceIdList = elbRaider.getInServiceInstances(elbName);

        // host becomes unhealthy at this point...
        softAssert.assertFalse( instanceIdList.contains( ec2SInstance.getInstanceId()));

        // Terminate this instance
        ec2Raider.terminateEc2InstancesById(ec2SInstance.getInstanceId());

        Thread.sleep(120000);

        softAssert.assertFalse(elbRaider.getOutOfServiceInstances(elbName).contains(ec2SInstance.getInstanceId()) );

        softAssert.assertAll();;

        //TODO check alerts
    }

    //    @AfterClass
    public void tearDown() throws Exception {

        logger.info("Stopping load on Cache Service");
//        obj.stopTest(executionId);
        logger.info("load has been stopped");

        logger.info("FMEA Test is completed");
    }

    @DataProvider(name = "processData")
    public static Object[][] processInfo() {

        String alarmName =  "alarmName";
        String elbName="elbName";
//, {"tomcat",2, alarmName, elbName}
        return new Object[][] {{"nginx",2, alarmName, elbName}};
    }

    @DataProvider(name = "testData")
    public static Object[][] testData() {

        String alarmName =  "alarmName";
        String elbName="elbName";
        return new Object[][] {{elbName,2, alarmName}};
    }

}
