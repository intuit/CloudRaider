package com.intuit.cloudraider.client.system;


import com.intuit.cloudraider.cucumber.model.ExecutionStateCache;
import com.intuit.cloudraider.cucumber.steps.*;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

public class AppServerFailureTest {

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
    public void terminateProcessTestSimple( String processName, int numHosts, String alarmName, String elbName, int minutes) throws Exception, Throwable{

        try {
            executionStateCache = new ExecutionStateCache();
            environmentHealer = new EnvironmentHealerStepDefinitions();
            loadBalancerExecutor = new LoadBalancerStepDefinitions();
            instanceFailureExecutor = new InstanceFailureStepDefinitions();
            delayExecutor = new DelayStepDefinitions();

            int unhealthyHostsCount = loadBalancerExecutor.givenLoadBalancerName("ELB",elbName)
                    .findAllOutOfServiceInstances().size();

            // start failure
            instanceFailureExecutor.terminateProcessOnHealthyInstances(processName, numHosts);

            delayExecutor.waitInMinutes(minutes);


            // Asserts
            softAssert.assertTrue(
                    loadBalancerExecutor.confirmUnHealthyHostCount(numHosts + unhealthyHostsCount),"Process Termination Failed");

           // Cloud Watch Assert

            softAssert.assertTrue(  cloudWatchValidator.givenAnAlarmName(alarmName).isAlarmOn(), "Alarm state mismatched");


        }
        finally {
            environmentHealer.recover();
            softAssert.assertAll();
        }


    }

    @DataProvider(name = "processData")
    public static Object[][] processInfo() {

        String alarmName =  "alarmName";
        String elbName="elbName";
//, {"tomcat",2, alarmName, elbName}
        return new Object[][] {{"nginx",2, alarmName, elbName, 3}};
    }
}
