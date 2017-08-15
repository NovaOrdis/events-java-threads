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

        String raw = e.getRawRepresentation();
        assertEquals(content, raw);
    }

    /**
     * This test applies to any parsing failure in the header line.
     */
    @Test
    public void parse_InvalidHeaderLine_TheWholeTraceWillBeSkippedButNextOneWillBeCollected_FirstTrace()
            throws Exception {

        String line1 = "\"GC task thread#0 (ParallelGC)\" os_prio=? tid=0x00007f6220025000 nid=0x1829 runnable\n";
        String line2 = "this line does not matter\n";
        String line3 = "\n";
        String line4 = "\"GC task thread#1 (ParallelGC)\" os_prio=0 tid=0x00007f6220026800 nid=0x182a runnable\n";
        String line5 = "\n";

        String content = line1 + line2 + line3 + line4 + line5;

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

        String raw = e.getRawRepresentation();
        assertEquals(line4 + line5, raw);
    }

    /**
     * This test applies to any parsing failure in the header line.
     */
    @Test
    public void parse_InvalidHeaderLine_TheWholeTraceWillBeSkippedButNextOneWillBeCollected_NotFirstTrace()
            throws Exception {

        String line1 = "\"GC task thread#0 (ParallelGC)\" os_prio=0 tid=0x00007f6220025000 nid=0x1829 runnable\n";
        String line2 = "\n";
        String line3 = "\"GC task thread#1 (ParallelGC)\" os_prio=? tid=0x00007f6220026800 nid=0x182a runnable\n";
        String line4 = "\n";
        String line5 = "\"GC task thread#2 (ParallelGC)\" os_prio=0 tid=0x00007f6220028800 nid=0x182b runnable\n";
        String line6 = "\n";

        String content = line1 + line2 + line3 + line4 + line5 + line6;

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
        assertEquals(line1 + line2, e.getRawRepresentation());

        StackTraceEvent e2 = (StackTraceEvent)events.get(1);
        assertEquals("GC task thread#2 (ParallelGC)", e2.getThreadName());
        assertEquals(line5 + line6, e2.getRawRepresentation());
    }

    @Test
    public void parse_InvalidTID_TheWholeTraceWillBeSkippedButNextOneWillBeCollected_FirstTrace() throws Exception {

        String line1 = "\"GC task thread#0 (ParallelGC)\" os_prio=0 tid=something-that-is-not-hex nid=0x1829 runnable\n";
        String line2 = "this line does not matter\n";
        String line3 = "\n";
        String line4 = "\"GC task thread#1 (ParallelGC)\" os_prio=0 tid=0x00007f6220026800 nid=0x182a runnable\n";
        String line5 = "\n";

        String content = line1 + line2 + line3 + line4 + line5;

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
        assertEquals(line4 + line5, e.getRawRepresentation());
    }

    /**
     * This test applies to any parsing failure in the header line.
     */
    @Test
    public void parse_InvalidTID_TheWholeTraceWillBeSkippedButNextOneWillBeCollected_NotFirstTrace() throws Exception {

        String line1 = "\"GC task thread#0 (ParallelGC)\" os_prio=0 tid=0x00007f6220025000 nid=0x1829 runnable\n";
        String line2 = "\n";
        String line3 = "\"GC task thread#1 (ParallelGC)\" os_prio=0 tid=something-that-is-not-hex nid=0x182a runnable\n";
        String line4 = "\n";
        String line5 = "\"GC task thread#2 (ParallelGC)\" os_prio=0 tid=0x00007f6220028800 nid=0x182b runnable\n";
        String line6 = "\n";

        String content = line1 + line2 + line3 + line4 + line5 + line6;

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
        assertEquals(line1 + line2, e.getRawRepresentation());

        StackTraceEvent e2 = (StackTraceEvent)events.get(1);
        assertEquals("GC task thread#2 (ParallelGC)", e2.getThreadName());
        assertEquals(line5 + line6, e2.getRawRepresentation());
    }

    @Test
    public void parse_InvalidThreadState_TheWholeTraceWillBeSkippedButNextOneWillBeCollected_FirstTrace()
            throws Exception {

        String line1 = "\"GC task thread#0 (ParallelGC)\" os_prio=0 tid=0xff nid=0x1829 no-such-thread-state\n";
        String line2 = "this line does not matter\n";
        String line3 = "\n";
        String line4 = "\"GC task thread#1 (ParallelGC)\" os_prio=0 tid=0x00007f6220026800 nid=0x182a runnable\n";
        String line5 = "\n";

        String content = line1 + line2 + line3 + line4 + line5;

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
        assertEquals(line4 + line5, e.getRawRepresentation());
    }

    /**
     * This test applies to any parsing failure in the header line.
     */
    @Test
    public void parse_InvalidThreadState_TheWholeTraceWillBeSkippedButNextOneWillBeCollected_NotFirstTrace()
            throws Exception {

        String line1 = "\"GC task thread#0 (ParallelGC)\" os_prio=0 tid=0xfe nid=0x1829 runnable\n";
        String line2 = "\n";
        String line3 = "\"GC task thread#1 (ParallelGC)\" os_prio=0 tid=0xff nid=0x182a no-such-thread-state\n";
        String line4 = "\n";
        String line5 = "\"GC task thread#2 (ParallelGC)\" os_prio=0 tid=0x00007f6220028800 nid=0x182b runnable\n";
        String line6 = "\n";

        String content = line1 + line2 + line3 + line4 + line5 + line6;

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
        assertEquals(line1 + line2, e.getRawRepresentation());

        StackTraceEvent e2 = (StackTraceEvent)events.get(1);
        assertEquals("GC task thread#2 (ParallelGC)", e2.getThreadName());
        assertEquals(line5 + line6, e2.getRawRepresentation());
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

        String rawContent = e.getRawRepresentation();
        assertEquals(rawContent, content);
    }

    @Test
    public void parse_Production2() throws Exception {

        String content =
                "\"http-0.0.0.0:8443-266\" #2422 daemon prio=5 os_prio=0 tid=0x00007f7dc853b800 nid=0x4799 runnable [0x00007f7d871f0000]\n" +
                        "   java.lang.Thread.State: RUNNABLE\n" +
                        "\tat java.net.SocketInputStream.socketRead0(Native Method)\n" +
                        "\tat java.net.SocketInputStream.socketRead(SocketInputStream.java:116)\n" +
                        "\tat java.net.SocketInputStream.read(SocketInputStream.java:170)\n" +
                        "\tat java.net.SocketInputStream.read(SocketInputStream.java:141)\n" +
                        "\tat sun.security.ssl.InputRecord.readFully(InputRecord.java:465)\n" +
                        "\tat sun.security.ssl.InputRecord.read(InputRecord.java:503)\n" +
                        "\tat sun.security.ssl.SSLSocketImpl.readRecord(SSLSocketImpl.java:973)\n" +
                        "\t- locked <0x00000007857a86c0> (a java.lang.Object)\n" +
                        "\tat sun.security.ssl.SSLSocketImpl.readDataRecord(SSLSocketImpl.java:930)\n" +
                        "\tat sun.security.ssl.AppInputStream.read(AppInputStream.java:105)\n" +
                        "\t- locked <0x00000007857c2a48> (a sun.security.ssl.AppInputStream)\n" +
                        "\tat org.apache.coyote.http11.InternalInputBuffer.fill(InternalInputBuffer.java:713)\n" +
                        "\tat org.apache.coyote.http11.InternalInputBuffer.parseRequestLine(InternalInputBuffer.java:351)\n" +
                        "\tat org.apache.coyote.http11.Http11Processor.process(Http11Processor.java:819)\n" +
                        "\tat org.apache.coyote.http11.Http11Protocol$Http11ConnectionHandler.process(Http11Protocol.java:656)\n" +
                        "\t- locked <0x00000007ba6e1380> (a org.apache.coyote.http11.Http11Processor)\n" +
                        "\tat org.apache.tomcat.util.net.JIoEndpoint$Worker.run(JIoEndpoint.java:926)\n" +
                        "\tat java.lang.Thread.run(Thread.java:745)\n" +
                        "\n" +
                        "   Locked ownable synchronizers:\n" +
                        "\t- None\n";

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

        assertEquals("http-0.0.0.0:8443-266", e.getThreadName());
//        assertTrue(e.isDaemon());
//        assertEquals(10, e.getPrio().intValue());
//        assertEquals(0, e.getOsPrio().intValue());
//        assertEquals("0x00007f62201d2000", e.getTid());
//        assertEquals("0x1832", e.getNid());
//        assertEquals(ThreadState.OBJECT_WAIT, e.getThreadState());
//        assertEquals("0x00007f6209147000", e.getMonitor());
//
//        String rawContent = e.getRawRepresentation();
//        assertEquals(rawContent, content);
//
//        StackTrace t = new StackTrace(1, s);
//
//        assertEquals("http-0.0.0.0:8443-266", t.getName());
//        assertEquals("0x00007f7dc853b800", t.getTidAsHexString());
//        assertEquals(s, t.getOriginal());
//        assertFalse(t.isEmpty());
//        assertTrue(t.isValid());

    }

//    @Test
//    public void testValidDefinition() throws Exception {
//
//        String s =
//                "\"http-192.168.30.11-8080-2035\" daemon prio=10 tid=0x000000005078a800 nid=0x4218 waiting for monitor entry [0x00002aab56862000]\n" +
//                        "   java.lang.Thread.State: BLOCKED (on object monitor)\n" +
//                        "\tat java.lang.Throwable.printStackTrace(Throwable.java:460)";
//
//        StackTrace td = new StackTrace();
//
//        String line;
//        BufferedReader br = new BufferedReader(new StringReader(s));
//        while((line = br.readLine()) != null)
//        {
//            td.append(line, -1);
//        }
//
//        assertTrue(td.isValid());
//        assertEquals("http-192.168.30.11-8080-2035", td.getName());
//    }
//
//    @Test
//    public void testValidDefinition_QuotesDoNotClose() throws Exception {
//
//        String s =
//                "\"http-192.168.30.11-8080-2035\n" +
//                        "   java.lang.Thread.State: BLOCKED (on object monitor)\n" +
//                        "\tat java.lang.Throwable.printStackTrace(Throwable.java:460)";
//
//        StackTrace td = new StackTrace();
//
//        String line;
//        BufferedReader br = new BufferedReader(new StringReader(s));
//        while((line = br.readLine()) != null)
//        {
//            td.append(line, -1);
//        }
//
//        assertFalse(td.isValid());
//        assertNull(td.getName());
//    }
//
//
//    // match() ---------------------------------------------------------------------------------------------------------
//
//    @Test
//    public void testMatch() throws Exception {
//
//        StackTrace td = new StackTrace();
//
//        td.append("\"ajp-10.7.25.129-8009-587\" daemon prio=3 tid=0x000000010be0c000 nid=0x34d runnable [0xfffffffe1b07e000]", -1);
//        td.append("   java.lang.Thread.State: RUNNABLE", -1);
//        td.append("\tat java.net.SocketInputStream.socketRead0(Native Method)", -1);
//        td.append("\tat java.net.SocketInputStream.read(SocketInputStream.java:129)", -1);
//        td.append("\tat java.net.ManagedSocketInputStreamHighPerformance.read(ManagedSocketInputStreamHighPerformance.java:258)", -1);
//        td.append("\tat org.apache.coyote.ajp.AjpProcessor.read(AjpProcessor.java:1036)", -1);
//        td.append("\tat org.apache.coyote.ajp.AjpProcessor.readMessage(AjpProcessor.java:1115)", -1);
//        td.append("\tat org.apache.coyote.ajp.AjpProcessor.process(AjpProcessor.java:383)", -1);
//        td.append("\tat org.apache.coyote.ajp.AjpProtocol$AjpConnectionHandler.process(AjpProtocol.java:384)", -1);
//        td.append("\tat org.apache.tomcat.util.net.JIoEndpoint$Worker.run(JIoEndpoint.java:451)", -1);
//        td.append("\tat java.lang.Thread.run(Thread.java:619)", -1);
//
//        assertFalse(td.matches("blah"));
//
//        assertTrue(td.matches("ajp-10."));
//    }


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

        StackTraceParser.processStackTraceHeader(
                7L, e, "something", "0xff", " os_prio=0", " nid=0x1829 runnable", "mock raw header");

        assertEquals("something", e.getThreadName());
        assertEquals("0xff", e.getTid());
        assertEquals(0, e.getOsPrio().intValue());
        assertEquals("0x1829", e.getNid());
        assertEquals(ThreadState.RUNNABLE, e.getThreadState());
    }

    @Test
    public void processStackTraceHeader2() throws Exception {

        StackTraceEvent e = new StackTraceEvent(7L);

        StackTraceParser.processStackTraceHeader(
                7L, e, "something", "0xff",
                "  #2 daemon prio=10 os_prio=1",
                " nid=0x1832 in Object.wait() [0x00007f6209147000]", "mock raw header");

        assertEquals("something", e.getThreadName());
        assertEquals("0xff", e.getTid());
        assertEquals(1, e.getOsPrio().intValue());
        assertEquals(10, e.getPrio().intValue());
        assertTrue(e.isDaemon());
        assertEquals("0x1832", e.getNid());

        ThreadState ts = e.getThreadState();
        assertEquals(ThreadState.OBJECT_WAIT, ts);
        assertEquals("0x00007f6209147000", e.getMonitor());
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
