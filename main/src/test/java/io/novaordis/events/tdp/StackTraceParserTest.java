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

package io.novaordis.events.tdp;

import io.novaordis.events.api.event.EndOfStreamEvent;
import io.novaordis.events.api.event.Event;
import io.novaordis.events.tdp.event.StackTraceEvent;
import io.novaordis.events.tdp.event.ThreadState;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 8/14/17
 */
public class StackTraceParserTest {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    // Tests -----------------------------------------------------------------------------------------------------------

    // parse() ---------------------------------------------------------------------------------------------------------

    @Test
    public void parse_simplestSyntheticStackTrace() throws Exception {

        String content =
                "\"GC task thread#0 (ParallelGC)\" os_prio=0 tid=0x00007f6220025000 nid=0x1829 runnable\n" +
                        "\n";

        StackTraceParser p = new StackTraceParser();

        BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(content.getBytes())));

        String line;

        List<Event> events = new ArrayList<>();

        long lineNumber = 1;

        for(; (line = br.readLine()) != null; lineNumber ++) {

            List<Event> es = p.parse(lineNumber, line);
            events.addAll(es);
        }

        List<Event> es = p.close(lineNumber);
        events.addAll(es);

        br.close();

        assertEquals(1, events.size());

        StackTraceEvent e = (StackTraceEvent)events.get(0);

        assertEquals("GC task thread#0 (ParallelGC)", e.getThreadName());
        assertEquals(0, e.getOsPrio().intValue());
        assertEquals(StackTraceEvent.longFromHexString("0x00007f6220025000"), e.getTidAsLong().longValue());
        assertEquals("0x00007f6220025000", e.getTid());
        assertEquals("0x1829", e.getNid());
        assertEquals(ThreadState.RUNNABLE, e.getThreadState());
    }

    /**
     * This test applies to any parsing failure in the header line.
     */
    @Test
    public void parse_InvalidHeaderLine_TheWholeTraceWillBeSkippedButNextOneWillBeCollected_FirstTrace()
            throws Exception {

        String content =
                "\"GC task thread#0 (ParallelGC)\" os_prio=? tid=0x00007f6220025000 nid=0x1829 runnable\n" +
                "this line does not matter\n" +
                "\n" +
                "\"GC task thread#1 (ParallelGC)\" os_prio=0 tid=0x00007f6220026800 nid=0x182a runnable\n" +
                "\n";

        StackTraceParser p = new StackTraceParser();

        BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(content.getBytes())));

        String line;

        List<Event> events = new ArrayList<>();

        long lineNumber = 1;

        for(; (line = br.readLine()) != null; lineNumber ++) {

            List<Event> es = p.parse(lineNumber, line);
            events.addAll(es);
        }

        List<Event> es = p.flush();
        events.addAll(es);

        br.close();

        assertEquals(1, events.size());

        StackTraceEvent e = (StackTraceEvent)events.get(0);

        assertEquals("GC task thread#1 (ParallelGC)", e.getThreadName());
    }

    /**
     * This test applies to any parsing failure in the header line.
     */
    @Test
    public void parse_InvalidHeaderLine_TheWholeTraceWillBeSkippedButNextOneWillBeCollected_NotFirstTrace()
            throws Exception {

        String content =
                "\"GC task thread#0 (ParallelGC)\" os_prio=0 tid=0x00007f6220025000 nid=0x1829 runnable\n" +
                        "\n" +
                        "\"GC task thread#1 (ParallelGC)\" os_prio=? tid=0x00007f6220026800 nid=0x182a runnable\n" +
                        "\n" +
                        "\"GC task thread#2 (ParallelGC)\" os_prio=0 tid=0x00007f6220028800 nid=0x182b runnable\n" +
                        "\n";

        StackTraceParser p = new StackTraceParser();

        BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(content.getBytes())));

        String line;

        List<Event> events = new ArrayList<>();

        long lineNumber = 1;

        for(; (line = br.readLine()) != null; lineNumber ++) {

            List<Event> es = p.parse(lineNumber, line);
            events.addAll(es);
        }

        List<Event> es = p.flush();
        events.addAll(es);

        br.close();

        assertEquals(2, events.size());

        StackTraceEvent e = (StackTraceEvent)events.get(0);

        assertEquals("GC task thread#0 (ParallelGC)", e.getThreadName());

        StackTraceEvent e2 = (StackTraceEvent)events.get(1);

        assertEquals("GC task thread#2 (ParallelGC)", e2.getThreadName());
    }

    @Test
    public void parse_InvalidTID_TheWholeTraceWillBeSkippedButNextOneWillBeCollected_FirstTrace() throws Exception {

        String content =
                "\"GC task thread#0 (ParallelGC)\" os_prio=0 tid=something-that-is-not-hex nid=0x1829 runnable\n" +
                        "this line does not matter\n" +
                        "\n" +
                        "\"GC task thread#1 (ParallelGC)\" os_prio=0 tid=0x00007f6220026800 nid=0x182a runnable\n" +
                        "\n";

        StackTraceParser p = new StackTraceParser();

        BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(content.getBytes())));

        String line;

        List<Event> events = new ArrayList<>();

        long lineNumber = 1;

        for(; (line = br.readLine()) != null; lineNumber ++) {

            List<Event> es = p.parse(lineNumber, line);
            events.addAll(es);
        }

        List<Event> es = p.flush();
        events.addAll(es);

        br.close();

        assertEquals(1, events.size());

        StackTraceEvent e = (StackTraceEvent)events.get(0);

        assertEquals("GC task thread#1 (ParallelGC)", e.getThreadName());
    }

    /**
     * This test applies to any parsing failure in the header line.
     */
    @Test
    public void parse_InvalidTID_TheWholeTraceWillBeSkippedButNextOneWillBeCollected_NotFirstTrace() throws Exception {

        String content =
                "\"GC task thread#0 (ParallelGC)\" os_prio=0 tid=0x00007f6220025000 nid=0x1829 runnable\n" +
                        "\n" +
                        "\"GC task thread#1 (ParallelGC)\" os_prio=0 tid=something-that-is-not-hex nid=0x182a runnable\n" +
                        "\n" +
                        "\"GC task thread#2 (ParallelGC)\" os_prio=0 tid=0x00007f6220028800 nid=0x182b runnable\n" +
                        "\n";

        StackTraceParser p = new StackTraceParser();

        BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(content.getBytes())));

        String line;

        List<Event> events = new ArrayList<>();

        long lineNumber = 1;

        for(; (line = br.readLine()) != null; lineNumber ++) {

            List<Event> es = p.parse(lineNumber, line);
            events.addAll(es);
        }

        List<Event> es = p.flush();
        events.addAll(es);

        br.close();

        assertEquals(2, events.size());

        StackTraceEvent e = (StackTraceEvent)events.get(0);

        assertEquals("GC task thread#0 (ParallelGC)", e.getThreadName());

        StackTraceEvent e2 = (StackTraceEvent)events.get(1);

        assertEquals("GC task thread#2 (ParallelGC)", e2.getThreadName());
    }

    @Test
    public void parse_InvalidThreadState_TheWholeTraceWillBeSkippedButNextOneWillBeCollected_FirstTrace()
            throws Exception {

        String content =
                "\"GC task thread#0 (ParallelGC)\" os_prio=0 tid=0xff nid=0x1829 no-such-thread-state\n" +
                        "this line does not matter\n" +
                        "\n" +
                        "\"GC task thread#1 (ParallelGC)\" os_prio=0 tid=0x00007f6220026800 nid=0x182a runnable\n" +
                        "\n";

        StackTraceParser p = new StackTraceParser();

        BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(content.getBytes())));

        String line;

        List<Event> events = new ArrayList<>();

        long lineNumber = 1;

        for(; (line = br.readLine()) != null; lineNumber ++) {

            List<Event> es = p.parse(lineNumber, line);
            events.addAll(es);
        }

        List<Event> es = p.flush();
        events.addAll(es);

        br.close();

        assertEquals(1, events.size());

        StackTraceEvent e = (StackTraceEvent)events.get(0);

        assertEquals("GC task thread#1 (ParallelGC)", e.getThreadName());
    }

    /**
     * This test applies to any parsing failure in the header line.
     */
    @Test
    public void parse_InvalidThreadState_TheWholeTraceWillBeSkippedButNextOneWillBeCollected_NotFirstTrace()
            throws Exception {

        String content =
                "\"GC task thread#0 (ParallelGC)\" os_prio=0 tid=0xfe nid=0x1829 runnable\n" +
                        "\n" +
                        "\"GC task thread#1 (ParallelGC)\" os_prio=0 tid=0xff nid=0x182a no-such-thread-state\n" +
                        "\n" +
                        "\"GC task thread#2 (ParallelGC)\" os_prio=0 tid=0x00007f6220028800 nid=0x182b runnable\n" +
                        "\n";

        StackTraceParser p = new StackTraceParser();

        BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(content.getBytes())));

        String line;

        List<Event> events = new ArrayList<>();

        long lineNumber = 1;

        for(; (line = br.readLine()) != null; lineNumber ++) {

            List<Event> es = p.parse(lineNumber, line);
            events.addAll(es);
        }

        List<Event> es = p.flush();
        events.addAll(es);

        br.close();

        assertEquals(2, events.size());

        StackTraceEvent e = (StackTraceEvent)events.get(0);

        assertEquals("GC task thread#0 (ParallelGC)", e.getThreadName());

        StackTraceEvent e2 = (StackTraceEvent)events.get(1);

        assertEquals("GC task thread#2 (ParallelGC)", e2.getThreadName());
    }

    @Test
    public void parse_Production1() throws Exception {

        String content =
                "\"Reference Handler\" #2 daemon prio=10 os_prio=0 tid=0x00007f62201d2000 nid=0x1832 in Object.wait() [0x00007f6209147000]\n" +
                        "   java.lang.Thread.State: WAITING (on object monitor)\n" +
                        "\tat java.lang.Object.wait(Native Method)\n" +
                        "\tat java.lang.Object.wait(Object.java:502)\n" +
                        "\tat java.lang.ref.Reference$ReferenceHandler.run(Reference.java:157)\n" +
                        "\t- locked <0x00000005cc36c560> (a java.lang.ref.Reference$Lock)\n" +
                        "\n" +
                        "   Locked ownable synchronizers:\n" +
                        "\t- None\n" +
                        "\n";

        StackTraceParser p = new StackTraceParser();

        BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(content.getBytes())));

        String line;

        List<Event> events = new ArrayList<>();

        long lineNumber = 1;

        for(; (line = br.readLine()) != null; lineNumber ++) {

            List<Event> es = p.parse(lineNumber, line);
            events.addAll(es);
        }

        List<Event> es = p.flush();
        events.addAll(es);

        br.close();

        assertEquals(1, events.size());

        StackTraceEvent e = (StackTraceEvent)events.get(0);

        assertEquals("Reference Handler", e.getThreadName());
        assertTrue(e.isDaemon());
        assertEquals(10, e.getPrio().intValue());
        assertEquals(0, e.getOsPrio().intValue());
        assertEquals("0x00007f62201d2000", e.getTid());
        assertEquals("0x1832", e.getNid());
        assertEquals(ThreadState.OBJECT_WAIT, e.getThreadState());
        assertEquals("0x00007f6209147000", e.getMonitor());
    }

    // close() ---------------------------------------------------------------------------------------------------------

    @Test
    public void close_Empty() throws Exception {

        StackTraceParser p = new StackTraceParser();

        List<Event> events = p.close();

        assertEquals(1, events.size());
        assertTrue(events.get(0) instanceof EndOfStreamEvent);

        //
        // noop
        //

        List<Event> events2 = p.close();
        assertTrue(events2.isEmpty());
    }

    // processStackTraceHeader() ---------------------------------------------------------------------------------------

    @Test
    public void processStackTraceHeader() throws Exception {

        StackTraceEvent e = new StackTraceEvent(7L);

        StackTraceParser.processStackTraceHeader(e, "something", "0xff", " os_prio=0", " nid=0x1829 runnable");

        assertEquals("something", e.getThreadName());
        assertEquals("0xff", e.getTid());
        assertEquals(0, e.getOsPrio().intValue());
        assertEquals("0x1829", e.getNid());
        assertEquals(ThreadState.RUNNABLE, e.getThreadState());
    }

    @Test
    public void processStackTraceHeader2() throws Exception {

        StackTraceEvent e = new StackTraceEvent(7L);

        StackTraceParser.processStackTraceHeader(e, "something", "0xff",
                "  #2 daemon prio=10 os_prio=1",
                " nid=0x1832 in Object.wait() [0x00007f6209147000]");

        assertEquals("something", e.getThreadName());
        assertEquals("0xff", e.getTid());
        assertEquals(1, e.getOsPrio().intValue());
        assertEquals(10, e.getPrio().intValue());
        assertTrue(e.isDaemon());
        assertEquals("0x1832", e.getNid());

        ThreadState ts = e.getThreadState();
        //assertEquals(ThreadState.OBJECT_WAIT, ts.);
    }


    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
