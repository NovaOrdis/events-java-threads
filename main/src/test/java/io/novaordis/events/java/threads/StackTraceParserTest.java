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

package io.novaordis.events.java.threads;

import io.novaordis.events.api.event.EndOfStreamEvent;
import io.novaordis.events.api.event.Event;
import io.novaordis.events.java.threads.StackTraceParser;
import io.novaordis.events.java.threads.event.StackTraceEvent;
import io.novaordis.events.java.threads.event.ThreadState;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
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
    public void testValidDefinition() throws Exception {

        String content =
                "\"http-192.168.30.11-8080-2035\" daemon prio=10 tid=0x000000005078a800 nid=0x4218 waiting for monitor entry [0x00002aab56862000]\n" +
                        "   java.lang.Thread.State: BLOCKED (on object monitor)\n" +
                        "\tat java.lang.Throwable.printStackTrace(Throwable.java:460)";

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

        assertEquals("http-192.168.30.11-8080-2035", e.getThreadName());
    }

    @Test
    public void testValidDefinition_QuotesDoNotClose() throws Exception {

        String content =
                "\"http-192.168.30.11-8080-2035\n" +
                        "   java.lang.Thread.State: BLOCKED (on object monitor)\n" +
                        "\tat java.lang.Throwable.printStackTrace(Throwable.java:460)";

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

        assertTrue(events.isEmpty());
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
        assertTrue(e.isDaemon());
        assertEquals(5, e.getPrio().intValue());
        assertEquals(0, e.getOsPrio().intValue());
        assertEquals("0x00007f7dc853b800", e.getTid());
        assertEquals("0x4799", e.getNid());
        assertEquals(ThreadState.RUNNABLE, e.getThreadState());
        assertEquals("0x00007f7d871f0000", e.getMonitor());

        String rawContent = e.getRawRepresentation();
        assertEquals(rawContent, content);
    }

    @Test
    public void parse_Production3() throws Exception {

        String content =
                "\n" +
                        "\"Thread-21\" prio=3 tid=0x00000001069c4800 nid=0x81 in Object.wait() [0xfffffffe4537f000]\n" +
                        "   java.lang.Thread.State: TIMED_WAITING (on object monitor)\n" +
                        "\tat java.lang.Object.wait(Native Method)\n" +
                        "\t- waiting on <0xfffffffe7beb7660> (a java.lang.Object)\n" +
                        "\tat com.arjuna.ats.internal.arjuna.recovery.PeriodicRecovery.doPeriodicWait(PeriodicRecovery.java:675)\n" +
                        "\tat com.arjuna.ats.internal.arjuna.recovery.PeriodicRecovery.run(PeriodicRecovery.java:434)\n" +
                        "\t- locked <0xfffffffe7beb7660> (a java.lang.Object)\n" +
                        "\n" +
                        "";

        StackTraceParser p = new StackTraceParser();

        BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(content.getBytes())));

        String line;

        List<Event> events = new ArrayList<>();

        long lineNumber = 1;

        for(; (line = br.readLine()) != null; lineNumber ++) {

            List<Event> es = p.parse(lineNumber, line);
            events.addAll(es);
        }

        events.addAll(p.flush());

        br.close();

        assertEquals(1, events.size());

        StackTraceEvent e = (StackTraceEvent)events.get(0);

        assertEquals("Thread-21", e.getThreadName());
        assertFalse(e.isDaemon());
        assertEquals(3, e.getPrio().intValue());
        assertNull(e.getOsPrio());
        assertEquals("0x00000001069c4800", e.getTid());
        assertEquals("0x81", e.getNid());
        assertEquals(ThreadState.OBJECT_WAIT, e.getThreadState());
        assertEquals("0xfffffffe4537f000", e.getMonitor());
    }

    @Test
    public void parse_Production4() throws Exception {

        String content =
            "\"http-192.168.30.11-8080-2038\" daemon prio=10 tid=0x000000005208b800 nid=0x421b waiting for monitor entry [0x00002aab56b65000]\n" +
            "   java.lang.Thread.State: BLOCKED (on object monitor)\n" +
            "\tat java.lang.Throwable.printStackTrace(Throwable.java:460)\n" +
            "\t- waiting to lock <0x0000000680a6b8e0> (a org.jboss.logging.util.LoggerStream)\n" +
            "\tat java.lang.Throwable.printStackTrace(Throwable.java:451)\n" +
            "\tat org.exoplatform.services.organization.idm.PicketLinkIDMOrganizationServiceImpl.endRequest(PicketLinkIDMOrganizationServiceImpl.java:183)\n" +
            "\tat java.lang.Thread.run(Thread.java:662)";

        StackTraceParser p = new StackTraceParser();

        BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(content.getBytes())));

        String line;

        List<Event> events = new ArrayList<>();

        long lineNumber = 1;

        for(; (line = br.readLine()) != null; lineNumber ++) {

            List<Event> es = p.parse(lineNumber, line);
            events.addAll(es);
        }

        events.addAll(p.flush());

        br.close();

        assertEquals(1, events.size());

        StackTraceEvent e = (StackTraceEvent)events.get(0);

        assertEquals("http-192.168.30.11-8080-2038", e.getThreadName());
        assertTrue(e.isDaemon());
        assertEquals(10, e.getPrio().intValue());
        assertNull(e.getOsPrio());
        assertEquals("0x000000005208b800", e.getTid());
        assertEquals("0x421b", e.getNid());
        assertEquals(ThreadState.WAITING_FOR_MONITOR_ENTRY, e.getThreadState());
        assertEquals("0x00002aab56b65000", e.getMonitor());
    }

    @Test
    public void parse_testFilteredFragment_NoCR() throws Exception {

        File f = new File(System.getProperty("basedir"), "src/test/resources/samples/013_filtered_fragment_no_CR.txt");

        assertTrue(f.isFile());

        StackTraceParser p = new StackTraceParser();

        List<Event> events = new ArrayList<>();

        BufferedReader br = new BufferedReader(new FileReader(f));

        String line;
        long lineNumber = 1;
        for(; (line = br.readLine()) != null; lineNumber ++) {

            List<Event> es = p.parse(lineNumber, line);
            events.addAll(es);
        }

        events.addAll(p.flush());

        br.close();

        assertEquals(1, events.size());
        StackTraceEvent e = (StackTraceEvent)events.get(0);
        assertEquals("0x0000000051421000", e.getTid());
        assertEquals("http-192.168.30.11-8080-1903", e.getThreadName());
    }

    @Test
    public void parse_testFilteredFragment() throws Exception {

        File f = new File(System.getProperty("basedir"), "src/test/resources/samples/014_filtered_fragment.txt");

        assertTrue(f.isFile());

        StackTraceParser p = new StackTraceParser();

        List<Event> events = new ArrayList<>();

        BufferedReader br = new BufferedReader(new FileReader(f));

        String line;
        long lineNumber = 1;
        for(; (line = br.readLine()) != null; lineNumber ++) {

            List<Event> es = p.parse(lineNumber, line);
            events.addAll(es);
        }

        events.addAll(p.flush());

        br.close();

        assertEquals(2, events.size());
        StackTraceEvent e = (StackTraceEvent)events.get(0);
        assertEquals("0x000000005208b800", e.getTid());
        assertEquals("http-192.168.30.11-8080-2038", e.getThreadName());

        StackTraceEvent e2 = (StackTraceEvent)events.get(1);
        assertEquals("0x000000005078a800", e2.getTid());
        assertEquals("http-192.168.30.11-8080-2035", e2.getThreadName());
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
