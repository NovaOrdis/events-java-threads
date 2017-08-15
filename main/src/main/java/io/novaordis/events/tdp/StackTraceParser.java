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
            "^\"(.+)\"(.+) tid=(0[xX][0-9a-fA-F]+)( .*$)");

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

            //
            // we process all expected fields in a long try/catch, and any exception at this stage gets the whole
            // thread stack discarded, and a warning message sent to log
            //

            try {

                processStackTraceHeader(currentStackTrace, m.group(1), m.group(3), m.group(2), m.group(4));
            }
            catch(Exception e) {

                log.warn("line " + lineNumber + ": " + e.getMessage());
                log.debug("current stack trace event " + currentStackTrace + " is being discarded");
                currentStackTrace = null;
                return EMPTY_LIST;
            }
        }
        else {

            //
            // the line did not match the header pattern; for the time being, accumulate it to raw representation of
            // the thread stack
            //

            if (currentStackTrace != null) {

                if (log.isDebugEnabled()) {

                    log.debug("adding line " + lineNumber + " to the raw representation of " + currentStackTrace);
                }
            }
            else {

                log.warn("line " + lineNumber + " will be discarded: " + line);
            }
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

    // Static package protected ----------------------------------------------------------------------------------------

    /**
     * @param e the StackTraceEvent instance that is being updated.
     *
     * @param threadName the thread name identified by the regular expression.
     * @param tid the TID identified by the regular expression.
     * @param fragment the header fragment between the header name and TID.
     * @param fragment2 the header fragment that comes after the TID.
     *
     * @throws Exception
     */
    static void processStackTraceHeader(
            StackTraceEvent e, String threadName, String tid, String fragment, String fragment2)
            throws Exception {


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
    }

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
