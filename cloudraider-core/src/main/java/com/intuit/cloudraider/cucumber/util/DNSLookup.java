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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xbill.DNS.Address;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class DNSLookup {

    /**
     * The Logger.
     */
    static Logger logger = LoggerFactory.getLogger(DNSLookup.class);

    /**
     * Finds the host name given an endpoint.
     *
     * @param endpoint endpoint to search on
     * @return hostname if found
     * @throws UnknownHostException if host name is not found
     */
    public static String hostNameLookup(String endpoint) throws UnknownHostException {
        InetAddress addr = Address.getByName(endpoint);
        logger.debug("DNSLookUp for " + endpoint + " resolved into :" + Address.getHostName(addr));
        return Address.getHostName(addr);
    }
}
