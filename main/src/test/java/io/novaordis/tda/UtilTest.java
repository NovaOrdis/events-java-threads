/*
 * Copyright (c) 2016 Nova Ordis LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.novaordis.tda;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 6/28/16
 */
public class UtilTest {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    // coalesceQuotedStrings() -----------------------------------------------------------------------------------------

    @Test
    public void coalesceQuotedStrings_NoQuotedString() throws Exception {

        String[] args = {

                "on",
                "object",
                "monitor",
                "00.txt"
        };

        String[] result = Util.coalesceQuotedStrings(args);

        assertEquals(4, result.length);
        assertEquals("on", result[0]);
        assertEquals("object", result[1]);
        assertEquals("monitor", result[2]);
        assertEquals("00.txt", result[3]);

        assertEquals(4, args.length);
        assertEquals("on", args[0]);
        assertEquals("object", args[1]);
        assertEquals("monitor", args[2]);
        assertEquals("00.txt", args[3]);
    }


    @Test
    public void coalesceQuotedStrings_QuotedString() throws Exception {

        String[] args = {

                "\"on",
                "object",
                "monitor\"",
                "00.txt"
        };

        String[] result = Util.coalesceQuotedStrings(args);

        assertEquals(2, result.length);
        assertEquals("on object monitor", result[0]);
        assertEquals("00.txt", result[1]);

        assertEquals(4, args.length);
        assertEquals("\"on", args[0]);
        assertEquals("object", args[1]);
        assertEquals("monitor\"", args[2]);
        assertEquals("00.txt", args[3]);
    }

    @Test
    public void coalesceQuotedStrings_TwoQuotedStrings() throws Exception {

        String[] args = {

                "A",
                "\"m",
                "n\"",
                "B",
                "\"p",
                "q\"",
                "C"
        };

        String[] result = Util.coalesceQuotedStrings(args);

        assertEquals(5, result.length);
        assertEquals("A", result[0]);
        assertEquals("m n", result[1]);
        assertEquals("B", result[2]);
        assertEquals("p q", result[3]);
        assertEquals("C", result[4]);

        assertEquals(7, args.length);
        assertEquals("A", args[0]);
        assertEquals("\"m", args[1]);
        assertEquals("n\"", args[2]);
        assertEquals("B", args[3]);
        assertEquals("\"p", args[4]);
        assertEquals("q\"", args[5]);
        assertEquals("C", args[6]);
    }


    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
