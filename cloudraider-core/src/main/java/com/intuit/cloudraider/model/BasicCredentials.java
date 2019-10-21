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

import com.amazonaws.auth.*;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder;
import com.amazonaws.services.securitytoken.model.AssumeRoleRequest;
import com.amazonaws.services.securitytoken.model.GetCallerIdentityRequest;
import com.amazonaws.services.securitytoken.model.GetCallerIdentityResult;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Creates credentials for accessing AWS resources based on multiple sources including direct key input and the
 * local AWS credentials file.
 * <p>
  */
public class BasicCredentials implements Credentials{

    /**
     * The Logger.
     */
    Logger logger = LoggerFactory.getLogger(this.getClass());

    private AWSCredentials awsCredentials;
    private String region;

    private String AWSAccessKeyId;
    private String AWSSecretKey;
    private String AWSSessionToken;

	private String targetAccount;

	/**
     * Reads from config.properties to search for potential AWS account credentials.
     * The aws.region must be specified to proceed.
     * A "aws.keyless" option exists and requires "aws.assumerRole", "aws.deployerRole", "aws.uuid" to be specified within
     * config.properties.
     * If the keyless option is not chosen, then the credentials will first be searched for within config.properties,
     * starting with "aws.accessKeyId", "aws.secretKey", "aws.sessionToken".
     * If the above were not specified, the credentials found locally within ~/.aws/credentials will be used.
     * To use any non-default profiles, use the "aws.profile" option within config.properties.
     */
    public BasicCredentials() {
        Properties prop = new Properties();
        InputStream input;
        try {
            input = ClassLoader.getSystemResourceAsStream("config.properties");
            prop.load(input);
            prop.putAll(System.getProperties());
            region = prop.getProperty("aws.region");
            System.setProperty("aws.region", region);
            if(Strings.isNullOrEmpty(region)){
                throw new RuntimeException("No Region defined in the configuration file");
            }
            
            // Get the target account from env or properties if env var is not set
            targetAccount = System.getenv("aws.targetAccount") != null ? System.getenv("aws.targetAccount") : prop.getProperty("aws.targetAccount");
            
            String profile = prop.getProperty("aws.profile");
            boolean keyless = Boolean.valueOf(prop.getProperty("aws.keyless"));

            if (keyless) {
                String roleArn = prop.getProperty("aws.assumerRole");
                String roleArn2 = prop.getProperty("aws.deployerRole");
                String extID = prop.getProperty("aws.uuid");

                Regions awsRegion = Regions.fromName(region);

                final AssumeRoleRequest assumeRole = new AssumeRoleRequest()
                        .withRoleArn(roleArn)
                        .withRoleSessionName("chaos-session");
                final AWSSecurityTokenService sts = AWSSecurityTokenServiceClientBuilder
                        .standard()
                        .withRegion(awsRegion)
                        .build();

                final com.amazonaws.services.securitytoken.model.Credentials credentials = sts.assumeRole(assumeRole).getCredentials();

                final AWSCredentials awsSessionCredentials = new BasicSessionCredentials(
                        credentials.getAccessKeyId(),
                        credentials.getSecretAccessKey(),
                        credentials.getSessionToken());

                final AssumeRoleRequest assumeRole2 = new AssumeRoleRequest()
                        .withRoleArn(roleArn2)
                        .withRoleSessionName("chaos-session-2")
                        .withExternalId(extID);

                final AWSSecurityTokenService sts2 = AWSSecurityTokenServiceClientBuilder
                        .standard()
                        .withCredentials(new AWSStaticCredentialsProvider(awsSessionCredentials))
                        .withRegion(awsRegion)
                        .build();

                final com.amazonaws.services.securitytoken.model.Credentials credentials2 = sts2.assumeRole(assumeRole2).getCredentials();

                AWSAccessKeyId = credentials2.getAccessKeyId();
                AWSSecretKey = credentials2.getSecretAccessKey();
                AWSSessionToken = credentials2.getSessionToken();
            }
            else {
                AWSAccessKeyId = prop.getProperty("aws.accessKeyId");
                AWSSecretKey = prop.getProperty("aws.secretKey");
                AWSSessionToken = prop.getProperty("aws.sessionToken");
            }

            if (!Strings.isNullOrEmpty(AWSAccessKeyId) && !Strings.isNullOrEmpty(AWSSecretKey) && !Strings.isNullOrEmpty(AWSSessionToken)) {
                awsCredentials = new BasicSessionCredentials(AWSAccessKeyId, AWSSecretKey, AWSSessionToken);
            } else if (!Strings.isNullOrEmpty(AWSAccessKeyId) && !Strings.isNullOrEmpty(AWSSecretKey)) {
                awsCredentials = new BasicAWSCredentials(AWSAccessKeyId, AWSSecretKey);
            } else {
                if (Strings.isNullOrEmpty(profile)) {
                    awsCredentials = new ProfileCredentialsProvider().getCredentials();
                } else {
                    awsCredentials = new ProfileCredentialsProvider(profile).getCredentials();
                }
            }
            if (awsCredentials == null) {
                logger.error("No BasicCredentials provided");
                throw new RuntimeException("AWS credentials missing");
            }

        } catch (IOException e) {
            throw new RuntimeException("config file is not found");
        } catch (IllegalArgumentException e) {
            //ignore to use amazon default provider
        }
    }

    /**
     * Returns AWSCredentials for the given user/account. Reverts to default credentials provider if no credentials exist.
     * @return AWSCredentials
     */
    public AWSCredentials getAwsCredentials() {
        if (awsCredentials == null) {
        	AWSCredentialsProvider provider = new DefaultAWSCredentialsProviderChain();
        	validateAccount(provider);
            awsCredentials = provider.getCredentials();
        }
        return awsCredentials;
    }

    public AWSCredentialsProvider getAwsCredentialProvider() {
        return new DefaultAWSCredentialsProviderChain();
    }

    /**
     * Returns region inputted from config.properties
     * @return region inputted from config.properties; null if no region was inputted
     */
    public String getRegion() {
        return region;
    }

    private void validateAccount(AWSCredentialsProvider provider) {
    	
    	if (targetAccount != null) {
	    	AWSSecurityTokenService sts = getSecurityTokenService(provider);
	    	GetCallerIdentityRequest getCallerIdentityRequest = new GetCallerIdentityRequest();
			GetCallerIdentityResult result = sts.getCallerIdentity(getCallerIdentityRequest);
			if (!result.getAccount().equals(targetAccount.replace("-", "").trim())) {
				throw new RuntimeException(String.format("account: %s does not match target account: %s", result.getAccount(), targetAccount));
			}
    	}
    }

    protected AWSSecurityTokenService getSecurityTokenService(AWSCredentialsProvider provider) {
    	return AWSSecurityTokenServiceClientBuilder.standard().withCredentials(provider).build();
    }
    
}
