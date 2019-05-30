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

import java.util.OptionalInt;
import java.util.concurrent.ThreadLocalRandom;

/**
 * A static class for generating random integers and doubles.
 * <p>
 * Renhao Hu (rhu2)
 */
public class Randomizer {

    private static final int DEFAULT_INT = 0;

    /**
     * Generates a random integer
     *
     * @param low  inclusive
     * @param high exclusive
     * @return random integer if successful; otherwise returns 0
     */
    public static int generateInt(int low, int high) {
        OptionalInt opt = ThreadLocalRandom.current().ints(low, high).findFirst();
        if (opt.isPresent()) {
            return opt.getAsInt();
        } else {
            return DEFAULT_INT;
        }
    }

    /**
     * Generates a random double
     *
     * @param low  inclusive
     * @param high exclusive
     * @return random double if successful
     */
    public static double generateDouble(double low, double high) {
      return ThreadLocalRandom.current().nextDouble(low, high);
    }
}
