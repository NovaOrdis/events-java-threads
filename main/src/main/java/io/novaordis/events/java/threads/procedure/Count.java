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

import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import io.novaordis.events.api.event.EndOfStreamEvent;
import io.novaordis.events.api.event.Event;
import io.novaordis.events.java.threads.event.JavaThreadDumpEvent;
import io.novaordis.events.processing.EventProcessingException;
import io.novaordis.events.processing.ProcedureBase;

/**
 * This is an "override" of the generic count, which counts stack traces per thread dump event instead of top-level
 * events.
 *
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 11/29/17
 */
public class Count extends ProcedureBase {

    // Constants -------------------------------------------------------------------------------------------------------

    public static final String LABEL = "count";

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private PrintStream out;

    private SimpleDateFormat timestampFormat;

    // Constructors ----------------------------------------------------------------------------------------------------

    public Count() {

        this.out = System.out;
        this.timestampFormat = new SimpleDateFormat("MM/dd/yy HH:mm:ss");
    }

    // ProcedureBase overrides -----------------------------------------------------------------------------------------

    @Override
    protected void process(AtomicLong invocationCount, Event e) throws EventProcessingException {

        if (e instanceof EndOfStreamEvent) {

            return;
        }

        if (!(e instanceof JavaThreadDumpEvent)) {

            throw new IllegalArgumentException("expecting a JavaThreadDumpEvent, got " + e);
        }

        JavaThreadDumpEvent jtde = (JavaThreadDumpEvent)e;

        int i = jtde.getThreadCount();
        long timestamp = jtde.getTime();

        out.println(timestampFormat.format(timestamp) + ", " + i);
    }

    @Override
    public List<String> getCommandLineLabels() {

        return Collections.singletonList(LABEL);
    }

    // Public ----------------------------------------------------------------------------------------------------------

    public DateFormat getTimestampFormat() {

        return timestampFormat;
    }

    // Package protected -----------------------------------------------------------------------------------------------

    void setPrintStream(PrintStream ps) {

        this.out = ps;
    }

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
