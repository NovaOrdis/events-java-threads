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
import io.novaordis.events.api.event.IntegerProperty;
import io.novaordis.events.api.event.StringProperty;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 8/14/17
 */
public class StackTraceEvent extends GenericEvent {

    // Constants -------------------------------------------------------------------------------------------------------

    public static final String THREAD_NAME_PROPERTY_NAME = "thread-name";
    public static final String OS_PRIO_PROPERTY_NAME = "os-prio";

    // the TID is maintained internally as a hexadecimal string
    public static final String TID_PROPERTY_NAME = "tid";

    // the NID is maintained internally as a hexadecimal string
    public static final String NID_PROPERTY_NAME = "nid";

    public static final String THREAD_STATE_PROPERTY_NAME = "thread-state";

    // Static ----------------------------------------------------------------------------------------------------------

    /**
     * @throws NumberFormatException if the hexadecimal string cannot be converted to a valid long.
     */
    public static long longFromHexString(String s) throws NumberFormatException {

        if (s.startsWith("0x") || s.startsWith("0X")) {

            s = s.substring(2);
        }

        return Long.parseUnsignedLong(s, 16);
    }

    /**
     * @throws NumberFormatException if the hexadecimal string cannot be converted to a valid int.
     */
    public static int intFromHexString(String s) throws NumberFormatException {

        if (s.startsWith("0x") || s.startsWith("0X")) {

            s = s.substring(2);
        }

        return Integer.parseUnsignedInt(s, 16);
    }

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    public StackTraceEvent(Long lineNumber) {

        setLineNumber(lineNumber);
    }

    // Public ----------------------------------------------------------------------------------------------------------

    public String getThreadName() {

        StringProperty p =  getStringProperty(THREAD_NAME_PROPERTY_NAME);

        if (p == null) {

            return null;
        }

        return p.getString();
    }

    public void setThreadName(String s) {

        setStringProperty(THREAD_NAME_PROPERTY_NAME, s);
    }

    public Integer getOsPrio() {

        IntegerProperty p =  getIntegerProperty(OS_PRIO_PROPERTY_NAME);

        if (p == null) {

            return null;
        }

        return p.getInteger();
    }

    public void setOsPrio(int i) {

        setIntegerProperty(OS_PRIO_PROPERTY_NAME, i);
    }

    /**
     * @throws NumberFormatException if the hexadecimal string cannot be converted to a valid long.
     */
    public void setTid(String hexadecimalString) throws NumberFormatException {

        //
        // sanity check - if it does not throw exception, we're good
        //
        longFromHexString(hexadecimalString);

        setStringProperty(TID_PROPERTY_NAME, hexadecimalString);
    }

    /**
     * @return the thread ID. May return null, which means the thread ID could not be extracted from the stack trace.
     */
    public Long getTidAsLong() {

        String s = getTid();

        if (s == null) {

            return null;
        }

        return longFromHexString(s);
    }

    /**
     * @return the thread ID in a hexadecimal representation, similar to the one recorded in the stack trace. May
     * return null, which means the thread ID could not be extracted from the stack trace.
     */
    public String getTid() {

        StringProperty p = getStringProperty(TID_PROPERTY_NAME);

        if (p == null) {

            return null;
        }

        return p.getString();
    }

    /**
     * @throws NumberFormatException if the hexadecimal string cannot be converted to a valid int.
     */
    public void setNid(String hexadecimalString) throws NumberFormatException {

        //
        // sanity check - if it does not throw exception, we're good
        //
        intFromHexString(hexadecimalString);

        setStringProperty(NID_PROPERTY_NAME, hexadecimalString);
    }

    /**
     * @return the NID. May return null, which means the NID could not be extracted from the stack trace.
     */
    public Integer getNidAsInt() {

        String s = getNid();

        if (s == null) {

            return null;
        }

        return intFromHexString(s);
    }

    /**
     * @return the NID in a hexadecimal representation, similar to the one recorded in the stack trace. May return null,
     * which means the NID could not be extracted from the stack trace.
     */
    public String getNid() {

        StringProperty p = getStringProperty(NID_PROPERTY_NAME);

        if (p == null) {

            return null;
        }

        return p.getString();
    }

    /**
     * @throws IllegalArgumentException if the thread state string cannot be converted to a known ThreadState
     */
    public void setThreadState(String s) throws IllegalArgumentException {

        ThreadState ts = ThreadState.fromString(s);

        if (ts == null) {

            throw new IllegalArgumentException("unknown thread state: " + s);
        }
        else {

            setStringProperty(THREAD_STATE_PROPERTY_NAME, ts.toString());
        }
    }

    /**
     * @throws IllegalStateException in case the thread state stored by the event is invalid.
     */
    public ThreadState getThreadState() throws IllegalStateException {

        StringProperty p = getStringProperty(THREAD_STATE_PROPERTY_NAME);

        if (p == null) {

            return null;
        }

        String s = p.getString();

        if (s == null) {

            return null;
        }

        ThreadState ts = ThreadState.fromString(s);

        if (ts == null) {

            throw new IllegalStateException("invalid stored thread state: " + s);
        }

        return ts;
    }

    @Override
    public String toString() {

        return "StackTraceEvent[" + getThreadName() + ", line " + getLineNumber() + "]";
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
