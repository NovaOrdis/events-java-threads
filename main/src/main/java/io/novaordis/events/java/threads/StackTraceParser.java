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

import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.novaordis.events.api.event.Event;
import io.novaordis.events.api.parser.ParserBase;
import io.novaordis.events.java.threads.event.StackTraceEvent;
import io.novaordis.events.query.Query;
import io.novaordis.utilities.parsing.ParsingException;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 8/14/17
 */
public class StackTraceParser extends ParserBase {

    // Constants -------------------------------------------------------------------------------------------------------

    public static final Pattern STACK_TRACE_HEADER_PATTERN = Pattern.compile(
            "^\"(.+)\"(.+) tid=([^ ]+)( .*$)");

    private static final Logger log = LoggerFactory.getLogger(StackTraceParser.class);

    private static final List<Event> EMPTY_LIST = Collections.emptyList();

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private StackTraceEvent currentStackTrace;

    // Constructors ----------------------------------------------------------------------------------------------------

    // ParserBase overrides --------------------------------------------------------------------------------------------

    @Override
    protected List<Event> parse(long lineNumber, String line, Query query) throws ParsingException {

        List<Event> result = EMPTY_LIST;

        Matcher m = STACK_TRACE_HEADER_PATTERN.matcher(line);

        if (m.matches()) {

            if (currentStackTrace != null) {

                result = Collections.singletonList(currentStackTrace);
                log.debug("parsing complete for " + currentStackTrace.toString());
            }

            if (log.isDebugEnabled()) {

                log.debug("new stack trace header identified at line " + lineNumber);
            }

            currentStackTrace = new StackTraceEvent(lineNumber);

            //
            // we process all expected fields in a long try/catch, and any exception at this stage gets the whole
            // thread stack discarded, and a warning message sent to log; if all goes well, the event will be updated
            // and the header will be added to the raw representation.
            //

            try {

                processStackTraceHeader(
                        lineNumber, currentStackTrace, m.group(1), m.group(3), m.group(2), m.group(4), line);
            }
            catch(Exception e) {

                log.warn("line " + lineNumber + ": " + e.getMessage());
                log.debug("current stack trace event " + currentStackTrace + " is being discarded");
                currentStackTrace = null;
                return result;
            }
        }
        else {

            //
            // the line did not match the header pattern; for the time being, accumulate it to raw representation of
            // the thread stack; this includes empty lines as well, we keep accumulating until we find another
            // thread dump
            //

            if (currentStackTrace != null) {

                currentStackTrace.appendRawLine(line);

                if (log.isDebugEnabled()) {

                    log.debug("line " + lineNumber + " appended to the raw representation of " + currentStackTrace);
                }
            }
            else {

                log.warn("line " + lineNumber + " will be discarded: " + line);
            }
        }

        return result;
    }

    @Override
    protected List<Event> close(long lineNumber) throws ParsingException {

        return flush();
    }

    // Public ----------------------------------------------------------------------------------------------------------

    /**
     * Collect remaining state, but do not close.
     */
    public List<Event> flush() {

        if (currentStackTrace != null) {

            List<Event> result = Collections.singletonList(currentStackTrace);
            currentStackTrace = null;
            return result;
        }

        return EMPTY_LIST;
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Static package protected ----------------------------------------------------------------------------------------

    /**
     * @param e the StackTraceEvent instance that is being updated.
     *
     * @param threadName the thread name identified by the regular expression.
     * @param tid the TID identified by the regular expression.
     * @param fragment the header fragment between the header name and TID.
     * @param fragment2 the header fragment that comes after the TID.
     * @param rawHeader provided "as is", to be added to the raw representation, if the instance wants to do that.
     *
     * @throws Exception
     */
    static void processStackTraceHeader(
            Long lineNumber, StackTraceEvent e, String threadName,
            String tid, String fragment, String fragment2, String rawHeader) throws Exception {


        e.setThreadName(threadName);
        e.setTid(tid);

        int i;

        if (fragment.contains("daemon")) {

            e.setDaemon(true);
        }

        //
        // prio
        //

        if (fragment.startsWith("prio=")) {

            i = 0;
        }
        else {

            i = fragment.indexOf(" prio=");
        }

        if (i != -1) {

            String prios = fragment.substring(i + " prio=".length());
            i = prios.indexOf(' ');
            if (i == -1) {
                i = prios.length();
            }
            e.setPrio(Integer.parseInt(prios.substring(0, i)));
        }

        //
        // os-prio
        //

        i = fragment.indexOf("os_prio=");

        if (i != -1) {

            String osPrios = fragment.substring(i + "os_prio=".length());
            i = osPrios.indexOf(' ');
            if (i == -1) {
                i = osPrios.length();
            }
            e.setOsPrio(Integer.parseInt(osPrios.substring(0, i)));
        }

        //
        // nid
        //

        i = fragment2.indexOf("nid=");
        int j = 0;

        if (i != -1) {

            j = fragment2.indexOf(' ', i + "nid=".length());
            e.setNid(fragment2.substring(i + "nid=".length(), j));
        }

        //
        // thread state
        //

        e.setThreadState(fragment2.substring(j));

        e.appendRawLine(rawHeader);

        if (log.isDebugEnabled()) {

            log.debug("line " + lineNumber + " appended to the raw representation of " + e);
        }
    }

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
