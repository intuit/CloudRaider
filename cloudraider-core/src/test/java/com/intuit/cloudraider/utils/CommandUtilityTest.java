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

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;


@RunWith(PowerMockRunner.class)
@PrepareForTest({Files.class, CommandUtility.class, Paths.class})
public class CommandUtilityTest {

    /**
     * The Mocked path.
     */
    Path mockedPath = Paths.get("\\tmp");

    /**
     * Test command utility.
     *
     * @throws Exception the exception
     */
    @Test
    public void testCommandUtility() throws Exception{

        List<String> commands = new ArrayList<>();
        commands.add("# comment");
        commands.add("ps -aef | grep {0}");
        Stream<String> commandStream = commands.stream();
        PowerMockito.mockStatic(Paths.class);
        PowerMockito.mockStatic(Files.class);
        PowerMockito.mockStatic(ClassLoader.class);

        URL url = PowerMockito.mock(URL.class);
        PowerMockito.when(ClassLoader.getSystemResource(Mockito.anyString())).thenReturn(url);

        PowerMockito.when(Paths.get(Mockito.anyObject())).thenReturn(mockedPath);
        PowerMockito.when(Files.lines(Mockito.any(Path.class))).thenReturn(commandStream);

        List<String> processedCommands = CommandUtility.getCommandsFromFile("test.txt", "nginx");
        Assert.assertNotNull(processedCommands);
        Assert.assertTrue(processedCommands.get(0).contains("nginx"));
    }

    /**
     * Test command utility io exception.
     *
     * @throws Exception the exception
     */
    @Test @Ignore
    public void testCommandUtilityIOException() throws Exception{

        PowerMockito.mockStatic(Paths.class);
        PowerMockito.mockStatic(Files.class);
        PowerMockito.mockStatic(ClassLoader.class);

        URL url = PowerMockito.mock(URL.class);
        PowerMockito.when(ClassLoader.getSystemResource(Mockito.anyString())).thenReturn(url);

        PowerMockito.when(Paths.get(Mockito.anyObject())).thenReturn(mockedPath);
        PowerMockito.when(Files.lines(Mockito.any(Path.class))).thenThrow(new IOException("IO Exception!!"));

        List<String> processedCommands = CommandUtility.getCommandsFromFile("test.txt", "nginx");
        Assert.assertNotNull(processedCommands);
        Assert.assertTrue(processedCommands.isEmpty());
    }

}
