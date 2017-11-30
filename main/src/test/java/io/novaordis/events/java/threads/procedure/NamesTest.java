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

package io.novaordis.events.java.threads.procedure;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.Test;

import io.novaordis.events.java.threads.event.JavaThreadDumpEvent;
import io.novaordis.events.java.threads.event.StackTraceEvent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 11/29/17
 */
public class NamesTest extends ProcedureTest {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    // Tests -----------------------------------------------------------------------------------------------------------

    @Test
    public void getCommandLineLabels() throws Exception {

        Names c = getProcedureToTest();

        assertTrue(c.getCommandLineLabels().contains(Names.LABEL));
    }

    // process() -------------------------------------------------------------------------------------------------------

    @Test
    public void process() throws Exception {

        Names c = getProcedureToTest();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(baos);

        c.setPrintStream(out);

        JavaThreadDumpEvent e = new JavaThreadDumpEvent(1L, 1000L);

        StackTraceEvent st = new StackTraceEvent(10L);
        st.setThreadName("Z");
        e.addStackTrace(st);

        StackTraceEvent st2 = new StackTraceEvent(20L);
        st2.setThreadName("K");
        e.addStackTrace(st2);

        StackTraceEvent st3 = new StackTraceEvent(30L);
        st3.setThreadName("A");
        e.addStackTrace(st3);

        c.process(e);

        out.flush();

        String actual = new String(baos.toByteArray());

        String expected = c.getTimestampFormat().format(1000L) + "\n  A\n  K\n  Z\n";

        assertEquals(expected, actual);
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    @Override
    protected Names getProcedureToTest() throws Exception {

        return new Names();
    }

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
