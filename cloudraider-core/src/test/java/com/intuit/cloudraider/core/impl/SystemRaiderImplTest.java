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

import com.intuit.cloudraider.commons.CloudRaiderSSHSessionFactory;
import com.intuit.cloudraider.commons.SystemDelegator;
import com.intuit.cloudraider.core.interfaces.SystemRaider;
import com.intuit.cloudraider.model.BasicCredentials;
import com.intuit.cloudraider.model.Credentials;
import com.intuit.cloudraider.model.SshParameters;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.Session;
import com.pastdev.jsch.SessionFactory;
import com.pastdev.jsch.proxy.SshProxy;
import com.pastdev.jsch.scp.ScpFile;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;


@PowerMockIgnore({"javax.management.*","javax.net.ssl.*"})
@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@PrepareForTest({SystemRaiderImpl.class, Credentials.class, SystemDelegator.class, ScpFile.class})
public class SystemRaiderImplTest {


    @Autowired
    private  SystemRaider systemRaider;

    @Autowired
    private  SystemDelegator systemDelegator;

    private  SshParameters sshParameters;
    private  CloudRaiderSSHSessionFactory mockedSessionFactory;

    /**
     * Sets .
     *
     * @throws Exception the exception
     */
    @Before
    public  void setup() throws Exception {

        sshParameters = SshParameters.getInstance();

       PowerMockito.when(systemDelegator.getSshParameters()).thenReturn(sshParameters);

        Session session = PowerMockito.mock(Session.class);


        ChannelExec channel = PowerMockito.mock(ChannelExec.class);


        SshProxy sshProxy = PowerMockito.mock(SshProxy.class);
        ScpFile scpFile = PowerMockito.mock(ScpFile.class);
        InputStream inputStream = PowerMockito.mock(InputStream.class);

        PowerMockito.whenNew(SshProxy.class).withAnyArguments().thenReturn(sshProxy);


        mockedSessionFactory = PowerMockito.mock(CloudRaiderSSHSessionFactory.class);


        PowerMockito.whenNew(CloudRaiderSSHSessionFactory.class).withAnyArguments().thenReturn(mockedSessionFactory);

        SessionFactory.SessionFactoryBuilder sessionFactoryBuilder = PowerMockito.mock(SessionFactory.SessionFactoryBuilder.class);

        PowerMockito.when(mockedSessionFactory.newSessionFactoryBuilder()).thenReturn(sessionFactoryBuilder);

        PowerMockito.when(mockedSessionFactory.newSessionFactoryBuilder().setHostname(systemDelegator.getSshParameters().getBastionHost())).thenReturn(sessionFactoryBuilder);
        PowerMockito.when(mockedSessionFactory.newSessionFactoryBuilder().setPort(SessionFactory.SSH_PORT)).thenReturn(sessionFactoryBuilder);


        PowerMockito.when(mockedSessionFactory.newSessionFactoryBuilder().setProxy(sshProxy)).thenReturn(sessionFactoryBuilder);
        PowerMockito.when(mockedSessionFactory.newSessionFactoryBuilder().build()).thenReturn(mockedSessionFactory);

        PowerMockito.when(mockedSessionFactory.newSession()).thenReturn(session);
        PowerMockito.when(session.openChannel("exec")).thenReturn(channel);

        PowerMockito.whenNew(ScpFile.class).withAnyArguments().thenReturn(scpFile);

        PowerMockito.whenNew(ScpFile.class).withArguments(Mockito.anyObject(),Mockito.anyString(),Mockito.anyString(),Mockito.anyString(),Mockito.anyString(),Mockito.anyString(),Mockito.anyString()).thenReturn(scpFile);

        PowerMockito.when(channel.getInputStream()).thenReturn(inputStream);
        PowerMockito.when(inputStream.available()).thenReturn(1);
        PowerMockito.when(inputStream.read(any(byte[].class),anyInt(),anyInt())).thenReturn(-1);
        PowerMockito.when(channel.getOutputStream()).thenReturn(PowerMockito.mock(OutputStream.class));

    }

    /**
     * Execute test.
     *
     * @throws URISyntaxException the uri syntax exception
     */
    @Test
    public void executeTest() throws URISyntaxException {
        String path  = getClass().getClassLoader().getResource("test.sh").getPath();
        Assert.assertEquals("success",systemRaider.executeScript("127.0.0.1",path,"param1","param2"));
    }


    /**
     * Execute file not found test.
     */
    @Test
    public void executeFileNotFoundTest(){

        Assert.assertEquals("Failed","unable to copy file to the host, error: java.io.FileNotFoundException: invalid.sh (No such file or directory)",systemRaider.executeScript("127.0.0.1","invalid.sh","param1","param2"));
    }

    /**
     * Execute with list of params test.
     */
    @Test
    public void executeWithListOfParamsTest(){

        String path  = getClass().getClassLoader().getResource("test.sh").getPath();
        List<String> params = new ArrayList<String>();
        params.add("param1");
        params.add("param2");
        Assert.assertEquals("success",systemRaider.executeScript("127.0.0.1",path,params));
    }


    /**
     * The type System raider impl test context configuration.
     */
    @Configuration
    protected static class SystemRaiderImplTestContextConfiguration {

        /**
         * System delegator system delegator.
         *
         * @return the system delegator
         */
        @Bean
        public SystemDelegator systemDelegator() {
            return Mockito.mock(SystemDelegator.class);
        }

        /**
         * System raider system raider.
         *
         * @return the system raider
         */
        @Bean
        public SystemRaider systemRaider() {
            return new SystemRaiderImpl();
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