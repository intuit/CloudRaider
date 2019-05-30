package com.intuit.cloudraider.client.component;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertyResolver;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by sabdulaziz on 4/17/18.
 */
@Component
public class PropertiesHolder {



    @Value("${myservice1.stacks}")
    private String myservice1_stack;

    @Value("${myservice1.elbs}")
    private String myservice1_elbs;

    @Value("${myservice1.Rout53}")
    private String myservice1_Rout53;

    @Value("${myservice1.volumes}")
    private String myservice1_volumes;

    @Value("${myservice2.stacks}")
    private String myservice2_stack;

    @Value("${myservice2.elbs}")
    private String myservice2_elbs;

    @Value("${myservice2.Rout53}")
    private String myservice2_Rout53;

    @Value("${myservice2.volumes}")
    private String myservice2_volumes;

    @Value("${myservice3.stacks}")
    private String myservice3_stack;

    @Value("${myservice3.elbs}")
    private String myservice3_elbs;

    @Value("${myservice3.Rout53}")
    private String myservice3_Rout53;

    @Value("${myservice3.volumes}")
    private String myservice3_volumes;

    public String getMyservice1_stack() {
        return myservice1_stack;
    }

    public String getMyservice1_elbs() {
        return myservice1_elbs;
    }

    public String getMyservice1_Rout53() {
        return myservice1_Rout53;
    }

    public String getMyservice1_volumes() {
        return myservice1_volumes;
    }

    public String getMyservice2_stack() {
        return myservice2_stack;
    }

    public String getMyservice2_elbs() {
        return myservice2_elbs;
    }

    public String getMyservice2_Rout53() {
        return myservice2_Rout53;
    }

    public String getMyservice2_volumes() {
        return myservice2_volumes;
    }

    public String getMyservice3_stack() {
        return myservice3_stack;
    }

    public String getMyservice3_elbs() {
        return myservice3_elbs;
    }

    public String getMyservice3_Rout53() {
        return myservice3_Rout53;
    }

    public String getMyservice3_volumes() {
        return myservice3_volumes;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("PropertiesHolder{");
        sb.append("myservice1_stack='").append(myservice1_stack).append('\'');
        sb.append(", myservice1_elbs='").append(myservice1_elbs).append('\'');
        sb.append(", myservice1_Rout53='").append(myservice1_Rout53).append('\'');
        sb.append(", myservice1_volumes='").append(myservice1_volumes).append('\'');
        sb.append(", myservice2_stack='").append(myservice2_stack).append('\'');
        sb.append(", myservice2_elbs='").append(myservice2_elbs).append('\'');
        sb.append(", myservice2_Rout53='").append(myservice2_Rout53).append('\'');
        sb.append(", myservice2_volumes='").append(myservice2_volumes).append('\'');
        sb.append(", myservice3_stack='").append(myservice3_stack).append('\'');
        sb.append(", myservice3_elbs='").append(myservice3_elbs).append('\'');
        sb.append(", myservice3_Rout53='").append(myservice3_Rout53).append('\'');
        sb.append(", myservice3_volumes='").append(myservice3_volumes).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
