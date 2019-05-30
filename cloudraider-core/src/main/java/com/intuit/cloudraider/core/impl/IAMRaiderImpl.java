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

import com.amazonaws.services.identitymanagement.model.*;
import com.intuit.cloudraider.commons.IAMDelegator;
import com.intuit.cloudraider.core.interfaces.IAMRaider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * The type Iam raider.
 */
@Component(value="iamRaiderBean")
public class IAMRaiderImpl implements IAMRaider {

    /**
     * The Logger.
     */
    Logger logger = LoggerFactory.getLogger(this.getClass());
    
    @Autowired
    private IAMDelegator iamDelegator;

    /**
     * Instantiates a new Iam raider.
     */
    public IAMRaiderImpl() {

    }

    @Override
    public String getRolePolicyDocument(String roleName, String policyName) {

        GetRolePolicyRequest iamRolePolicyRequest = new GetRolePolicyRequest().withRoleName(roleName).withPolicyName(policyName);
        return iamDelegator.getIAM().getRolePolicy(iamRolePolicyRequest).getPolicyDocument();
    }

    @Override
    public List<AttachedPolicy> getRolePolicyList(String roleName) {
        ListAttachedRolePoliciesRequest request =
                new ListAttachedRolePoliciesRequest()
                        .withRoleName(roleName);

        List<AttachedPolicy> policies = iamDelegator.getIAM().listAttachedRolePolicies(request).getAttachedPolicies();


        return policies;

    }

    @Override
    public String getRolePolicyArn(String roleName, String policyName) {


        String policyArn = getRolePolicyList(roleName)
                .stream()
                .collect(Collectors.toList())
                .get(1 )
                .getPolicyArn();



        return policyArn;

    }

    @Override
    public String createAndAttachPolicy(String roleName, String policyName, String policyDocument) {

        CreatePolicyRequest createPolicyRequest = new CreatePolicyRequest()
                .withPolicyName(policyName)
                .withPolicyDocument(policyDocument)
                .withDescription("CloudRaider generated policy");

        String policyArn = iamDelegator.getIAM().createPolicy(createPolicyRequest).getPolicy().getArn();

        AttachRolePolicyRequest attachRolePolicyRequest = new AttachRolePolicyRequest()
                .withPolicyArn(policyArn)
                .withRoleName(roleName);

        iamDelegator.getIAM().attachRolePolicy(attachRolePolicyRequest);
        return policyArn;
    }

    @Override
    public boolean detachAndDeletePolicy(String policyArn, String roleName) {

       DetachRolePolicyRequest detachPolicyRequest = new DetachRolePolicyRequest().withPolicyArn(policyArn)
               .withRoleName(roleName);
        iamDelegator.getIAM().detachRolePolicy(detachPolicyRequest);
        DeletePolicyRequest deletePolicyRequest = new DeletePolicyRequest().withPolicyArn(policyArn);
        try {
            iamDelegator.getIAM().deletePolicy(deletePolicyRequest);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            return false;
        }
        return true;
    }
}
