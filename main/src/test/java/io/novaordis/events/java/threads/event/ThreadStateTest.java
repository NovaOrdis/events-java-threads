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

package io.novaordis.events.java.threads.event;

import io.novaordis.events.java.threads.event.StackTraceEvent;
import io.novaordis.events.java.threads.event.ThreadState;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 8/15/17
 */
public class ThreadStateTest {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    // Tests -----------------------------------------------------------------------------------------------------------

    // fromString() ----------------------------------------------------------------------------------------------------

    @Test
    public void fromString_NoSuchValue() throws Exception {

        assertNull(ThreadState.fromString("something"));
    }

    @Test
    public void fromString_AllEnumElementsCanBeReconstitutedFromTheirToString() throws Exception {

        for(ThreadState ts: ThreadState.values()) {

            String s = ts.toString();
            assertEquals(ts, ThreadState.fromString(s));
        }
    }

    @Test
    public void fromString_Runnable() throws Exception {

        assertEquals(ThreadState.RUNNABLE, ThreadState.fromString("runnable"));
    }

    @Test
    public void fromString_InObjectWait() throws Exception {

        assertEquals(ThreadState.OBJECT_WAIT, ThreadState.fromString(" in Object.wait() [0x00007f6209147000]"));
    }

    @Test
    public void fromString_WaitingOnCondition() throws Exception {

        assertEquals(ThreadState.WAITING_ON_CONDITION, ThreadState.fromString(" waiting on condition [0x00007f0109264000]"));
    }

    @Test
    public void fromString_Sleeping() throws Exception {

        assertEquals(ThreadState.SLEEPING, ThreadState.fromString(" sleeping[0x00007f013a8e8000]"));
    }

    @Test
    public void fromString_WaitingForMonitorEntry() throws Exception {

        assertEquals(ThreadState.WAITING_FOR_MONITOR_ENTRY,
                ThreadState.fromString(" waiting for monitor entry [0x00007f013b5f4000]"));
    }

    // setMonitor() ---------------------------------------------------------------------------------------------------

    @Test
    public void setMonitor_ObjectWait() throws Exception {

        StackTraceEvent e = new StackTraceEvent(7L);

        assertNull(e.getMonitor());

        ThreadState.setMonitor(e, " in Object.wait() [0x00007f6209147000]");

        assertEquals("0x00007f6209147000", e.getMonitor());
    }

    @Test
    public void setMonitor_WaitingOnCondition() throws Exception {

        StackTraceEvent e = new StackTraceEvent(7L);

        assertNull(e.getMonitor());

        ThreadState.setMonitor(e, " waiting on condition [0x00007f0109264000]");

        assertEquals("0x00007f0109264000", e.getMonitor());
    }

    @Test
    public void setMonitor_Sleeping() throws Exception {

        StackTraceEvent e = new StackTraceEvent(7L);

        assertNull(e.getMonitor());

        ThreadState.setMonitor(e, " sleeping[0x00007f013a8e8000]");

        assertEquals("0x00007f013a8e8000", e.getMonitor());
    }

    @Test
    public void setMonitor_WaitingForMonitorEntry() throws Exception {

        StackTraceEvent e = new StackTraceEvent(7L);

        assertNull(e.getMonitor());

        ThreadState.setMonitor(e, " waiting for monitor entry [0x00007f013b5f4000]");

        assertEquals("0x00007f013b5f4000", e.getMonitor());
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
