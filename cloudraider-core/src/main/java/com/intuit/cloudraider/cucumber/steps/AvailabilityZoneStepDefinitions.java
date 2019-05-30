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

package com.intuit.cloudraider.cucumber.steps;

import com.amazonaws.services.ec2.model.Tag;
import com.intuit.cloudraider.core.interfaces.EC2Raider;
import com.intuit.cloudraider.cucumber.model.ExecutionStateCache;
import com.intuit.cloudraider.cucumber.util.CucumberHelperFunctions;
import com.intuit.cloudraider.model.EC2InstanceTO;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Cucumber Step Definitions for filtering EC2 instances based in a certain availability zone.
 */
public class AvailabilityZoneStepDefinitions {

    @Autowired
    @Qualifier("ec2raiderBean")
    private EC2Raider ec2Raider;

    /**
     * The Logger.
     */
    Logger logger = LoggerFactory.getLogger(this.getClass());

    public ExecutionStateCache getExecutionStateCache() {
        return executionStateCache;
    }

    public void setExecutionStateCache(ExecutionStateCache executionStateCache) {
        this.executionStateCache = executionStateCache;
    }

    @Autowired
    private ExecutionStateCache executionStateCache;

    public AvailabilityZoneStepDefinitions() {

    }

    /**
     * Finds all instances in given availability zones that do not match any of the tags provided.
     *
     * @param azs availability zones (i.e. us-west-2a)
     * @param instanceIgnoreTags tags to ignore; tags are in the form of tagName:tagValue,tagName:tagValue
     * @return AvailabilityZoneStepDefinitions
     */
    @Given("^AZs \"([^\"]*)\" ignore instances with tags \"([^\"]*)\"$")
    public AvailabilityZoneStepDefinitions givenAzWithIgnoreTags(String azs, String instanceIgnoreTags) {
        String[] availabilityZones = azs.split(",\\s*");

        List<Tag> tags = CucumberHelperFunctions.tagStringToList(instanceIgnoreTags);

        List<String> instanceIdsToIgnore =
                new ArrayList<>(ec2Raider.getInstancesFromAnyTags(tags).stream().map(EC2InstanceTO::getInstanceId).collect(Collectors.toList()));

        for (String availabilityZone : availabilityZones) {
            executionStateCache.addInstances(ec2Raider.getEc2InstancesForAvailabilityZone(availabilityZone, instanceIdsToIgnore));
        }
        return this;
    }
    
    /**
     * Finds all instances in given availability zones that do not match any of the tags to ignore and matches ALL of the
     * compulsory tags.
     *
     * @param azs availability zones (i.e. us-west-2a)
     * @param compulsoryTagsString tags that must be present; tags are in the form of tagName:tagValue,tagName:tagValue
     * @param ignoreTagsString tags to ignore; tags are in the form of tagName:tagValue,tagName:tagValue
     */
    @Given("^AZs \"([^\"]*)\" instance only with tags \"([^\"]*)\" ignore instances with tags \"([^\"]*)\"$")
    public void givenAzWithCompulsoryTags(String azs, String compulsoryTagsString, String ignoreTagsString) {
        String[] availabilityZones = azs.split(",\\s*");

        List<Tag> compulsoryTags = CucumberHelperFunctions.tagStringToList(compulsoryTagsString);
        List<Tag> ignoreTags = CucumberHelperFunctions.tagStringToList(ignoreTagsString);

        List<EC2InstanceTO> instances = new ArrayList<>();

        for (String availabilityZone : availabilityZones) {
            instances.addAll(ec2Raider.getEc2InstanceIdsWithCompulsoryTagsForAvailabilityZone(availabilityZone, ignoreTags, compulsoryTags));
        }

        if (instances.isEmpty()) {
            throw new RuntimeException("No instances available");
        }
        executionStateCache.setInstances(instances);
    }


    @When("^terminate (\\d+) instance in AZ$")
    public AvailabilityZoneStepDefinitions terminateInstanceOnNumInstances(int numInstances )
    {
        List<EC2InstanceTO> instances = executionStateCache.getInstances();

        if (instances == null  || instances.isEmpty())
        {
            throw new RuntimeException("Unable to terminate process, no instances available in AZ");
        }

        if (numInstances > instances.size())
        {
            numInstances = instances.size();
        }

        //Added to include randomness
        Collections.shuffle(instances);

        IntStream.range(0, numInstances )
                .parallel()
                .forEach(
                        i ->
                        {
                            logger.info("AZ Terminated Instance : ");
                            logger.info(instances.get(i).getInstanceId());
                            ec2Raider.terminateEc2InstancesById(instances.get(i).getInstanceId());
                        } );

        return this;
    }

    @When("^terminate (\\d+) instance in AZ only with tags \\\"([^\\\"]*)\\\"$")
    public AvailabilityZoneStepDefinitions terminateInstanceOnNumInstancesWithTags(int numInstances, String compulsoryTagsString)
    {
        List<Tag> compulsoryTags = new ArrayList<>();
        List<Tag> currentTags = new ArrayList<>();

        String[] compulsoryTagKeyValuePairs = compulsoryTagsString.split("; ");

        for (String compulsoryTagKeyValuePair : compulsoryTagKeyValuePairs) {
            String[] compulsoryTagKeyValue = compulsoryTagKeyValuePair.split("=");
            Tag compulsoryTag = new Tag();

            compulsoryTag.setKey(compulsoryTagKeyValue[0]);
            compulsoryTag.setValue(compulsoryTagKeyValue[1]);
            compulsoryTags.add(compulsoryTag);
        }

        List<EC2InstanceTO> instances = executionStateCache.getInstances();

        if (instances == null  || instances.isEmpty())
        {
            throw new RuntimeException("Unable to terminate process, no instances available in AZ");
        }

        if (numInstances > instances.size())
        {
            numInstances = instances.size();
        }

        //Added to include randomness
        Collections.shuffle(instances);

        int terminatedInstances = 0;
        for ( EC2InstanceTO instance : instances) {
            boolean flag = true;

            for(Tag compulsoryTag : compulsoryTags) {
                boolean instanceContainsTag = false;
                for(Tag instanceTag : instance.getTags()) {
                    if(instanceTag.equals(compulsoryTag)) {
                        instanceContainsTag = true;
                        break;
                    }
                }
                if(!instanceContainsTag) {
                    flag = false;
                    break;
                }
            }

            if(flag) {
                logger.info("Terminated Instance: " +instance.getInstanceId());
                ec2Raider.terminateEc2InstancesById(instance.getInstanceId());

                terminatedInstances += 1;
            }

            if(terminatedInstances == numInstances) {
                break;
            }

        }
        return this;
    }

    List<Tag> tagStringToList(String tagString) {
        List<Tag> tags = new ArrayList<>();

        if(tagString == null || tagString.equals("")) {
            return tags;
        }

        String[] tagKeyValuePairs = tagString.split("; ");

        for (String tagKeyValuePair : tagKeyValuePairs) {
            String[] tagKeyValue = tagKeyValuePair.split("=");
            Tag tag = new Tag();
            tag.setKey(tagKeyValue[0]);
            tag.setValue(tagKeyValue[1]);
            tags.add(tag);
        }

        return tags;
    }

    @Then("^assertEC2 healthy host count in AZ$")
    public void confirmHealthyHostCount() throws Throwable
    {
        String[] availaibilityzones = executionStateCache.getAvailaibilityzones();
        List<Tag> compulsoryTags = executionStateCache.getCompulsoryTags();
        List<Tag> ignoreTags = executionStateCache.getIgnoreTags();

        List<EC2InstanceTO> instances = new ArrayList<>();

        for(String availaibilityzone : availaibilityzones) {
            instances.addAll(ec2Raider.getEc2InstanceIdsWithCompulsoryTagsForAvailabilityZone(availaibilityzone,ignoreTags,compulsoryTags));
        }
        logger.info("AZ healthy host count : " + instances.size());
        Assert.assertTrue ("Healthy host count mismatched " ,this.confirmHealthyHostCount(instances.size()));
    }

    public boolean confirmHealthyHostCount(int expected) throws Throwable
    {
        return (executionStateCache.getHealthyHostCount() == expected);
    }
}