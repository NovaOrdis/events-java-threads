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
import io.novaordis.events.tdp.event.JavaThreadDumpEvent;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
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
    public void parse_simplestSyntheticThreadDump() throws Exception {

        String content =
                "\n" +
                        "something that should not bother the parser\n" +
                        "2016-08-13 17:42:10\n" +
                        "Full thread dump Java HotSpot(TM) 64-Bit Server VM (25.51-b03 mixed mode):\n" +
                        "\n" +
                        "";

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

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
