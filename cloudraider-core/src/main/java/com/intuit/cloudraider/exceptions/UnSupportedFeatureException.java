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
package com.intuit.cloudraider.exceptions;

public class UnSupportedFeatureException extends RuntimeException
{

    private static final long serialVersionUID = 1L;

    /**
     * Constructs exception without message or cause.
     */
    public UnSupportedFeatureException()
    {
        super();
    }

    /**
     * Construct with a message {@code String} that is returned by the inherited
     * {@code Throwable.getMessage}.
     *
     * @param message
     *            the message that is returned by the inherited
     *            {@code Throwable.getMessage}
     */
    public UnSupportedFeatureException(String message)
    {
        super(message);
    }

    /**
     * Construct with a {@code Throwable} cause that is returned by the
     * inherited {@code Throwable.getCause}. The {@code Throwable.getMessage}
     * will display the output from {@code toString} called on the {@code cause}
     * .
     *
     * @param cause
     *            the cause that is returned by the inherited
     *            {@code Throwable.getCause}
     */
    public UnSupportedFeatureException(Throwable cause)
    {
        super(cause);
    }

    /**
     * Construct with both a {@code String} message and a {@code Throwable}
     * cause. The {@code message} is returned by the inherited
     * {@code Throwable.getMessage}. The cause that is returned by the inherited
     * {@code Throwable.getCause}.
     *
     * @param message
     *            the message that is returned by the inherited
     *            {@code Throwable.getMessage}
     * @param cause
     *            the cause that is returned by the inherited
     *            {@code Throwable.getCause}
     */
    public UnSupportedFeatureException(String message, Throwable cause)
    {
        super(message, cause);
    }

}
