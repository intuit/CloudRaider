package com.intuit.cloudraider.utils;

import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.model.GetCallerIdentityRequest;
import com.amazonaws.services.securitytoken.model.GetCallerIdentityResult;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

import org.junit.Test;

public class AWSAccountValidatorTest {
	
	@Test
	public void validateAccountNullTest() {
		
		AWSCredentialsProvider mockProvider = Mockito.mock(AWSCredentialsProvider.class);
		AWSAccountValidator validator = new AWSAccountValidator(mockProvider);
		
		AWSSecurityTokenService mockSts = Mockito.mock(AWSSecurityTokenService.class);
		
		assertNotNull(validator.getSts());
		
		validator.setSts(mockSts);
		
		validator.validateAccount(null);
		
		verifyZeroInteractions(mockSts);
		
	}
	
	@Test
	public void validateAccountNotValid() {
		
		AWSCredentialsProvider mockProvider = Mockito.mock(AWSCredentialsProvider.class);
		AWSAccountValidator validator = new AWSAccountValidator(mockProvider);
		
		AWSSecurityTokenService mockSts = Mockito.mock(AWSSecurityTokenService.class);
		
		assertNotNull(validator.getSts());
		
		validator.setSts(mockSts);
		
		when(mockSts.getCallerIdentity((GetCallerIdentityRequest) notNull())).thenAnswer(new Answer<GetCallerIdentityResult>() {

			@Override
			public GetCallerIdentityResult answer(InvocationOnMock invocation) throws Throwable {
				GetCallerIdentityResult result = new GetCallerIdentityResult();
				result.setAccount("5678");
				return result;
			}
			
		});
		
		try {
		validator.validateAccount("1234");
		fail("validateAccount did not throw an exception");
		} catch (RuntimeException e) {
			assertEquals("account: 5678 does not match target account: 1234", e.getMessage());
		}		
	}

	@Test
	public void validateAccountValid() {
		
		AWSCredentialsProvider mockProvider = Mockito.mock(AWSCredentialsProvider.class);
		AWSAccountValidator validator = new AWSAccountValidator(mockProvider);
		
		AWSSecurityTokenService mockSts = Mockito.mock(AWSSecurityTokenService.class);
		
		assertNotNull(validator.getSts());
		
		validator.setSts(mockSts);
		
		when(mockSts.getCallerIdentity((GetCallerIdentityRequest) notNull())).thenAnswer(new Answer<GetCallerIdentityResult>() {

			@Override
			public GetCallerIdentityResult answer(InvocationOnMock invocation) throws Throwable {
				GetCallerIdentityResult result = new GetCallerIdentityResult();
				result.setAccount("1234");
				return result;
			}
			
		});
		
		validator.validateAccount("1234");
	}
	
	@Test
	public void validateAccountValidWithDashes() {
		
		AWSCredentialsProvider mockProvider = Mockito.mock(AWSCredentialsProvider.class);
		AWSAccountValidator validator = new AWSAccountValidator(mockProvider);
		
		AWSSecurityTokenService mockSts = Mockito.mock(AWSSecurityTokenService.class);
		
		assertNotNull(validator.getSts());
		
		validator.setSts(mockSts);
		
		when(mockSts.getCallerIdentity((GetCallerIdentityRequest) notNull())).thenAnswer(new Answer<GetCallerIdentityResult>() {

			@Override
			public GetCallerIdentityResult answer(InvocationOnMock invocation) throws Throwable {
				GetCallerIdentityResult result = new GetCallerIdentityResult();
				result.setAccount("1234");
				return result;
			}
			
		});
		
		validator.validateAccount("12-3-4-");
	}

	@Test
	public void validateAccountValidWithWhitespace() {
		
		AWSCredentialsProvider mockProvider = Mockito.mock(AWSCredentialsProvider.class);
		AWSAccountValidator validator = new AWSAccountValidator(mockProvider);
		
		AWSSecurityTokenService mockSts = Mockito.mock(AWSSecurityTokenService.class);
		
		assertNotNull(validator.getSts());
		
		validator.setSts(mockSts);
		
		when(mockSts.getCallerIdentity((GetCallerIdentityRequest) notNull())).thenAnswer(new Answer<GetCallerIdentityResult>() {

			@Override
			public GetCallerIdentityResult answer(InvocationOnMock invocation) throws Throwable {
				GetCallerIdentityResult result = new GetCallerIdentityResult();
				result.setAccount("1234");
				return result;
			}
			
		});
		
		validator.validateAccount("12-3-4- ");
	}
	
}
