package com.intuit.cloudraider.model;

import org.mockito.Mockito;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.intuit.cloudraider.utils.AWSAccountValidator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

import org.junit.Test;

public class BasicCredentialsTest {
	
	@Test
	public void testValidateAccountNull() {
		System.setProperty("aws.accessKeyId", "accessKey");
		System.setProperty("aws.secretKey", "secret");
		AWSAccountValidator mockValidator = Mockito.mock(AWSAccountValidator.class);
		BasicCredentials creds = new BasicCredentials(mockValidator);
		
		creds.getAwsCredentials();
		
		verify(mockValidator).validateAccount(null);
		
	}
	
	@Test
	public void testValidateAccount() {
		System.setProperty("aws.accessKeyId", "accessKey");
		System.setProperty("aws.secretKey", "secret");
		AWSAccountValidator mockValidator = Mockito.mock(AWSAccountValidator.class);
		BasicCredentials creds = new BasicCredentials(mockValidator);
		creds.setTargetAccount("1234");
		
		creds.getAwsCredentials();
		
		verify(mockValidator).validateAccount("1234");
		
	}

	
	@Test
	public void testValidateAccountInvalid() {
		AWSAccountValidator mockValidator = Mockito.mock(AWSAccountValidator.class);
		BasicCredentials creds = new BasicCredentials(mockValidator);
		creds.setTargetAccount("1234");
		RuntimeException expected = new RuntimeException();
		
		doThrow(expected).when(mockValidator).validateAccount("1234");
		
		try {
			creds.getAwsCredentials();
			fail("getAwsCredentials did not throw an exception");
		} catch(RuntimeException e) {
			assertSame(expected, e);
		}
		
		
	}
	
	@Test
	public void testSetTargetAccountViaSystemProperties() {
		System.setProperty("aws.targetAccount", "76543");
		BasicCredentials creds = new BasicCredentials();
		assertEquals("76543", creds.getTargetAccount());
	}
	
}
