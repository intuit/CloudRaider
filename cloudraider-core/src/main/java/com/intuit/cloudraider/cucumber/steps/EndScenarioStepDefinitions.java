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

package com.intuit.cloudraider.cucumber.steps;

import org.springframework.beans.factory.annotation.Autowired;

import com.intuit.cloudraider.cucumber.model.ExecutionStateCache;

import cucumber.api.java.en.Then;

/**
 * The type End scenario step definitions.
 */
public class EndScenarioStepDefinitions {
    /**
     * Gets execution state cache.
     *
     * @return the execution state cache
     */
    public ExecutionStateCache getExecutionStateCache() {
        return executionStateCache;
    }

    /**
     * Sets execution state cache.
     *
     * @param executionStateCache the execution state cache
     */
    public void setExecutionStateCache(ExecutionStateCache executionStateCache) {
        this.executionStateCache = executionStateCache;
    }

    @Autowired
    private ExecutionStateCache executionStateCache;

    /**
     * Instantiates a new End scenario step definitions.
     */
    public EndScenarioStepDefinitions()
    {
    }

    /**
     * End scenario.
     */
    @Then("^end scenario$")
    public void endScenario()
    {
    	executionStateCache = new ExecutionStateCache();

    	System.out.println("---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
    }
    
}
