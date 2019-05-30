package com.intuit.cloudraider.client.dataProviderSource;

import org.testng.annotations.DataProvider;

import java.lang.reflect.Method;

/**
 * Created by sabdulaziz on 4/17/18.
 */
public class DataProviderSource {


    @DataProvider(name="terminateInstances")
    public static Object[][] getinstatnces()
    {
        return new Object[][] {
//                number of instances and the instance tag name
                {"1","serviceOneInstance"},{"3","serviceTwoInstances"}};
    }

    @DataProvider(name="elbs")
    public static Object[][] getElbData()
    {
        return new Object[][] {
                {"1001","ElbName1","changePort","3445"},{"1003","ElbName2","unRegistrInstance","instanceName"}};
    }

    @DataProvider(name="Termination")
    public static Object[][] getScenarioesData(Method method) {
        String testCase = method.getName();
        if ("terminateAllEC2sUnderOneTagNameDP".equals(testCase)) {
            return new Object[][]{{"InstanceName1"}};
        } else if ("terminatPartitalsInstance".equals(testCase)) {
            return new Object[][]{{2,"Scenario2 data"}};
        } else {
            return new Object[][]{{"common Test cases"}};
        }
    }

    /***
     * Example for Volumes DataProvider
     */

    @DataProvider(name="Service1Volumes")
    public static Object [][]  getVolumeIds()
    {
        return new Object[][] {
//                Volumes Ids for attachement or dettachemnt
                {"FirstID"},{"SecondID"}};
    }
}
