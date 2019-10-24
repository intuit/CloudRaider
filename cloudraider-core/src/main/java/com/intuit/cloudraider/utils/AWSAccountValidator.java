package com.intuit.cloudraider.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder;
import com.amazonaws.services.securitytoken.model.GetCallerIdentityRequest;
import com.amazonaws.services.securitytoken.model.GetCallerIdentityResult;

public class AWSAccountValidator {
		
    /**
     * The Logger.
     */
    Logger logger = LoggerFactory.getLogger(this.getClass());

    private AWSSecurityTokenService sts;
	
	
	public AWSSecurityTokenService getSts() {
		return sts;
	}

	public void setSts(AWSSecurityTokenService sts) {
		this.sts = sts;
	}

	public AWSAccountValidator(AWSCredentialsProvider provider) {
		sts = AWSSecurityTokenServiceClientBuilder.standard().withCredentials(provider).build();	
	}
		
    public void validateAccount(String targetAccount) {
    	
    	if (targetAccount != null) {
	    	GetCallerIdentityRequest getCallerIdentityRequest = new GetCallerIdentityRequest();
			GetCallerIdentityResult result = sts.getCallerIdentity(getCallerIdentityRequest);
			if (!result.getAccount().equals(targetAccount.trim().replace("-", ""))) {
				throw new RuntimeException(String.format("account: %s does not match target account: %s", result.getAccount(), targetAccount));
			}
    	} else {
    		logger.info("No targetAccount was set; skipping AWS Account validation");
    	}
    }
}
