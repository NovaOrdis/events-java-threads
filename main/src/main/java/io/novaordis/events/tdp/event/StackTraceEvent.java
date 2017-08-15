/*
 * Copyright (c) 2017 Nova Ordis LLC
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

package io.novaordis.events.tdp.event;

import io.novaordis.events.api.event.GenericEvent;

import java.util.Arrays;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 8/14/17
 */
public class StackTraceEvent extends GenericEvent {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    //
    // if null, it means the tid could not be extracted from the stack trace
    //
    private Long tid;
    private boolean tidHexRepresentationStartsWith0x;

    //
    // the hex string representation (without leading 0x, if present) length
    //
    private int tidHexRepresentationLength;

    // Constructors ----------------------------------------------------------------------------------------------------

    public StackTraceEvent(Long lineNumber) {

        setLineNumber(lineNumber);
    }

    // Public ----------------------------------------------------------------------------------------------------------

    /**
     * @return the thread ID. May return null, which means the thread ID could not be extracted from the stack trace.
     */
    public Long getTid() {

        return tid;
    }

    /**
     * @return the thread ID in a hexadecimal representation, similar to the one recorded in the stack trace. May
     * return null, which means the thread ID could not be extracted from the stack trace.
     */
    public String getTidAsHexString() {

        if (tid == null) {

            return null;
        }

        String s = Long.toHexString(tid);

        if (s.length() != tidHexRepresentationLength) {

            //
            // pad with leading zeroes
            //

            char[] padding = new char[tidHexRepresentationLength - s.length()];
            Arrays.fill(padding, '0');
            s = new String(padding) + s;
        }

        if (tidHexRepresentationStartsWith0x) {

            s = "0x" + s;
        }

        return s;
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
