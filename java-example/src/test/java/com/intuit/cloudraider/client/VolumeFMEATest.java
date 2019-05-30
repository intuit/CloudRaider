package com.intuit.cloudraider.client;

import com.amazonaws.services.ec2.model.Volume;

import com.intuit.cloudraider.client.component.PropertiesHolder;
import com.intuit.cloudraider.client.dataProviderSource.DataProviderSource;
import com.intuit.cloudraider.core.impl.EBSRaiderImpl;
import com.intuit.cloudraider.core.impl.LoadBalancerRaiderImpl;
import com.intuit.cloudraider.core.interfaces.EBSRaider;
import com.intuit.cloudraider.core.interfaces.LoadBalancerRaider;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;


/**
 * Created by aalmekhlafi on 9/22/17.
 */

@Test
@ContextConfiguration(locations = { "classpath:spring/mini-xml-config-context.xml" })
public class VolumeFMEATest extends AbstractTestNGSpringContextTests {


    Logger logger = LoggerFactory.getLogger(this.getClass());
    EBSRaider ebsRaider;
    LoadBalancerRaider elbRaider;
    private final String elbName = "elbName";


    @Autowired
    PropertiesHolder propertiesHolder;

    @BeforeTest
    public void setup(){
        ebsRaider = new EBSRaiderImpl();
        elbRaider = new LoadBalancerRaiderImpl();

    }

    @Test (enabled = false)
    public void DetachVolume() throws InterruptedException {

        String volumeId="vol-0ec97ffeb292058ad";


        logger.info("Checking if Volume in Use "+ebsRaider.getVolumesState(volumeId).get(volumeId));
        Assert.assertTrue(ebsRaider.getVolumesState(volumeId).get(volumeId).equals("in-use"));
        ebsRaider.detachEbsVolume(volumeId);
        Thread.sleep(12000);
        logger.info("Checking volume is available  "+ebsRaider.getVolumesState(volumeId));
        Assert.assertTrue(ebsRaider.getVolumesState(volumeId).get(volumeId).equals("available"));

    }

    /**
     * This an Example on how you can use dataprovider to detach volumes
     * @throws InterruptedException
     */
    @Test (dataProvider="Service1Volumes", dataProviderClass=DataProviderSource.class, enabled = false)
    public void DetachVolumeDataProvider(String volumeId) throws InterruptedException {


        logger.info("Checking if Volume in Use "+ebsRaider.getVolumesState(volumeId).get(volumeId));
        Assert.assertTrue(ebsRaider.getVolumesState(volumeId).get(volumeId).equals("in-use"));
        ebsRaider.detachEbsVolume(volumeId);
        Thread.sleep(12000);
        logger.info("Checking volume is available  "+ebsRaider.getVolumesState(volumeId));
        Assert.assertTrue(ebsRaider.getVolumesState(volumeId).get(volumeId).equals("available"));

    }

    /**
     * this is an example on how you can use Spring profile to get all the data from config file
     * @throws InterruptedException
     */
    @Test (enabled = false)
    public void DetachVolumeDataInput() throws InterruptedException {


        String volumeIdAsString=propertiesHolder.getMyservice1_volumes();

        List<String> volumeIds= Arrays.asList(volumeIdAsString);


        for(String volumeId: volumeIds) {
            logger.info("Checking if Volume in Use " + ebsRaider.getVolumesState(volumeId).get(volumeId));
            Assert.assertTrue(ebsRaider.getVolumesState(volumeId).get(volumeId).equals("in-use"));
            ebsRaider.detachEbsVolume(volumeId);
            Thread.sleep(12000);
            logger.info("Checking volume is available  " + ebsRaider.getVolumesState(volumeId));
            Assert.assertTrue(ebsRaider.getVolumesState(volumeId).get(volumeId).equals("available"));
        }

    }

    @Test (enabled = false)
    public void DetachAllVolumesForInstance() throws InterruptedException {

        String instanceId = elbRaider.getInServiceInstances(elbName).get(0).toString();

        List<Volume> ebsVolumes = ebsRaider.getVolumesForGivenInstanceId(instanceId);

        ebsVolumes.forEach(System.out::println);

        ebsVolumes.forEach(
                volume ->
                {
                    ebsRaider.detachEbsVolume(volume.getVolumeId());
                }
        );

        Thread.sleep(60000);

        logger.info("InService instances : " + elbRaider.getInServiceInstances(elbName));


        ebsVolumes.forEach(
                volume ->
                {
                    ebsRaider.attachEbsVolume(instanceId, volume.getAttachments().get(0).getDevice(),volume.getVolumeId());
                }
        );




    }



    @Test (enabled = false)
    public void volumeAttachment() throws InterruptedException {

        String volumeId="vol-0ec97ffeb292058ad";

        String instanceId="i-0cd05d071c81b5a53";
        String deviceName="/dev/sdf";

        logger.info("Checking if Volume available "+ebsRaider.getVolumesState(volumeId).get(volumeId));
        Assert.assertTrue(ebsRaider.getVolumesState(volumeId).get(volumeId).equals("available"));

        ebsRaider.attachEbsVolume(instanceId,deviceName,volumeId);
        Thread.sleep(12000);
        logger.info("Checking if Volume in-use "+ebsRaider.getVolumesState(volumeId).get(volumeId));
        Assert.assertTrue(ebsRaider.getVolumesState(volumeId).get(volumeId).equals("in-use"));

    }
}
