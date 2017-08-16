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
import io.novaordis.events.api.event.EventProperty;
import io.novaordis.events.api.event.Property;
import io.novaordis.events.tdp.event.JavaThreadDumpEvent;
import io.novaordis.events.tdp.event.MemorySnapshotEvent;
import io.novaordis.events.tdp.event.StackTraceEvent;
import io.novaordis.events.tdp.event.ThreadState;
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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 8/14/17
 */
public class JavaThreadDumpParserTest {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    // Tests -----------------------------------------------------------------------------------------------------------

    // close() ---------------------------------------------------------------------------------------------------------

    @Test
    public void overridden_close_OnEmpty() throws Exception {

        JavaThreadDumpParser p = new JavaThreadDumpParser();

        assertTrue(p.close(7L).isEmpty());
    }

    @Test
    public void close_OnEmpty() throws Exception {

        JavaThreadDumpParser p = new JavaThreadDumpParser();

        List<Event> events = p.close();
        assertEquals(1, events.size());
        assertTrue(events.get(0) instanceof EndOfStreamEvent);
    }

    // parse() ---------------------------------------------------------------------------------------------------------

    @Test
    public void parse_InvalidContent() throws Exception {

        String content =
                "\n" +
                        "blah\n" +
                        "blah\n" +
                        "\n";

        JavaThreadDumpParser p = new JavaThreadDumpParser();

        BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(content.getBytes())));

        String line;

        List<Event> events = new ArrayList<>();

        long lineNumber = 1;

        for(; (line = br.readLine()) != null; lineNumber ++) {

            List<Event> es = p.parse(lineNumber, line);
            events.addAll(es);
        }

        br.close();

        events.addAll(p.close());

        assertEquals(1, events.size());
        assertTrue(events.get(0) instanceof EndOfStreamEvent);
    }

    @Test
    public void parse_simplestSyntheticThreadDump() throws Exception {

        String line1 = "\n";
        String line2 = "something that should not bother the parser\n";
        String line3 = "2016-08-13 17:42:10\n";
        String line4 = "Full thread dump Java HotSpot(TM) 64-Bit Server VM (25.51-b03 mixed mode):\n";
        String line5 = "\n";
        String line6 = "\"GC task thread#0 (ParallelGC)\" os_prio=0 tid=0x00007f6220025000 nid=0x1829 runnable\n";
        String line7 = "\n";

        String content = line1 + line2 + line3 + line4 + line5 + line6 + line7;

        JavaThreadDumpParser p = new JavaThreadDumpParser();

        BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(content.getBytes())));

        String line;

        List<Event> events = new ArrayList<>();

        long lineNumber = 1;

        for(; (line = br.readLine()) != null; lineNumber ++) {

            List<Event> es = p.parse(lineNumber, line);
            events.addAll(es);
        }

        //
        // since it is the only event, it should not show up here, but on close()
        //

        assertTrue(events.isEmpty());

        events = p.close();

        br.close();

        assertEquals(2, events.size());

        JavaThreadDumpEvent e = (JavaThreadDumpEvent)events.get(0);

        assertEquals(
                JavaThreadDumpParser.THREAD_DUMP_TIMESTAMP_FORMATS[0].parse("2016-08-13 17:42:10").getTime(),
                e.getTime().longValue());

        assertTrue(events.get(1) instanceof EndOfStreamEvent);
    }

    @Test
    public void parse_simplestSyntheticThreadDump_JNI_global_references_marker_found() throws Exception {

        String[] lines = {

                "",
                "something that should not bother the parser",
                "2016-08-13 17:42:10",
                "Full thread dump Java HotSpot(TM) 64-Bit Server VM (25.51-b03 mixed mode):",
                "",
                "\"GC task thread#0 (ParallelGC)\" os_prio=0 tid=0x00007f6220025000 nid=0x1829 runnable",
                "",
                "JNI global references: 2311"
        };

        String content = fromArray(lines);

        JavaThreadDumpParser p = new JavaThreadDumpParser();

        BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(content.getBytes())));

        String line;

        List<Event> events = new ArrayList<>();

        long lineNumber = 1;

        for(; (line = br.readLine()) != null; lineNumber ++) {

            List<Event> es = p.parse(lineNumber, line);
            events.addAll(es);
        }

        events.addAll(p.close());

        br.close();

        assertEquals(2, events.size());
        assertTrue(events.get(1) instanceof EndOfStreamEvent);
        JavaThreadDumpEvent e = (JavaThreadDumpEvent)events.get(0);

        //
        // make sure the marker is not added to the last stack trace
        //

        String theLastAndOnlyStackTrace = e.getStackTraceEvent(0).getRawRepresentation();

        assertTrue(theLastAndOnlyStackTrace.contains("0x00007f6220025000"));
        assertFalse(theLastAndOnlyStackTrace.contains("JNI"));
    }

    @Test
    public void parse_simplestSyntheticThreadDump_JNI_global_references_marker_And_Heap() throws Exception {

        String[] lines = {
                "",
                "something that should not bother the parser",
                "2011-10-04 00:09:00",
                "Full thread dump Java HotSpot(TM) 64-Bit Server VM (16.3-b01 mixed mode):",
                "",
                "\"GC task thread#0 (ParallelGC)\" os_prio=0 tid=0x00007f6220025000 nid=0x1829 runnable",
                "",
                "JNI global references: 2311",
                "",
                "Heap",
                " par new generation   total 191744K, used 185976K [0xfffffffe50000000, 0xfffffffe5d000000, 0xfffffffe5d000000)",
                "  eden space 170496K,  96% used [0xfffffffe50000000, 0xfffffffe5a0de248, 0xfffffffe5a680000)",
                "  from space 21248K, 100% used [0xfffffffe5a680000, 0xfffffffe5bb40000, 0xfffffffe5bb40000)",
                " to   space 21248K,   0% used [0xfffffffe5bb40000, 0xfffffffe5bb40000, 0xfffffffe5d000000)",
                " concurrent mark-sweep generation total 3981312K, used 2740212K [0xfffffffe5d000000, 0xffffffff50000000, 0xffffffff50000000)",
                " concurrent-mark-sweep perm gen total 275232K, used 165064K [0xffffffff50000000, 0xffffffff60cc8000, 0xffffffff70000000)",
                "",
                "2011-10-04 00:09:02",
                "Full thread dump Java HotSpot(TM) 64-Bit Server VM (16.3-b01 mixed mode):",
                "\"ajp-10.7.25.129-8009-600\" daemon prio=3 tid=0x000000010accf000 nid=0x35a runnable [0xfffffffe1a37e000]",
        };

        String content = fromArray(lines);

        JavaThreadDumpParser p = new JavaThreadDumpParser();

        BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(content.getBytes())));

        String line;

        List<Event> events = new ArrayList<>();

        long lineNumber = 1;

        for(; (line = br.readLine()) != null; lineNumber ++) {

            List<Event> es = p.parse(lineNumber, line);
            events.addAll(es);
        }

        events.addAll(p.close());

        br.close();

        assertEquals(3, events.size());
        assertTrue(events.get(0) instanceof JavaThreadDumpEvent);
        assertTrue(events.get(1) instanceof MemorySnapshotEvent);
        assertTrue(events.get(2) instanceof EndOfStreamEvent);

        JavaThreadDumpEvent e = (JavaThreadDumpEvent)events.get(0);

        //
        // make sure the marker is not added to the last stack trace
        //

        String theLastAndOnlyStackTrace = e.getStackTraceEvent(0).getRawRepresentation();

        assertTrue(theLastAndOnlyStackTrace.contains("0x00007f6220025000"));
        assertFalse(theLastAndOnlyStackTrace.contains("JNI"));

        MemorySnapshotEvent mse = (MemorySnapshotEvent)events.get(1);
        assertNotNull(mse);
    }

    @Test
    public void parse_simplestSyntheticThreadDump_JNI_global_references_marker_And_Heap_At_The_End() throws Exception {

        String[] lines = {
                "",
                "something that should not bother the parser",
                "2011-10-04 00:09:00",
                "Full thread dump Java HotSpot(TM) 64-Bit Server VM (16.3-b01 mixed mode):",
                "",
                "\"GC task thread#0 (ParallelGC)\" os_prio=0 tid=0x00007f6220025000 nid=0x1829 runnable",
                "",
                "JNI global references: 2311",
                "",
                "Heap",
                " par new generation   total 191744K, used 185976K [0xfffffffe50000000, 0xfffffffe5d000000, 0xfffffffe5d000000)",
                "  eden space 170496K,  96% used [0xfffffffe50000000, 0xfffffffe5a0de248, 0xfffffffe5a680000)",
                "  from space 21248K, 100% used [0xfffffffe5a680000, 0xfffffffe5bb40000, 0xfffffffe5bb40000)",
        };

        String content = fromArray(lines);

        JavaThreadDumpParser p = new JavaThreadDumpParser();

        BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(content.getBytes())));

        String line;

        List<Event> events = new ArrayList<>();

        long lineNumber = 1;

        for(; (line = br.readLine()) != null; lineNumber ++) {

            List<Event> es = p.parse(lineNumber, line);
            events.addAll(es);
        }

        events.addAll(p.close());

        br.close();

        assertEquals(3, events.size());
        assertTrue(events.get(0) instanceof JavaThreadDumpEvent);
        assertTrue(events.get(1) instanceof MemorySnapshotEvent);
        assertTrue(events.get(2) instanceof EndOfStreamEvent);

        JavaThreadDumpEvent e = (JavaThreadDumpEvent)events.get(0);

        //
        // make sure the marker is not added to the last stack trace
        //

        String theLastAndOnlyStackTrace = e.getStackTraceEvent(0).getRawRepresentation();

        assertTrue(theLastAndOnlyStackTrace.contains("0x00007f6220025000"));
        assertFalse(theLastAndOnlyStackTrace.contains("JNI"));

        MemorySnapshotEvent mse = (MemorySnapshotEvent)events.get(1);
        assertNotNull(mse);
    }

    @Test
    public void parse_HeaderFailsToFollowTimestamp() throws Exception {

        String content =
                "\n" +
                        "something that should not bother the parser\n" +
                        "2016-08-13 01:01:01\n" +
                        "something unexpected\n" +
                        "Full thread dump Java HotSpot(TM) 64-Bit Server VM (25.51-b03 mixed mode):\n" +
                        "\n" +
                        "something that does not matter\n" +
                        "something that does not matter\n" +
                        "2016-08-13 02:02:02\n" +
                        "Full thread dump Java HotSpot(TM) 64-Bit Server VM (25.51-b03 mixed mode):\n" +
                        "\n" +
                        "\n";

        JavaThreadDumpParser p = new JavaThreadDumpParser();

        BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(content.getBytes())));

        String line;

        List<Event> events = new ArrayList<>();

        long lineNumber = 1;

        for(; (line = br.readLine()) != null; lineNumber ++) {

            List<Event> es = p.parse(lineNumber, line);
            events.addAll(es);
        }

        events.addAll(p.close());

        br.close();

        //
        // we must catch the follow up event
        //

        assertEquals(2, events.size());

        JavaThreadDumpEvent e = (JavaThreadDumpEvent)events.get(0);

        assertEquals(
                JavaThreadDumpParser.THREAD_DUMP_TIMESTAMP_FORMATS[0].parse("2016-08-13 02:02:02").getTime(),
                e.getTime().longValue());

        assertTrue(events.get(1) instanceof EndOfStreamEvent);
    }

    @Test
    public void parse_HeaderFailsToFollowTimestamp_AfterAValidThreadDump() throws Exception {

        String content =
                "2016-08-13 01:01:01\n" +
                        "Full thread dump Java HotSpot(TM) 64-Bit Server VM (25.51-b03 mixed mode):\n" +
                        "\n" +
                        "2016-08-13 02:02:02\n" +
                        "something unexpected\n" +
                        "Full thread dump Java HotSpot(TM) 64-Bit Server VM (25.51-b03 mixed mode):\n" +
                        "\n" +
                        "something that does not matter\n" +
                        "something that does not matter\n" +
                        "2016-08-13 03:03:03\n" +
                        "Full thread dump Java HotSpot(TM) 64-Bit Server VM (25.51-b03 mixed mode):\n" +
                        "\n" +
                        "\n";

        JavaThreadDumpParser p = new JavaThreadDumpParser();

        BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(content.getBytes())));

        String line;

        List<Event> events = new ArrayList<>();

        long lineNumber = 1;

        for(; (line = br.readLine()) != null; lineNumber ++) {

            List<Event> es = p.parse(lineNumber, line);
            events.addAll(es);
        }

        events.addAll(p.close());

        br.close();

        //
        // we must catch the follow up event
        //

        assertEquals(3, events.size());

        JavaThreadDumpEvent e = (JavaThreadDumpEvent)events.get(0);

        assertEquals(
                JavaThreadDumpParser.THREAD_DUMP_TIMESTAMP_FORMATS[0].parse("2016-08-13 01:01:01").getTime(),
                e.getTime().longValue());

        JavaThreadDumpEvent e2 = (JavaThreadDumpEvent)events.get(1);

        assertEquals(
                JavaThreadDumpParser.THREAD_DUMP_TIMESTAMP_FORMATS[0].parse("2016-08-13 03:03:03").getTime(),
                e2.getTime().longValue());

        assertTrue(events.get(2) instanceof EndOfStreamEvent);
    }

    @Test
    public void parse_EmptyLineExpectedAfterHeaderButNonEmptyLineArrives() throws Exception {

        String line1 = "2016-08-13 01:01:01\n";
        String line2 = "Full thread dump Java HotSpot(TM) 64-Bit Server VM (25.51-b03 mixed mode):\n";
        String line3 = "SYNTHETIC NON-EMPTY LINE\n";
        String line4 = "\"GC task thread#0 (ParallelGC)\" os_prio=0 tid=0x00007f6220025000 nid=0x1829 runnable\n";
        String line5 = "\n";
        String line6 = "this will be discarded as well\n";
        String line7 = "2016-08-13 02:02:02\n";
        String line8 = "Full thread dump Java HotSpot(TM) 64-Bit Server VM (25.51-b03 mixed mode):\n";
        String line9 = "\n";
        String line10 = "\"GC task thread#0 (ParallelGC)\" os_prio=0 tid=0x00007f6220025000 nid=0x1829 runnable\n";
        String line11 = "\n";

        String content = line1 + line2 + line3 + line4 + line5 + line6 + line7 + line8 + line9 + line10 + line11;

        JavaThreadDumpParser p = new JavaThreadDumpParser();

        BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(content.getBytes())));

        String line;

        List<Event> events = new ArrayList<>();

        long lineNumber = 1;

        for(; (line = br.readLine()) != null; lineNumber ++) {

            List<Event> es = p.parse(lineNumber, line);
            events.addAll(es);
        }

        //
        // since it is the only event, it should not show up here, but on close()
        //

        assertTrue(events.isEmpty());

        events = p.close();

        br.close();

        assertEquals(2, events.size());

        JavaThreadDumpEvent e = (JavaThreadDumpEvent)events.get(0);

        assertEquals(
                JavaThreadDumpParser.THREAD_DUMP_TIMESTAMP_FORMATS[0].parse("2016-08-13 02:02:02").getTime(),
                e.getTime().longValue());

        assertTrue(events.get(1) instanceof EndOfStreamEvent);

        List<Property> eventProperties = e.getProperties(Event.class);
        assertEquals(1, eventProperties.size());
        EventProperty ep = (EventProperty)eventProperties.get(0);
        StackTraceEvent e2 = (StackTraceEvent)ep.getEvent();

        assertEquals("GC task thread#0 (ParallelGC)", e2.getThreadName());
    }

    @Test
    public void parse_twoSyntheticThreadDumps() throws Exception {

        String content =
                "\n" +
                        "something that should not bother the parser\n" +
                        "2016-01-01 01:01:01\n" +
                        "Full thread dump Java HotSpot(TM) 64-Bit Server VM (25.51-b03 mixed mode):\n" +
                        "\n" +
                        "something else that should not bother the parser\n" +
                        "2016-02-02 02:02:02\n" +
                        "Full thread dump Java HotSpot(TM) 64-Bit Server VM (25.51-b03 mixed mode):\n" +
                        "\n";

        JavaThreadDumpParser p = new JavaThreadDumpParser();

        BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(content.getBytes())));

        String line;

        List<Event> events = new ArrayList<>();

        long lineNumber = 1;

        for(; (line = br.readLine()) != null; lineNumber ++) {

            List<Event> es = p.parse(lineNumber, line);
            events.addAll(es);
        }

        assertEquals(1, events.size());

        JavaThreadDumpEvent e = (JavaThreadDumpEvent)events.get(0);

        assertEquals(
                JavaThreadDumpParser.THREAD_DUMP_TIMESTAMP_FORMATS[0].parse("2016-01-01 01:01:01").getTime(),
                e.getTime().longValue());


        events = p.close();

        br.close();

        assertEquals(2, events.size());

        JavaThreadDumpEvent e2 = (JavaThreadDumpEvent)events.get(0);

        assertEquals(
                JavaThreadDumpParser.THREAD_DUMP_TIMESTAMP_FORMATS[0].parse("2016-02-02 02:02:02").getTime(),
                e2.getTime().longValue());

        assertTrue(events.get(1) instanceof EndOfStreamEvent);
    }

    @Test
    public void parse_endToEnd() throws Exception {

        File f = new File(System.getProperty("basedir"), "src/test/resources/samples/015_successive_thread_dumps.txt");

        assertTrue(f.isFile());

        JavaThreadDumpParser p = new JavaThreadDumpParser();

        BufferedReader br = new BufferedReader(new FileReader(f));

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

        assertEquals(3, events.size());
    }

    @Test
    public void parse_Production1() throws Exception {

        File f = new File(System.getProperty("basedir"), "src/test/resources/samples/000.txt");

        assertTrue(f.isFile());

        JavaThreadDumpParser p = new JavaThreadDumpParser();

        BufferedReader br = new BufferedReader(new FileReader(f));

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

        assertEquals(2, events.size());

        JavaThreadDumpEvent e = (JavaThreadDumpEvent)events.get(0);

        assertEquals(3, e.getThreadCount());

        StackTraceEvent ste = e.getStackTraceEvent(2);
        assertEquals("VM Periodic Task Thread", ste.getThreadName());
        assertEquals(ThreadState.WAITING_ON_CONDITION, ste.getThreadState());

        MemorySnapshotEvent memorySnapshot = (MemorySnapshotEvent)events.get(1);
        assertNotNull(memorySnapshot);
    }

    // tests from the previous version ---------------------------------------------------------------------------------

    @Test
    public void parse_Production2() throws Exception {

        File f = new File(System.getProperty("basedir"),
                "src/test/resources/samples/010_thread_dump_file_timestamp_header.txt");

        assertTrue(f.isFile());

        JavaThreadDumpParser p = new JavaThreadDumpParser();

        List<Event> events = new ArrayList<>();

        BufferedReader br = new BufferedReader(new FileReader(f));

        String line;
        long lineNumber = 1;
        for(; (line = br.readLine()) != null; lineNumber ++) {

            List<Event> es = p.parse(lineNumber, line);
            events.addAll(es);
        }

        events.addAll(p.close(lineNumber));

        br.close();

        assertEquals(2, events.size());

        JavaThreadDumpEvent e = (JavaThreadDumpEvent)events.get(0);

        assertEquals(JavaThreadDumpParser.THREAD_DUMP_TIMESTAMP_FORMATS[0].parse("2010-12-14 01:02:03").getTime(),
                e.getTimestamp().getTime());

        assertEquals(2, e.getThreadCount());

        MemorySnapshotEvent me = (MemorySnapshotEvent)events.get(1);
        assertNotNull(me);
    }
//
//    @Test
//    public void testCtor_JustHeader() throws Exception {
//
//        ThreadDumpFile tdf = new ThreadDumpFile(
//            "./src/test/resources/samples/011_thread_dump_header_no_timestamp.txt");
//
//        assertEquals(1, tdf.getCount());
//
//        ThreadDump td = tdf.get(0);
//        assertNull(td.getTimestamp());
//        assertEquals("Full thread dump Java HotSpot(TM) 64-Bit Server VM (16.3-b01 mixed mode):",
//                     td.getHeader());
//        assertEquals(2, td.getThreadCount());
//    }
//
//    @Test
//    public void testCtor_TwoThreadDumps() throws Exception {
//
//        ThreadDumpFile tdf =
//            new ThreadDumpFile("./src/test/resources/samples/012_two_thread_dumps.txt");
//
//        assertEquals(2, tdf.getCount());
//
//        ThreadDump td = tdf.get(0);
//        assertEquals(ThreadDumpFile.TIMESTAMP_FORMAT.parseObject("2010-12-14 01:02:03"),
//                     td.getTimestamp());
//        assertEquals("Full thread dump Java HotSpot(TM) 64-Bit Server VM (16.3-b05 mixed mode):",
//                     td.getHeader());
//        assertEquals(2, td.getThreadCount());
//
//        td = tdf.get(1);
//        assertEquals(ThreadDumpFile.TIMESTAMP_FORMAT.parseObject("2011-01-02 03:04:05"),
//                     td.getTimestamp());
//        assertEquals("Full thread dump Java HotSpot(TM) 64-Bit Server VM (16.3-b11 mixed mode):",
//                     td.getHeader());
//        assertEquals(3, td.getThreadCount());
//    }
//
//    @Test
//    public void testCtor_FullThreadDumpOnFirstLine() throws Exception {
//
//        // this is NOT a fragment, but an incomplete thread dump
//
//        try {
//
//            new ThreadDumpFile("./src/test/resources/samples/002_1_FullThreadDumpOnFirstLine.txt");
//            fail("should have failed with UserErrorException");
//        }
//        catch(ParsingException e) {
//
//            log.info(e.getMessage());
//            assertEquals(1, e.getLineNumber().longValue());
//        }
//    }
//
//    @Test
//    public void testCtor_FullThreadDumpOnFirstLine_2() throws Exception {
//
//        ThreadDumpFile tdf =
//            new ThreadDumpFile("./src/test/resources/samples/002_2_FullThreadDumpOnFirstLine.txt");
//
//        assertEquals(1, tdf.getCount());
//        ThreadDump td = tdf.get(0);
//
//        assertNull(td.getTimestamp());
//        assertEquals("Full thread dump Java HotSpot(TM) 64-Bit Server VM (16.3-b71 mixed mode):",
//                     td.getHeader());
//
//        assertEquals(2, td.getThreadCount());
//    }
//
//    @Test
//    public void testCtor_FullThreadDumpOnInvalidTimestamp() throws Exception {
//
//        // this is NOT a fragment, but an incomplete thread dump
//
//        try {
//
//            new ThreadDumpFile("./src/test/resources/samples/003_FullThreadDumpInvalidTimestamp.txt");
//            fail("should have failed with UserErrorException");
//        }
//        catch(ParsingException e) {
//
//            log.info(e.getMessage());
//            assertEquals(5, e.getLineNumber().longValue());
//        }
//    }
//
//    @Test
//    public void testCtor_NoEmptyLineAfterHeader() throws Exception {
//
//        try {
//
//            new ThreadDumpFile("./src/test/resources/samples/005_NoEmptyLine.txt");
//            fail("should have failed with UserErrorException");
//        }
//        catch(ParsingException e) {
//
//            log.info(e.getMessage());
//            assertEquals(9, e.getLineNumber().longValue());
//        }
//    }
//
//    @Test
//    public void testCtor_Minimal() throws Exception {
//
//        ThreadDumpFile tdf = new ThreadDumpFile("./src/test/resources/samples/004_Minimal.txt");
//
//        assertEquals(1, tdf.getCount());
//        ThreadDump td = tdf.get(0);
//
//        assertEquals(ThreadDumpFile.TIMESTAMP_FORMAT.parseObject("2011-09-09 15:16:17"),
//                     td.getTimestamp());
//        assertEquals("Full thread dump Java HotSpot(TM) 64-Bit Server VM (15.3-b01 mixed mode):",
//                     td.getHeader());
//        assertEquals(2, td.getThreadCount());
//    }
//
//    @Test
//    public void testCtor_Real_001() throws Exception {
//
//        ThreadDumpFile tdf = new ThreadDumpFile("./src/test/resources/samples/001.txt");
//
//        assertEquals(1, tdf.getCount());
//        ThreadDump td = tdf.get(0);
//
//        assertEquals(ThreadDumpFile.TIMESTAMP_FORMAT.parseObject("2011-10-04 00:09:02"),
//                     td.getTimestamp());
//
//        assertEquals("Full thread dump Java HotSpot(TM) 64-Bit Server VM (16.3-b01 mixed mode):",
//                     td.getHeader());
//
//        assertEquals(786, td.getThreadCount());
//    }
//
//    @Test
//    public void testFilteredFragment_NoCR() throws Exception {
//
//        ThreadDumpFile tdf =
//            new ThreadDumpFile("./src/test/resources/samples/013_filtered_fragment_no_CR.txt");
//
//        assertEquals(1, tdf.getCount());
//        ThreadDump td = tdf.get(0);
//
//        assertNull(td.getTimestamp());
//        assertNull(td.getHeader());
//        assertEquals(1, td.getThreadCount());
//    }
//
//    @Test
//    public void testFilteredFragment() throws Exception {
//
//        ThreadDumpFile tdf =
//            new ThreadDumpFile("./src/test/resources/samples/014_filtered_fragment.txt");
//
//        assertEquals(1, tdf.getCount());
//        ThreadDump td = tdf.get(0);
//
//        assertNull(td.getTimestamp());
//        assertNull(td.getHeader());
//        assertEquals(2, td.getThreadCount());
//    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    private static String fromArray(String[] lines) {

        String s = "";

        for(String line: lines) {

            s += line + "\n";
        }

        return s;
    }

    // Inner classes ---------------------------------------------------------------------------------------------------

}
