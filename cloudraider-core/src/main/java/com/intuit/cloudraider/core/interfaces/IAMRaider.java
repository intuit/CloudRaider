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

package com.intuit.cloudraider.core.interfaces;


import com.amazonaws.services.identitymanagement.model.AttachedPolicy;

import java.util.List;

/**
 * The interface Iam raider.
 */
public interface IAMRaider {

    /**
     * Gets role policy document.
     *
     * @param roleName   the role name
     * @param policyName the policy name
     * @return the role policy document
     */
    public String getRolePolicyDocument (String roleName, String policyName);

    /**
     * Gets role policy list.
     *
     * @param roleName the role name
     * @return the role policy list
     */
    public List<AttachedPolicy> getRolePolicyList(String roleName);

    /**
     * Gets role policy arn.
     *
     * @param roleName   the role name
     * @param policyName the policy name
     * @return the role policy arn
     */
    public String getRolePolicyArn(String roleName, String policyName);

    /**
     * Create and attach policy string.
     *
     * @param roleName       the role name
     * @param policyName     the policy name
     * @param policyDocument the policy document
     * @return the string
     */
    public String createAndAttachPolicy(String roleName, String policyName, String policyDocument);

    /**
     * Detach and delete policy boolean.
     *
     * @param policyArn the policy arn
     * @param roleName  the role name
     * @return the boolean
     */
    public boolean detachAndDeletePolicy(String policyArn, String roleName);
}
