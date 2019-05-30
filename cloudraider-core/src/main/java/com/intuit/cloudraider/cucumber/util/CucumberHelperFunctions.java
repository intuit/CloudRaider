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

package com.intuit.cloudraider.cucumber.util;

import com.amazonaws.services.ec2.model.Tag;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper function class for any Cucumber Step Definitions
 */
public class CucumberHelperFunctions {

    /**
     * Given a string consisting of tags, parse the string and return a list of Tags.
     *
     * @param tagString tags string in the form of t1:v1,t2:v2 .......
     * @return list of Tags
     */
    public static List<Tag> tagStringToList(String tagString) {
        List<Tag> tags = new ArrayList<>();

        if (tagString == null || tagString.equals("")) {
            return tags;
        }

        String[] tagKeyValuePairs = tagString.split(",\\s*");

        for (String tagKeyValuePair : tagKeyValuePairs) {
            String[] tagKeyValue = tagKeyValuePair.split(":");
            Tag tag = new Tag();

            tag.setKey(tagKeyValue[0]);
            tag.setValue(tagKeyValue[1]);
            tags.add(tag);
        }

        return tags;
    }

    /**
     * Checks if the instance contains all the compulsory tags
     *
     * @param compulsoryTags compulsory tags
     * @param instanceTags instance's tags
     * @return true if the instance contains all the compulsory tags; false otherwise
     */
    public static boolean containsAllCompulsoryTags(List<Tag> compulsoryTags, List<Tag> instanceTags) {
        boolean flag = true;

        for (Tag compulsoryTag : compulsoryTags) {
            boolean instanceContainsTag = false;
            for (Tag instanceTag : instanceTags) {
                if (instanceTag.equals(compulsoryTag)) {
                    instanceContainsTag = true;
                    break;
                }
            }
            if (!instanceContainsTag) {
                flag = false;
                break;
            }
        }
        return flag;
    }

}
