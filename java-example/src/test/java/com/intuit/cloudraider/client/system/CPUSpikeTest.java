package com.intuit.cloudraider.client.system;



import com.intuit.cloudraider.cucumber.model.ExecutionStateCache;
import com.intuit.cloudraider.cucumber.steps.*;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

public class CPUSpikeTest {

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

    @Test(dataProvider = "testData", enabled = false)
    public void cpuSpikeTest( int numHosts, String alarmName, String elbName, int cores, int wait) throws Exception, Throwable{

        try {

            executionStateCache = new ExecutionStateCache();
            environmentHealer = new EnvironmentHealerStepDefinitions();
            loadBalancerExecutor = new LoadBalancerStepDefinitions();
            instanceFailureExecutor = new InstanceFailureStepDefinitions();
            delayExecutor = new DelayStepDefinitions();

            cloudWatchValidator = new CloudWatchStepDefinitions();

            int unhealthyHostsCount = loadBalancerExecutor.givenLoadBalancerName("ELB",elbName)
                    .findAllOutOfServiceInstances().size();

            // start failure
            instanceFailureExecutor
                    .spikeCPUOnHealthyInstances(numHosts, cores);


            delayExecutor.waitInMinutes(wait);



           // Cloud Watch Assert

            softAssert.assertTrue(  cloudWatchValidator.givenAnAlarmName(alarmName).isAlarmOn(), "Alarm state mismatched");


        }
        finally {
           // environmentHealer.givenELBHealInstancesByRestartingProcess(elbName, processName);
            softAssert.assertAll();
        }


    }

    @DataProvider(name = "testData")
    public static Object[][] testData() {

        String alarmName =   "alarmName";
        String elbName="elbName";
//, {"tomcat",2, alarmName, elbName}
        return new Object[][] {{3, alarmName, elbName, 4, 4}};
    }
}
