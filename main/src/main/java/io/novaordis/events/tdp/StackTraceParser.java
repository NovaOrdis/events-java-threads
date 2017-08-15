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

import io.novaordis.events.api.event.Event;
import io.novaordis.events.api.parser.ParserBase;
import io.novaordis.events.api.parser.ParsingException;
import io.novaordis.events.tdp.event.StackTraceEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Currency;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 8/14/17
 */
public class StackTraceParser extends ParserBase {

    // Constants -------------------------------------------------------------------------------------------------------

    public static final Pattern STACK_TRACE_HEADER_PATTERN = Pattern.compile(
            "^\"(.+)\" os_prio=(\\d) tid=(0x[0-9a-fA-F]+) nid=(0x\\d+) (.+)");

    private static final Logger log = LoggerFactory.getLogger(StackTraceParser.class);

    private static final List<Event> EMPTY_LIST = Collections.emptyList();

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private StackTraceEvent currentStackTrace;

    // Constructors ----------------------------------------------------------------------------------------------------

    // ParserBase overrides --------------------------------------------------------------------------------------------

    @Override
    protected List<Event> parse(long lineNumber, String line) throws ParsingException {

        List<Event> result = null;

        if (line.isEmpty()) {

            //
            // stack trace separator found
            //

            if (currentStackTrace != null) {

                if (log.isDebugEnabled()) {

                    log.debug(currentStackTrace.toString() + " parsing complete");
                }

                result = Collections.singletonList(currentStackTrace);
                currentStackTrace = null;
                return result;
            }
            else {

                if (log.isDebugEnabled()) {

                    log.debug("line " + lineNumber + ": empty line ignored");
                }

                return EMPTY_LIST;
            }
        }

        Matcher m = STACK_TRACE_HEADER_PATTERN.matcher(line);

        if (m.matches()) {

            if (log.isDebugEnabled()) {

                log.debug("stack trace header identified at line " + lineNumber);
            }

            if (currentStackTrace != null) {

                result = Collections.singletonList(currentStackTrace);
            }

            currentStackTrace = new StackTraceEvent(lineNumber);

            String threadName = m.group(1);
            currentStackTrace.setThreadName(threadName);

            String osPrios = m.group(2);
            int osPrio;

            try {

                osPrio = Integer.parseInt(osPrios);
            }
            catch(Exception e) {

                log.warn("");

                log.debug("current stack trace event " + currentStackTrace + " is being discarded");
                currentStackTrace = null;
                return EMPTY_LIST;
            }

            currentStackTrace.setOsPrio(osPrio);

            String tid = m.group(3);
            String nid = m.group(4);
        }
        else {


        }

        if (result == null) {

            return EMPTY_LIST;
        }
        else {

            return result;
        }
    }

    @Override
    protected List<Event> close(long lineNumber) throws ParsingException {

        return Collections.emptyList();
    }

    // Public ----------------------------------------------------------------------------------------------------------

    /**
     * Collect remaining state, but do not close.
     */
    public List<Event> flush() {

        return Collections.emptyList();
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
