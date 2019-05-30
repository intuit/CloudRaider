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

import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.model.*;
import com.intuit.cloudraider.commons.CFNDelegator;
import com.intuit.cloudraider.core.interfaces.CFNRaider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;

/**
 * AWS Cloud Formation functionality.
 */
@Component(value="cfnRaiderBean")
public class CFNRaiderImpl implements CFNRaider {

    private AmazonCloudFormation amazonCloudFormation;

    @Autowired
    private CFNDelegator cfnDelegator;

    /**
     * The Logger.
     */
    Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * Instantiates a new Cfn raider.
     */
    public CFNRaiderImpl() {
        
    }

    /**
     * Stack creation with template body.
     *
     * @param stackName stack name
     * @param templateBody body
     * @param parameters parameters
     * @return newly created stack's id
     */
    public String CreateWithBody(String stackName, String templateBody,  Collection<Parameter> parameters) {
        CreateStackRequest createStackRequest = new CreateStackRequest().withTemplateBody(templateBody).withParameters(parameters).withStackName(stackName);
        CreateStackResult createStackResult = cfnDelegator.getAmazonCloudFormationClient().createStack(createStackRequest);
        return createStackResult.getStackId();
    }

    /**
     * Stack creation with template url.
     *
     * @param stackName stack name
     * @param templateUrl url
     * @param parameters parameters
     * @return newly created stack's id
     */
    public String CreateWithUrl(String stackName, String templateUrl, Collection<Parameter> parameters) {
        CreateStackRequest createStackRequest = new CreateStackRequest().withTemplateURL(templateUrl).withParameters(parameters).withStackName(stackName);
        CreateStackResult createStackResult = cfnDelegator.getAmazonCloudFormationClient().createStack(createStackRequest);
        return createStackResult.getStackId();
    }

    /**
     * Delete the stack with the given name.
     *
     * @param stackName stack name
     * @return http status code
     */
    public int delete(String stackName) {

        DeleteStackRequest deleteStackRequest = new DeleteStackRequest().withStackName(stackName);
        DeleteStackResult deleteStackResult = cfnDelegator.getAmazonCloudFormationClient().deleteStack(deleteStackRequest);
        return deleteStackResult.getSdkHttpMetadata().getHttpStatusCode();
    }

    /**
     * Get the status of the stack with the given name.
     *
     * @param stackName stack name
     * @return stack status
     */
    public String getStackStatus(String stackName) {

        DescribeStacksRequest describeStacksRequest = new DescribeStacksRequest().withStackName(stackName);
        DescribeStacksResult describeStacksResult = cfnDelegator.getAmazonCloudFormationClient().describeStacks(describeStacksRequest);
        return describeStacksResult.getStacks().get(0).getStackStatus();
    }
}
