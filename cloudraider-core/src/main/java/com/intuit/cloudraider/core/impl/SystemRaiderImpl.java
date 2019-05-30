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

package com.intuit.cloudraider.core.impl;

import com.google.common.base.Strings;
import com.google.common.net.InetAddresses;
import com.intuit.cloudraider.commons.CloudRaiderSSHSessionFactory;
import com.intuit.cloudraider.commons.SystemDelegator;
import com.intuit.cloudraider.core.interfaces.SystemRaider;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.pastdev.jsch.SessionFactory;
import com.pastdev.jsch.proxy.SshProxy;
import com.pastdev.jsch.scp.ScpFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Script Execution functionality.
 * <p>
  */
@Component (value="systemRaiderBean")
public class SystemRaiderImpl implements SystemRaider {

    /**
     * The System delegator.
     */
    @Autowired
    private SystemDelegator systemDelegator;

    /**
     * The Logger.
     */
    Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * Instantiates a new System raider.
     */
    public SystemRaiderImpl() {
           }
    

    /**
     * Execute the given script on the specified instance with parameters.
     *
     * @param ip private ip address of AWS resources
     * @param path path to script
     * @param params parameters for commands
     * @return execution response
     */
    @Override
    public String executeScript(String ip, String path, String... params) {
        String response;
        SessionFactory sessionFactory;
        try {
            sessionFactory = createSessionFactory(ip);
            scpScript(sessionFactory, path);
            response = executer(ip, params);

        } catch (JSchException e) {
            return "unable to connect to " + ip;
        } catch (IOException e) {
            return "unable to copy file to the host, error: " + e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return response;

    }

    /**
     * Execute the given script on the specified instance with parameters.
     *
     * @param ip private ip address of AWS resources
     * @param path path to script
     * @param params list of command parameters
     * @return execution response
     */
    @Override
    public String executeScript(String ip, String path, List<String> params) {
        String response;
        SessionFactory sessionFactory;
        try {
            sessionFactory = createSessionFactory(ip);
            scpScript(sessionFactory, path);
            response = executer(ip, params);

        } catch (JSchException e) {
            return "unable to connect to " + ip;
        } catch (IOException e) {
            return "unable to copy file to the host, error: " + e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return response;

    }


    /**
     * Generates SSH session with the given ip address.
     *
     * @param ip ip address
     * @return SessionFactory
     */
    private SessionFactory createSessionFactory(String ip) throws JSchException, IOException {
    	
    	Properties prop = new Properties();
        InputStream input;
        String configfile = System.getProperty("config.file");
        logger.debug("System property config.file= " + configfile);

        if(Strings.isNullOrEmpty(configfile)){
            configfile = "config.properties";
        }
        input = ClassLoader.getSystemResourceAsStream(configfile);
        prop.load(input);
        
        String skipbastion = prop.getProperty("skipbastion");

        InetAddresses.forString(ip);
        logger.debug("SSH params = " + systemDelegator.getSshParameters());
        CloudRaiderSSHSessionFactory sessionFactory = new CloudRaiderSSHSessionFactory(
                systemDelegator.getSshParameters().getUsername(), ip, SessionFactory.SSH_PORT );


        sessionFactory.addIdentityFromPrivateKey(systemDelegator.getSshParameters().getPrivateKeyPath(), systemDelegator.getSshParameters().getPassPhrase());
        
        sessionFactory.printIdentities();
        Map<String,String> config = new HashMap<String, String>();

        config.put("StrictHostKeyChecking", "no");
        config.put("PreferredAuthentications", "publickey");

        sessionFactory.setConfig(config);

 //   	logger.debug(skipbastion);

        if(skipbastion!=null && skipbastion.equalsIgnoreCase("yes"))
        {
        	SessionFactory destinationSessionFactory = sessionFactory
                    .newSessionFactoryBuilder()
                    .build();

            logger.debug("SSH HostName: " + destinationSessionFactory.getHostname());
            logger.debug("SSH Port: " +String.valueOf(destinationSessionFactory.getPort()));
        	
            return destinationSessionFactory;
        }
        else
        {
	        SessionFactory proxySessionFactory = sessionFactory
	                .newSessionFactoryBuilder()
	                .setHostname( systemDelegator.getSshParameters().getBastionHost() )
	                .setPort( SessionFactory.SSH_PORT )
	                .build();
	
	        SessionFactory destinationSessionFactory = sessionFactory
	                .newSessionFactoryBuilder()
	                .setProxy( new SshProxy( proxySessionFactory ) )
	                .build();

            logger.debug("SSH HostName: " + destinationSessionFactory.getHostname());
            logger.debug("SSH Port: " +String.valueOf(destinationSessionFactory.getPort()));
        	
	
	        return destinationSessionFactory;
        }
    }

    /**
     * Copy the provided script over to the SSH'd instance using SCP.
     *
     * @param sessionFactory SessionFactory
     * @param scriptPath path to find script
     * @throws FileNotFoundException
     */
    private void scpScript(SessionFactory sessionFactory, String scriptPath) throws FileNotFoundException {
        FileReader fr = new FileReader(scriptPath);
        File file = new File(scriptPath);

        try {
            // ScpFile to = new ScpFile( sessionFactory, "/home/"+systemDelegator.getSshParameters().getUsername()+"/action.sh" );
            ScpFile to = new ScpFile(sessionFactory, "/", "home", "/", systemDelegator.getSshParameters().getUsername(), "/", "action.sh");
            to.copyFrom(file);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private String executer(String ip) throws Exception {
        executer("sh /home/" + systemDelegator.getSshParameters().getUsername() + "/action.sh", createSessionFactory(ip).newSession());
        return "success";
    }

    private String executer(String ip, String... params) throws Exception {
        executer("sh /home/" + systemDelegator.getSshParameters().getUsername() + "/action.sh", createSessionFactory(ip).newSession(), params);
        return "success";
    }

    private String executer(String ip, List<String> params) throws Exception {
        executer("sh /home/" + systemDelegator.getSshParameters().getUsername() + "/action.sh", createSessionFactory(ip).newSession(), params);
        return "success";
    }

    /**
     * Executes the given command and its paramters on the specified session.
     *
     * @param command command to run
     * @param session Session to run command on
     * @param params command parameters
     * @return true
     * @throws Exception if error occurred during execution
     */
    private Boolean executer(String command, Session session, String... params) throws Exception {
        try {
            session.connect();
            Channel channel = session.openChannel("exec");

            channel.setInputStream(null);

            channel.setOutputStream(System.out);

            String parameters = "";
            for (String p : params) {
                parameters += " " + p;
            }
            ((ChannelExec) channel).setCommand("sudo " + command + parameters);

            OutputStream out = channel.getOutputStream();

            channel.connect();

            InputStream in = channel.getInputStream();
            Boolean exit = false;
            byte[] tmp = new byte[1024];

            while (true) {
                while (in.available() > 0) {
                    int i = in.read(tmp, 0, 1024);
                    if (i < 0) {
                        exit = true;
                        break;
                    }
                    String output = new String(tmp, 0, i);
                    if (output.contains("executing..")) {
                        exit = true;
                        break;
                    }
                }
                if (channel.isClosed()) {
                    logger.debug("SSH exit-status:" + channel.getExitStatus());
                    break;
                }
                if (exit) {
                    break;
                }
                try {
                    Thread.sleep(200);
                } catch (Exception ee) {
                    throw new Exception(ee);
                }
            }
            channel.disconnect();
            session.disconnect();
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     *
     * @param command
     * @param session
     * @param params
     * @return
     * @throws Exception
     */
    private Boolean executer(String command, Session session, List<String> params) throws Exception {
        try {
            session.connect();
            Channel channel = session.openChannel("exec");

            channel.setInputStream(null);

            channel.setOutputStream(System.out);

            String parameters = "";
            for (String p : params) {
                parameters += " " + p;
            }
            ((ChannelExec) channel).setCommand("sudo " + command + parameters);

            OutputStream out = channel.getOutputStream();

            channel.connect();

            InputStream in = channel.getInputStream();
            Boolean exit = false;
            byte[] tmp = new byte[1024];

            while (true) {
                while (in.available() > 0) {
                    int i = in.read(tmp, 0, 1024);
                    if (i < 0) {
                        exit = true;
                        break;
                    }
                    String output = new String(tmp, 0, i);
                    if (output.contains("executing..")) {
                        exit = true;
                        break;
                    }
                }
                if (channel.isClosed()) {
                    logger.debug("SSH exit-status:" + channel.getExitStatus());
                    break;
                }
                if (exit) {
                    break;
                }
                try {
                    Thread.sleep(200);
                } catch (Exception ee) {
                    throw new Exception(ee);
                }
            }
            channel.disconnect();
            session.disconnect();
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
