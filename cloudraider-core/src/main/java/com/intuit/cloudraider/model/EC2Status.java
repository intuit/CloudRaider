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

package com.intuit.cloudraider.model;

import com.fasterxml.jackson.annotation.JsonView;
import com.intuit.cloudraider.utils.View;

import java.util.Map;

/**
 * Object for storing EC2 statuses and tag name.
 * <p>
  */
public class EC2Status {

    @JsonView(View.Normal.class)
    private String tagName;

    @JsonView(View.Normal.class)
    private Map<String, String> status;

    /**
     * Gets tag name.
     *
     * @return the tag name
     */
    public String getTagName() {
        return tagName;
    }

    /**
     * Sets tag name.
     *
     * @param tagName the tag name
     */
    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    /**
     * Gets status.
     *
     * @return the status
     */
    public Map getStatus() {
        return status;
    }

    /**
     * Sets status.
     *
     * @param status the status
     */
    public void setStatus(Map<String, String> status) {
        this.status = status;
    }
}
