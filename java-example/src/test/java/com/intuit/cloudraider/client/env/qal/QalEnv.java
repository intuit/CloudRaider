package com.intuit.cloudraider.client.env.qal;

import com.intuit.cloudraider.client.component.GenericEnv;
import org.springframework.beans.factory.annotation.Value;

/**
 * Created by sabdulaziz on 4/17/18.
 */
public class QalEnv implements GenericEnv {

    private String envName = "qal";

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
