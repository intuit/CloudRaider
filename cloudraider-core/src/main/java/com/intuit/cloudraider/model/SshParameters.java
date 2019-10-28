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

package com.intuit.cloudraider.model;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import com.google.common.base.Strings;
import com.intuit.cloudraider.utils.ConfigUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Object to keep track of SSH parameters set by the user.
 * <p>
  */
public class SshParameters {
    private static SshParameters ourInstance = new SshParameters();

    /**
     * The Logger.
     */
    Logger logger = LoggerFactory.getLogger(this.getClass());
    /**
     * Gets instance.
     *
     * @return the instance
     */
    public static SshParameters getInstance() {
        return ourInstance;
    }

    private String privateKeyPath;
    private String username;
    private String bastionHost;
    private String passPhrase;

    private byte[] privateKey = null;

    /**
     * The Prop.
     */
    Properties prop = new Properties();
    /**
     * The Input.
     */
    InputStream input = null;

    private SshParameters() {

        try {

            String configfile = ConfigUtils.getConfigFilePath();
            input = getClass().getClassLoader().getResourceAsStream(configfile);

            prop.load(input);
            this.privateKeyPath = prop.getProperty("aws.ec2.privateKeyPath");
            this.username = prop.getProperty("aws.ec2.username");
            this.bastionHost = prop.getProperty("aws.ec2.bastionIp");
            this.passPhrase = prop.getProperty("aws.ec2.privateKeyPassPhrase");
            logger.debug("System property - bastionHost=åå" + bastionHost);

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return "SshParameters{" +
                ", username='" + username + '\'' +
                ", privateKeyPath='" + privateKeyPath + '\'' +
                ", bastionHost='" + bastionHost + '\'' +
                '}';
    }

    /**
     * Gets private key path.
     *
     * @return the private key path
     */
    public String getPrivateKeyPath() {
        return privateKeyPath;
    }

    /**
     * Gets username.
     *
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Gets bastion host.
     *
     * @return the bastion host
     */
    public String getBastionHost() {
        return bastionHost;
    }

    /**
     * Gets pass phrase.
     *
     * @return the pass phrase
     */
    public String getPassPhrase()
    {
        return passPhrase;
    }

    /**
     * Get private key byte [ ].
     *
     * @return the byte [ ]
     */
    public byte[] getPrivateKey() {
        return privateKey;
    }

    /**
     * Sets private key.
     *
     * @param privateKey the private key
     */
    public void setPrivateKey(byte[] privateKey) {
        this.privateKey = privateKey;
    }
}
