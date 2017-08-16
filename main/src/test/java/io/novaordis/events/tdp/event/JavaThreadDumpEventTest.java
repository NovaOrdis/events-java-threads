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

import io.novaordis.events.api.event.EndOfStreamEvent;
import io.novaordis.events.api.event.Event;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 8/14/17
 */
public class JavaThreadDumpEventTest {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    // Tests -----------------------------------------------------------------------------------------------------------

    // addStackTraces() ------------------------------------------------------------------------------------------------

    @Test
    public void addStackTraces_Null() throws Exception {

        JavaThreadDumpEvent tde = new JavaThreadDumpEvent(7L, 1L);

        try {

            tde.addStackTraces(null);
            fail("should have thrown exception");
        }
        catch(IllegalArgumentException e) {

            String msg = e.getMessage();
            assertTrue(msg.contains("null stack trace list"));
        }
    }

    @Test
    public void addStackTraces_NotAStackTraceEvent() throws Exception {

        JavaThreadDumpEvent tde = new JavaThreadDumpEvent(7L, 1L);

        List<Event> events = new ArrayList<>();

        events.add(new StackTraceEvent(17L));
        events.add(new EndOfStreamEvent());

        try {

            tde.addStackTraces(events);
            fail("should have thrown exception");
        }
        catch(IllegalArgumentException e) {

            String msg = e.getMessage();
            assertTrue(msg.contains("not a StackTraceEvent"));
        }
    }

    // addStackTrace() -------------------------------------------------------------------------------------------------

    @Test
    public void addStackTrace() throws Exception {

        JavaThreadDumpEvent tde = new JavaThreadDumpEvent(7L, 1L);

        StackTraceEvent t = new StackTraceEvent(17L);
        tde.addStackTrace(t);

        List<StackTraceEvent> traces = tde.getStackTraceEvents();

        assertEquals(1, traces.size());
        StackTraceEvent t2 = traces.get(0);
        assertEquals(17L, t2.getLineNumber().longValue());

        //
        // add preserves order
        //

        StackTraceEvent t3 = new StackTraceEvent(21L);
        tde.addStackTrace(t3);

        List<StackTraceEvent> traces2 = tde.getStackTraceEvents();

        assertEquals(2, traces2.size());
        StackTraceEvent t4 = traces2.get(0);
        assertEquals(17L, t4.getLineNumber().longValue());
        StackTraceEvent t5 = traces2.get(1);
        assertEquals(21L, t5.getLineNumber().longValue());
    }

    // getStackTraceEvent() --------------------------------------------------------------------------------------------

    @Test
    public void getStackTraceEvent_ObviouslyIncorrectIndex() throws Exception {

        JavaThreadDumpEvent tde = new JavaThreadDumpEvent(7L, 1L);

        try {

            tde.getStackTraceEvent(-1);
            fail("there cannot be a -1 index, this invocation must throw exception");
        }
        catch(IllegalArgumentException e) {

            String msg = e.getMessage();
            assertTrue(msg.contains("invalid index"));
        }
    }

    @Test
    public void getStackTraceEvent() throws Exception {

        JavaThreadDumpEvent tde = new JavaThreadDumpEvent(7L, 1L);

        StackTraceEvent t = new StackTraceEvent(17L);
        tde.addStackTrace(t);

        assertEquals(t, tde.getStackTraceEvent(0));

        StackTraceEvent t2 = new StackTraceEvent(19L);
        tde.addStackTrace(t2);

        assertEquals(t, tde.getStackTraceEvent(0));
        assertEquals(t2, tde.getStackTraceEvent(1));

        assertNull(tde.getStackTraceEvent(2));
        assertNull(tde.getStackTraceEvent(3));
    }

    // getThreadCount() ------------------------------------------------------------------------------------------------

    @Test
    public void getThreadCount() throws Exception {

        JavaThreadDumpEvent tde = new JavaThreadDumpEvent(7L, 1L);

        assertEquals(0, tde.getThreadCount());

        StackTraceEvent t = new StackTraceEvent(17L);
        tde.addStackTrace(t);

        assertEquals(1, tde.getThreadCount());

        StackTraceEvent t2 = new StackTraceEvent(19L);
        tde.addStackTrace(t2);

        assertEquals(2, tde.getThreadCount());
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
