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
public class CountTest extends ProcedureTest {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    // Tests -----------------------------------------------------------------------------------------------------------

    @Test
    public void getCommandLineLabels() throws Exception {

        Count c = getProcedureToTest();

        assertTrue(c.getCommandLineLabels().contains(Count.LABEL));
    }

    // process() -------------------------------------------------------------------------------------------------------

    @Test
    public void process() throws Exception {

        Count c = getProcedureToTest();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(baos);

        c.setPrintStream(out);

        JavaThreadDumpEvent e = new JavaThreadDumpEvent(1L, 1000L);
        e.addStackTrace(new StackTraceEvent(10L));
        e.addStackTrace(new StackTraceEvent(20L));

        c.process(e);

        out.flush();

        String actual = new String(baos.toByteArray());

        String expected = c.getTimestampFormat().format(1000L) + ", 2\n";

        assertEquals(expected, actual);
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    @Override
    protected Count getProcedureToTest() throws Exception {

        return new Count();
    }

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
