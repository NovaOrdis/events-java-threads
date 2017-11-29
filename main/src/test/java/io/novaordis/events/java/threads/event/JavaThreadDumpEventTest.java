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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.junit.Test;

import io.novaordis.events.api.event.EndOfStreamEvent;
import io.novaordis.events.api.event.Event;
import io.novaordis.events.java.threads.JavaThreadDumpParser;

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

    // getRawRepresentation() ------------------------------------------------------------------------------------------

    @Test
    public void getRawRepresentation_Synthetic() throws Exception {

        JavaThreadDumpEvent e = new JavaThreadDumpEvent(7L, 1L);
        e.appendRawLine("something that simulates a thread dump header");
        assertNull(e.getStringProperty(JavaThreadDumpEvent.RAW_EPILOGUE_PROPERTY_NAME));

        StackTraceEvent t = new StackTraceEvent(17L);
        t.appendRawLine("stack trace header");
        t.appendRawLine("stack trace extra line");
        e.addStackTrace(t);

        String rawRepresentation = e.getRawRepresentation();

        String expected =
                "something that simulates a thread dump header\n" +
                        "stack trace header\n" +
                        "stack trace extra line";

        assertEquals(expected, rawRepresentation);
    }

    @Test
    public void getRawRepresentation_Synthetic_WithEpilogue() throws Exception {

        JavaThreadDumpEvent e = new JavaThreadDumpEvent(7L, 1L);
        e.appendRawLine("something that simulates a thread dump header");
        e.setStringProperty(JavaThreadDumpEvent.RAW_EPILOGUE_PROPERTY_NAME, "something that simulates an epilogue");

        StackTraceEvent t = new StackTraceEvent(17L);
        t.appendRawLine("stack trace header");
        t.appendRawLine("stack trace extra line");
        e.addStackTrace(t);

        String rawRepresentation = e.getRawRepresentation();

        String expected =
                "something that simulates a thread dump header\n" +
                        "stack trace header\n" +
                        "stack trace extra line\n" +
                        "something that simulates an epilogue";

        assertEquals(expected, rawRepresentation);
    }

    @Test
    public void getRawRepresentation_Synthetic_MultipleStackTraces() throws Exception {

        JavaThreadDumpEvent e = new JavaThreadDumpEvent(7L, 1L);
        e.appendRawLine("something that simulates a thread dump header");

        StackTraceEvent t = new StackTraceEvent(17L);
        t.appendRawLine("h1");
        t.appendRawLine("el1");
        e.addStackTrace(t);

        StackTraceEvent t2 = new StackTraceEvent(18L);
        t2.appendRawLine("h2");
        t2.appendRawLine("el2");
        e.addStackTrace(t2);

        StackTraceEvent t3 = new StackTraceEvent(19L);
        t3.appendRawLine("h3");
        t3.appendRawLine("el3");
        e.addStackTrace(t3);

        String rawRepresentation = e.getRawRepresentation();

        String expected =
                "something that simulates a thread dump header\n" +
                        "h1\n" +
                        "el1\n" +
                        "h2\n" +
                        "el2\n" +
                        "h3\n" +
                        "el3";

        assertEquals(expected, rawRepresentation);
    }

    @Test
    public void getRawRepresentation_MustBeIdenticalWithOriginalContent() throws Exception {

        File f = new File(System.getProperty("basedir"), "src/test/resources/samples/016_raw_representation.txt");

        assertTrue(f.isFile());

        String originalContent = new String(Files.readAllBytes(f.toPath()));

        JavaThreadDumpParser p = new JavaThreadDumpParser();

        BufferedReader br = new BufferedReader(new FileReader(f));

        String line;

        List<Event> events = new ArrayList<>();

        while((line = br.readLine()) != null) {

            events.addAll(p.parse(line));
        }

        br.close();

        JavaThreadDumpEvent e = (JavaThreadDumpEvent)events.get(0);

        String rawRepresentation = e.getRawRepresentation();

        //
        // compare it line by line to make easier to spot the differences
        //

        StringTokenizer orig = new StringTokenizer(originalContent, "\n");
        StringTokenizer raw = new StringTokenizer(rawRepresentation, "\n");
        int lineNumber = 1;

        while (orig.hasMoreTokens()) {

            if (!raw.hasMoreTokens()) {

                fail("the original content has more lines than the processed raw content, line " + lineNumber);
            }

            String origLine = orig.nextToken();
            String rawLine = raw.nextToken();

            if (!origLine.equals(rawLine)) {

                fail("lines " + lineNumber + " differ, original:\n>" + origLine + "<\n, raw:\n>" + rawLine + "<\n");
            }

            lineNumber ++;
        }

        if (raw.hasMoreTokens()) {

            fail("the proceesed raw content has more lines than the original content, line " + lineNumber);
        }
    }

    @Test
    public void getRawRepresentation_MustBeIdenticalWithOriginalContent_FirstLineContainsSpaces() throws Exception {

        File f = new File(System.getProperty("basedir"),
                "src/test/resources/samples/017_raw_representation_first_line_contains_spaces.txt");

        assertTrue(f.isFile());

        String originalContent = new String(Files.readAllBytes(f.toPath()));

        // make sure that the first line contains spaces
        int i = originalContent.indexOf('\n');
        assertTrue(i != -1);
        String firstLine = originalContent.substring(0, i);
        assertTrue(firstLine.length() != firstLine.trim().length());

        JavaThreadDumpParser p = new JavaThreadDumpParser();

        BufferedReader br = new BufferedReader(new FileReader(f));

        String line;

        List<Event> events = new ArrayList<>();

        while((line = br.readLine()) != null) {

            events.addAll(p.parse(line));
        }

        br.close();

        JavaThreadDumpEvent e = (JavaThreadDumpEvent)events.get(0);

        String rawRepresentation = e.getRawRepresentation();

        //
        // compare it line by line to make easier to spot the differences
        //

        StringTokenizer orig = new StringTokenizer(originalContent, "\n");
        StringTokenizer raw = new StringTokenizer(rawRepresentation, "\n");
        int lineNumber = 1;

        while (orig.hasMoreTokens()) {

            if (!raw.hasMoreTokens()) {

                fail("the original content has more lines than the processed raw content, line " + lineNumber);
            }

            String origLine = orig.nextToken();
            String rawLine = raw.nextToken();

            if (!origLine.equals(rawLine)) {

                fail("lines " + lineNumber + " differ, original:\n>" + origLine + "<\n, raw:\n>" + rawLine + "<\n");
            }

            lineNumber ++;
        }

        if (raw.hasMoreTokens()) {

            fail("the proceesed raw content has more lines than the original content, line " + lineNumber);
        }
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
