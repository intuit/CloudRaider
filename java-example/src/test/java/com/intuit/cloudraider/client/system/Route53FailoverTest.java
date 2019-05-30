package com.intuit.cloudraider.client.system;



import com.intuit.cloudraider.cucumber.model.ExecutionStateCache;
import com.intuit.cloudraider.cucumber.steps.*;
import com.intuit.cloudraider.cucumber.util.DNSLookup;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

public class Route53FailoverTest {

    private SoftAssert softAssert;
    private InstanceFailureStepDefinitions instanceFailureExecutor;
    private LoadBalancerStepDefinitions loadBalancerExecutor;
    private EnvironmentHealerStepDefinitions environmentHealer;
    private CloudWatchStepDefinitions cloudWatchValidator;
    private DelayStepDefinitions delayExecutor;
    private ExecutionStateCache executionStateCache;

    @BeforeClass
    public void setup() throws Exception {
        softAssert = new SoftAssert();

    }

    @Test(dataProvider = "processData", enabled = false)
    public void route53FailoverTest( String alarmName, String elbName, int minutes, String route53Destination, String failOverDestination) throws Exception, Throwable {

        try {


            executionStateCache = new ExecutionStateCache();
            environmentHealer = new EnvironmentHealerStepDefinitions();
            loadBalancerExecutor = new LoadBalancerStepDefinitions();
            instanceFailureExecutor = new InstanceFailureStepDefinitions();
            delayExecutor = new DelayStepDefinitions();

            cloudWatchValidator = new CloudWatchStepDefinitions();

            // Asserts Before Failover
            softAssert.assertFalse(DNSLookup.hostNameLookup(route53Destination).equalsIgnoreCase(failOverDestination), "DNS Lookup Before Failover Mismatch");


            // start failure
            loadBalancerExecutor.givenLoadBalancerName("ELB" ,
                            elbName);

            instanceFailureExecutor.terminateAllInstances();

             delayExecutor.waitInMinutes(minutes);


            // Asserts after Failover Completed
            softAssert.assertTrue( DNSLookup.hostNameLookup(route53Destination).equalsIgnoreCase(failOverDestination) , "DNS Lookup After Failover  Mismatch");
            softAssert.assertTrue(
                    loadBalancerExecutor.confirmHealthyHostCount(0),"EC2 Instances Termination Failed");
            softAssert.assertTrue(  cloudWatchValidator.givenAnAlarmName(alarmName).isAlarmOn(), "Alarm state mismatched");


        }
        finally {
            softAssert.assertAll();
        }


    }


    @Test(dataProvider = "processData", enabled = false)
    public void route53FailoverByTerminatingAppServerTest( String alarmName, String elbName, int minutes, String route53Destination, String failOverDestination, String processName)
            throws Exception, Throwable {


        try {
            executionStateCache = new ExecutionStateCache();
            environmentHealer = new EnvironmentHealerStepDefinitions();
            loadBalancerExecutor = new LoadBalancerStepDefinitions();
            instanceFailureExecutor = new InstanceFailureStepDefinitions();
            delayExecutor = new DelayStepDefinitions();

            // Asserts Before Failover
            softAssert.assertNotEquals(DNSLookup.hostNameLookup(route53Destination), failOverDestination, "DNS Lookup Before Failover  Mismatch");


            // start failure
            loadBalancerExecutor.givenLoadBalancerName("ELB",elbName);

            instanceFailureExecutor .terminateProcessOnAllHealthyInstances(processName);

            delayExecutor.waitInMinutes(minutes);


            // Asserts after Failover Completed
            softAssert.assertEquals( DNSLookup.hostNameLookup(route53Destination), failOverDestination , "DNS Lookup After Failover  Mismatch");
            softAssert.assertTrue(
                    loadBalancerExecutor.confirmHealthyHostCount(0),"Process Termination Failed");
            softAssert.assertTrue(  cloudWatchValidator.givenAnAlarmName(alarmName).isAlarmOn(), "Alarm state mismatched");


        }
        finally {
            environmentHealer.recover();

            delayExecutor.waitInMinutes(minutes);
            softAssert.assertFalse(  cloudWatchValidator.givenAnAlarmName(alarmName).isAlarmOn(), "Alarm state mismatched");
            softAssert.assertNotEquals(DNSLookup.hostNameLookup(route53Destination), failOverDestination , "DNS Lookup  Mismatch");


            softAssert.assertAll();
        }


    }

    @DataProvider(name = "processData")
    public static Object[][] processInfo() {

        String alarmName =  "alarmName";
        String elbName="elbName";
        String route53Destination = "mypage.com";
        String failOverDestination="somepage.com";
        String processName = "nginx";

        return new Object[][] {{alarmName, elbName, 5, route53Destination, failOverDestination, processName }};
    }
}
