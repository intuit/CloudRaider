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

package com.intuit.cloudraider.commons;

import com.intuit.cloudraider.model.SshParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Proxy class for managing SSH Parameters.
 * <p>
  */
@Component
public class SystemDelegator {

    /**
     * The Logger.
     */
    Logger logger = LoggerFactory.getLogger(this.getClass());

    private SshParameters sshParameters;

    /**
     * Instantiates a new System delegator.
     */
    public SystemDelegator() {
        sshParameters = SshParameters.getInstance();

    }

    /**
     * Gets ssh parameters.
     *
     * @return the ssh parameters
     */
    public SshParameters getSshParameters() {
        return sshParameters;
    }

    /**
     * Sets ssh parameters.
     *
     * @param sshParameters the ssh parameters
     */
    public void setSshParameters(SshParameters sshParameters) {
        this.sshParameters = sshParameters;
    }

    /**
     * Instantiates a new System delegator.
     *
     * @param sshParameters the ssh parameters
     */
    public SystemDelegator(SshParameters sshParameters){
        this.sshParameters = sshParameters;
    }
}
