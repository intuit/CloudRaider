package com.intuit.cloudraider.client.env.dev;

import com.intuit.cloudraider.client.component.GenericEnv;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Created by sabdulaziz on 4/17/18.
 */
@Component
public class DevEnv implements GenericEnv {
	
	private String envName = "dev";
	
	@Value("${profile.name}")
	private String profileName;

	public String getEnvName() {
		return envName;
	}

	public void setEnvName(String envName) {
		this.envName = envName;
	}

	public String getProfileName() {
		return profileName;
	}

	public void setProfileName(String profileName) {
		this.profileName = profileName;
	}

	@Override
	public String toString() {
		return "DevEnv [envName=" + envName + ", profileName=" + profileName
				+ "]";
	}

}
