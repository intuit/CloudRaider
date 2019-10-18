package com.intuit.cloudraider.commons;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder;
import com.amazonaws.services.securitytoken.model.GetCallerIdentityRequest;
import com.amazonaws.services.securitytoken.model.GetCallerIdentityResult;
import com.intuit.cloudraider.model.Credentials;

@Component
public abstract class DelegatorBase<T> {

    private T client;
    
	Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private static final String TARGET_ACCOUNT = System.getenv("TargetAccount");

    @Autowired
    private Credentials credentials;
    
    protected abstract T buildClient(AWSCredentials creds, String region);

    @PostConstruct
    private void init() {
    	validateAccount();
        client = buildClient(credentials.getAwsCredentials(), credentials.getRegion());
    }
    
    protected T getClient() {
    	return client;
    }
    
    private void validateAccount() {
    	
    	if (TARGET_ACCOUNT != null) {
	    	AWSSecurityTokenService sts = getSecurityTokenService();
	    	GetCallerIdentityRequest getCallerIdentityRequest = new GetCallerIdentityRequest();
			GetCallerIdentityResult result = sts.getCallerIdentity(getCallerIdentityRequest);
			if (!result.getAccount().equals(TARGET_ACCOUNT.replace("-", "").trim())) {
				throw new RuntimeException(String.format("account: %s does not match target account: %s", result.getAccount(), TARGET_ACCOUNT));
			}
    	}
    }
    
    protected AWSSecurityTokenService getSecurityTokenService() {
    	return AWSSecurityTokenServiceClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(credentials.getAwsCredentials())).build();
    }
    
}
