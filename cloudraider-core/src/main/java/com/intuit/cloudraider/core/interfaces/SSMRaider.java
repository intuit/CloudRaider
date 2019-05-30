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

package com.intuit.cloudraider.core.interfaces;

import com.intuit.cloudraider.model.Command;

import java.util.List;

/**
 * AWS SSM functionality.
 */
public interface SSMRaider {

    /**
     * Runs the commands to execute on each instance provided.
     *
     * @param instances instances to run commands on
     * @param commands  list of commands to execute
     * @return sent command's id
     */
    public String executeShellCommands(List<String> instances, List<String> commands);

    /**
     * Execute the shell command with the parameters on the given instances.
     *
     * @param instances instances to execute on
     * @param command   command to execute
     * @param params    parameters for command
     * @return command 's id
     */
    public String executeShellCommand(List<String> instances, Command command, String... params);

    /**
     * Execute the shell command with the parameters on the given instances.
     *
     * @param instances instances to execute on
     * @param fileName  file name
     * @param params    parameters for command
     * @return command 's id
     */
    public String executeShellCommandsFromFile(List<String> instances, String fileName, String... params);

    /**
     * Get status of the command execution.
     *
     * @param commandId  command id
     * @param instanceId instance id
     * @return String status
     */
    public String getCommandStatus(String commandId, String instanceId);

    /**
     * Get any output from the command execution.
     *
     * @param commandId  command id
     * @param instanceId instance id
     * @return String output
     */
    public String getCommandStandardOutput(String commandId, String instanceId);

    /**
     * Get any errors from the command execution.
     *
     * @param commandId  command id
     * @param instanceId instance id
     * @return String errors
     */
    public String getCommandErrors(String commandId, String instanceId);
}
