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

import io.novaordis.events.api.event.Event;
import io.novaordis.events.api.event.GenericTimedEvent;
import io.novaordis.events.api.event.Property;
import io.novaordis.utilities.time.TimestampImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 8/14/17
 */
public class JavaThreadDumpEvent extends GenericTimedEvent {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(JavaThreadDumpEvent.class);

    private static final DateFormat TIMESTAMP_DISPLAY_FORMAT = new SimpleDateFormat("MM/dd/yy HH:mm:ss");

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    /**
     * @param timestamp timestamp extracted and parsed by the upper layer.
     */
    public JavaThreadDumpEvent(Long lineNumber, long timestamp) {

        if (lineNumber != null) {

            setLineNumber(lineNumber);
        }

        setTimestamp(new TimestampImpl(timestamp));

        log.debug(this + " constructed");
    }

    // Public ----------------------------------------------------------------------------------------------------------

    /**
     * Preserves the order in which the stack traces were added.
     *
     * @exception IllegalArgumentException if the event is not a StackTraceEvent
     */
    public void addStackTraces(List<Event> stackTraces) {

        if (stackTraces == null) {

            throw new IllegalArgumentException("null stack trace list");
        }

        if (stackTraces.isEmpty()) {

            return;
        }

        for(Event e: stackTraces) {

            if (e instanceof StackTraceEvent) {

                addStackTrace((StackTraceEvent)e);
            }
            else {

                throw new IllegalArgumentException("not a StackTraceEvent: " + e);
            }
        }
    }

    /**
     * Preserves the order in which the stack traces were added.
     */
    public void addStackTrace(StackTraceEvent stackTrace) {

        if (stackTrace == null) {

            throw new IllegalArgumentException("null stack trace");
        }

        String tid = stackTrace.getTid();
        if (tid == null) {

            tid = UUID.randomUUID().toString();
        }

        setEventProperty(tid, stackTrace);
    }

    /**
     * @return the stack traces in order in which they were added.
     */
    public List<StackTraceEvent> getStackTraceEvents() {

        List<Property> properties = getProperties(StackTraceEvent.class);

        if (properties.isEmpty()) {

            return Collections.emptyList();
        }

        List<StackTraceEvent> result = new ArrayList<>(properties.size());

        //noinspection Convert2streamapi
        for(Property p: properties) {

            result.add((StackTraceEvent)p.getValue());
        }

        return result;
    }

    @Override
    public String toString() {

        Long time = getTime();

        if (time == null) {

            return "ThreadDump[UNINITIALIZED]";
        }

        Long lineNumber = getLineNumber();

        return "ThreadDump[" +
                (lineNumber != null ? "line " + lineNumber + ", "  : "") +
                TIMESTAMP_DISPLAY_FORMAT.format(time) + "]";
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
