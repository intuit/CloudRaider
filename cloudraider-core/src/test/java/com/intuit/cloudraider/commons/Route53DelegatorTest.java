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

package com.intuit.cloudraider.commons;

import com.intuit.cloudraider.model.BasicCredentials;
import com.intuit.cloudraider.model.Credentials;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * The type Route 53 delegator test.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration

public class Route53DelegatorTest {

    @Autowired
    private  Route53Delegator route53Delegator;


    /**
     * Test r 53 delegator.
     */
    @Test
    public void testR53Delegator(){
        Assert.assertNotNull(route53Delegator.getAmazonRoute53());
    }

    /**
     * The type R 53 delegator test context configuration.
     */
    @Configuration
    protected static class R53DelegatorTestContextConfiguration {

        /**
         * Route 53 delegator route 53 delegator.
         *
         * @return the route 53 delegator
         */
        @Bean
        public Route53Delegator route53Delegator() {
            return new Route53Delegator();
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
