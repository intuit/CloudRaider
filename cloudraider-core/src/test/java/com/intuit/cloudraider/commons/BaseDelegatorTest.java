package com.intuit.cloudraider.commons;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.model.GetCallerIdentityRequest;
import com.amazonaws.services.securitytoken.model.GetCallerIdentityResult;
import com.intuit.cloudraider.model.BasicCredentials;
import com.intuit.cloudraider.model.Credentials;

import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class BaseDelegatorTest {

	public AWSSecurityTokenService sts = mock(AWSSecurityTokenService.class);
	
	DelegatorBase<String> delegator;

	@Test
	public void testCorrectAccount() {
		when(sts.getCallerIdentity((GetCallerIdentityRequest) notNull())).thenAnswer(new Answer<GetCallerIdentityResult>() {

			@Override
			public GetCallerIdentityResult answer(InvocationOnMock invocation) throws Throwable {
				GetCallerIdentityResult result = new GetCallerIdentityResult();
				result.setAccount("1234567890");
				return result;
			}
			
		});
		
		TestDelegator d = new TestDelegator(sts);
	
				
	}
	
	
	protected static class TestDelegator extends DelegatorBase<String> {

		AWSSecurityTokenService sts;
		public TestDelegator(AWSSecurityTokenService sts) {
			this.sts = sts;
		}
		
		@Override
		protected String buildClient(AWSCredentials creds, String region) {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		protected AWSSecurityTokenService getSecurityTokenService() { return sts;}
		
		
	}
	
	/**
     * The type Asg delegator test context configuration.
     */
    @Configuration
    protected static class ASGDelegatorTestContextConfiguration {

    	/**
         * Asg delegator asg delegator.
         *
         * @return the asg delegator
         */
        @Bean
        public ASGDelegator asgDelegator() {
            return new ASGDelegator();
        }

        /**
         * Credentials credentials.
         *
         * @return the credentials
         */
        @Bean
        public Credentials credentials() {
            return new BasicCredentials();
        }
        
    }
    
}
