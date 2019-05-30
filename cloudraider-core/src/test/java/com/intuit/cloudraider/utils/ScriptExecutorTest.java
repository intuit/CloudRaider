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

package com.intuit.cloudraider.utils;

import com.intuit.cloudraider.commons.SystemDelegator;
import com.intuit.cloudraider.core.impl.SystemRaiderImpl;
import com.intuit.cloudraider.cucumber.model.ExecutionStateCache;
import com.intuit.cloudraider.cucumber.util.ScriptExecutor;
import com.intuit.cloudraider.model.BasicCredentials;
import com.intuit.cloudraider.model.Credentials;
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

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;


@PowerMockIgnore({"javax.management.*","javax.net.ssl.*"})
@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@PrepareForTest({ScriptExecutor.class, SystemRaiderImpl.class})

public class ScriptExecutorTest {


    @Autowired
    private   ScriptExecutor scriptExecutor;

    private Path mockedPath = Paths.get("\\tmp");


    /**
     * Sets method.
     *
     * @throws Exception the exception
     */
    @Before
    public void setupMethod() throws Exception{

         File mockFile = PowerMockito.mock(File.class);

        PowerMockito.mockStatic(ClassLoader.class);
        URL mockURL = PowerMockito.mock(URL.class);
        PowerMockito.when(ClassLoader.getSystemResource(Mockito.anyString())).thenReturn(mockURL);

        PowerMockito.when(mockURL.getFile()).thenReturn("/test");


         PowerMockito.whenNew(File.class).withAnyArguments().thenReturn(mockFile);
         PowerMockito.when(mockFile.getAbsolutePath()).thenReturn("/test");



    }

    /**
     * Test execute process termination.
     *
     * @throws Exception the exception
     */
    @Test
    public void testExecuteProcessTermination() throws Exception
    {
       scriptExecutor.executeProcessTermination("10.0.0.1","nginx");

    }


    /**
     * Test execute disk full.
     *
     * @throws Exception the exception
     */
    @Test
    public void testExecuteDiskFull() throws Exception
    {
        scriptExecutor.executeDiskFull("10.0.0.1","root", 50);

    }

    /**
     * Test execute cpu spike.
     *
     * @throws Exception the exception
     */
    @Test
    public void testExecuteCPUSpike() throws Exception
    {
        scriptExecutor.executeCPUSpike("10.0.0.1",4);

    }

    /**
     * Test execute stop process.
     *
     * @throws Exception the exception
     */
    @Test
    public void testExecuteStopProcess() throws Exception
    {
        scriptExecutor.executeStopProcess("10.0.0.1","nginx");

    }

    /**
     * Test execute start process.
     *
     * @throws Exception the exception
     */
    @Test
    public void testExecuteStartProcess() throws Exception
    {
        scriptExecutor.executeStartProcess("10.0.0.1","nginx");

    }

    /**
     * Test execute random network latency.
     *
     * @throws Exception the exception
     */
    @Test
    public void testExecuteRandomNetworkLatency() throws Exception
    {
        scriptExecutor.executeRandomNetworkLatency("10.0.0.1","50","20");

    }

    /**
     * Test execute network latency.
     *
     * @throws Exception the exception
     */
    @Test
    public void testExecuteNetworkLatency() throws Exception
    {
        scriptExecutor.executeNetworkLatency("10.0.0.1","200");

    }

    /**
     * Test execute networ packet loss.
     *
     * @throws Exception the exception
     */
    @Test
    public void testExecuteNetworPacketLoss() throws Exception
    {
        scriptExecutor.executeNetworkPacketLoss("10.0.0.1","20");

    }

    /**
     * Test execute block domain.
     *
     * @throws Exception the exception
     */
    @Test
    public void testExecuteBlockDomain() throws Exception
    {
        scriptExecutor.executeBlockDomain("10.0.0.1","test.com");

    }

    /**
     * Test execute block port.
     *
     * @throws Exception the exception
     */
    @Test
    public void testExecuteBlockPort() throws Exception
    {
        scriptExecutor.executeBlockPort("10.0.0.1","8080");

    }

    /**
     * Test execute un block domain.
     *
     * @throws Exception the exception
     */
    @Test
    public void testExecuteUnBlockDomain() throws Exception
    {
        scriptExecutor.executeUnBlockDomain("10.0.0.1","test.com");

    }

    /**
     * Test execute script.
     *
     * @throws Exception the exception
     */
    @Test
    public void testExecuteScript() throws Exception
    {
        scriptExecutor.executeScript("test","10.0.0.1","test.com");

    }


    /**
     * The type Script executor test context configuration.
     */
    @Configuration
    protected static class ScriptExecutorTestContextConfiguration {

        /**
         * Script executor script executor.
         *
         * @return the script executor
         */
        @Bean(name = {"scriptExecutor"})
        public ScriptExecutor scriptExecutor() {
            return new ScriptExecutor();
        }

        /**
         * System raider system raider.
         *
         * @return the system raider
         */
        @Bean(name = {"systemRaiderBean"})
            public SystemRaiderImpl systemRaider() {
                return Mockito.mock(SystemRaiderImpl.class);
            }

        /**
         * Credentials credentials.
         *
         * @return the credentials
         */
        @Bean
            public Credentials credentials () {
                return new BasicCredentials();
            }


        /**
         * Execution state cache execution state cache.
         *
         * @return the execution state cache
         */
        @Bean
            public ExecutionStateCache executionStateCache () {
                return new ExecutionStateCache();
            }

        /**
         * System delegator system delegator.
         *
         * @return the system delegator
         */
        @Bean
            public SystemDelegator systemDelegator () {
                return Mockito.mock(SystemDelegator.class);
            }

    }



}
