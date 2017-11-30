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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.novaordis.events.api.event.EndOfStreamEvent;
import io.novaordis.events.api.event.Event;
import io.novaordis.events.api.parser.ParserBase;
import io.novaordis.events.api.parser.QueryOnce;
import io.novaordis.events.java.threads.event.JavaThreadDumpEvent;
import io.novaordis.events.java.threads.event.MemorySnapshotEvent;
import io.novaordis.events.java.threads.event.StackTraceEvent;
import io.novaordis.events.query.Query;
import io.novaordis.utilities.parsing.ParsingException;

/**
 * The implementation is NOT thread safe.
 *
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 8/14/17
 */
public class JavaThreadDumpParser extends ParserBase {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(JavaThreadDumpParser.class);

    public static final String[] THREAD_DUMP_TIMESTAMP_FORMAT_STRINGS = new String[] {

            // 2016-08-13 17:42:10
            "yyyy-MM-dd HH:mm:ss",
    };

    public static final Pattern[] THREAD_DUMP_TIMESTAMP_PATTERNS = new Pattern[] {

            // 2016-08-13 17:42:10
            Pattern.compile("^[1-3]\\d\\d\\d-[0-1]\\d-[0-3]\\d [0-2]\\d:[0-5]\\d:[0-5]\\d *$"),
    };

    public static final DateFormat[] THREAD_DUMP_TIMESTAMP_FORMATS = new DateFormat[] {

            // 2016-08-13 17:42:10
            new SimpleDateFormat(THREAD_DUMP_TIMESTAMP_FORMAT_STRINGS[0]),
    };

    //
    // IMPORTANT: every time a new timestamp pattern is added, the corresponding String, Format and a Pattern must be
    //            added on the same position in the corresponding arrays.
    //

    //
    // Full thread dump Java HotSpot(TM) 64-Bit Server VM (25.51-b03 mixed mode):
    //

    public static final Pattern[] THREAD_DUMP_HEADER_PATTERNS = new Pattern[] {

            Pattern.compile("^Full thread dump.*$"),
    };

    private static final String MARKER_JNI_GLOBAL_REFERENCES = "JNI global references:";
    private static final String MARKER_HEAP = "Heap";

    private static final List<Event> EMPTY_EVENT_LIST = Collections.emptyList();

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private StackTraceParser stackTraceParser;

    // maintained in raw format (untrimmed)
    private String threadDumpTimestamp;

    private JavaThreadDumpEvent currentJavaThreadDumpEvent;

    private boolean discardEmptyLine;

    private MemorySnapshotEvent memorySnapshotEvent;

    // Constructors ----------------------------------------------------------------------------------------------------

    public JavaThreadDumpParser() {

        this.stackTraceParser = new StackTraceParser();
    }

    // ParserBase overrides --------------------------------------------------------------------------------------------

    /**
     * @return StackTraceEvents, MemorySnapshotEvents, etc.
     */
    @Override
    protected List<Event> parse(long lineNumber, String line, Query query) throws ParsingException {

        if (log.isDebugEnabled()) {

            log.debug("parsing line " + lineNumber + ": " + line);
        }

        List<Event> result = null;

        //
        // a thread dump event is marked by a consecutive succession of a timestamp, a header and an empty line
        //

        if (discardEmptyLine) {

            //
            // the previous line was a thread dump header and we expect this line to be empty
            //

            discardEmptyLine = false;

            if (line.trim().isEmpty()) {

                //
                // we "logically" discard the line, but we keep it in the raw representation of the event
                //

                currentJavaThreadDumpEvent.appendRawLine(line);

                if (log.isDebugEnabled()) {

                    log.debug("discarded empty line " + lineNumber);
                }
            }
            else {

                //
                // we are expecting an empty line and we got a non-empty line, the format of the thread dump is
                // invalid, drop the current thread dump event and warn through the rest of the lines associated
                // with this thread dump event
                //

                log.warn("line " + lineNumber + " is invalid: expecting an empty line that follows a thread dump header but got: " + line);
                currentJavaThreadDumpEvent = null;
            }

        }
        else if (threadDumpTimestamp != null) {

            //
            // we identified a thread dump timestamp on the previous line, this line must be a thread dump header

            Matcher m = THREAD_DUMP_HEADER_PATTERNS[0].matcher(line);

            if (!m.find()) {

                threadDumpTimestamp = null;
                log.warn("skipping thread dump started at line " + (lineNumber - 1) + " because thread dump header missing on line " + lineNumber + ": " + line);
            }
            else {

                if (log.isDebugEnabled()) {

                    log.debug("thread dump header found, building the thread dump event ...");
                }

                //
                // parse the timestamp, we keep the format at this layer
                //

                long timestamp;

                try {

                    timestamp = THREAD_DUMP_TIMESTAMP_FORMATS[0].parse(threadDumpTimestamp.trim()).getTime();
                }
                catch(ParseException e) {

                    //
                    // this indicates a programming error, as the correct pattern was already identified; it means
                    // there is a mismatch between pattern and format, so we signal invalid state
                    //

                    throw new IllegalStateException(
                            "mismatch between thread dump timestamp pattern and format, line: " + lineNumber, e);
                }

                currentJavaThreadDumpEvent = new JavaThreadDumpEvent(lineNumber, timestamp);
                currentJavaThreadDumpEvent.appendRawLine(threadDumpTimestamp);
                currentJavaThreadDumpEvent.appendRawLine(line);
                threadDumpTimestamp = null;
                discardEmptyLine = true;
            }
        }
        else if (memorySnapshotEvent != null) {

            if (line.trim().isEmpty()) {

                if (log.isDebugEnabled()) {

                    log.debug("line " + lineNumber + " concludes memory snapshot parsing");
                }

                QueryOnce.set(memorySnapshotEvent, query != null);
                result = Collections.singletonList(memorySnapshotEvent);
                memorySnapshotEvent = null;
            }
            else {

                memorySnapshotEvent.parse(line);
            }
        }
        else if (line.startsWith(MARKER_JNI_GLOBAL_REFERENCES) || line.startsWith(MARKER_HEAP)) {

            //
            // we are not doing anything with it yet, but we use the information to tokenize the stream
            //

            if (currentJavaThreadDumpEvent != null) {

                //
                // there are no more stack traces coming after this, so we don't want to append this line the last
                // one, wrap up the current thread dump instead
                //

                result = wrapUpCurrentThreadDump(query, currentJavaThreadDumpEvent, stackTraceParser, line);
                currentJavaThreadDumpEvent = null;
            }

            if (line.startsWith(MARKER_HEAP)) {

                memorySnapshotEvent = new MemorySnapshotEvent();
            }
        }
        else {

            //
            // TODO: currently we only check a single set of patterns, generalize when we need to check the second
            //

            Matcher m = THREAD_DUMP_TIMESTAMP_PATTERNS[0].matcher(line);

            if (m.matches()) {

                //
                // we identified a new thread dump in the same file, put the thread dump parser in "expect a header
                // line" mode. We maintain the whole line, without trimming, to add it later to the raw representation
                //

                this.threadDumpTimestamp = line;

                if (log.isDebugEnabled()) {

                    log.debug("thread dump timestamp found: " + threadDumpTimestamp);
                }

                if (currentJavaThreadDumpEvent != null) {

                    //
                    // since we established that another thread dump is starting, we are wrapping up the current thread
                    // dump event, if any. The method collects all leftovers from the stack trace parser, but does not
                    // close the stack trace parser
                    //

                    result = wrapUpCurrentThreadDump(query, currentJavaThreadDumpEvent, stackTraceParser, null);
                    currentJavaThreadDumpEvent = null;
                }
            }
            else {

                if (currentJavaThreadDumpEvent == null) {

                    //
                    // we ignore this line, there's nothing we can do with it; normally there should be no ignored
                    // lines in a valid thread dump file, so we make it visible and warn
                    //

                    //
                    // if it is not blank, warn
                    //

                    if (!line.trim().isEmpty()) {

                        log.warn("discarding line " + lineNumber + ": " + line);
                    }
                }
                else
                {

                    //
                    // engage the stack trace parser and identify individual stack traces
                    //

                    List<Event> stackTraces = stackTraceParser.parse(lineNumber, line, query);
                    currentJavaThreadDumpEvent.addStackTraces(stackTraces);
                }
            }
        }

        if (result == null) {

            return EMPTY_EVENT_LIST;
        }
        else {

            return result;
        }
    }

    /**
     * @return StackTraceEvents, MemorySnapshotEvents, EndOfStreamEvents.
     */
    @Override
    protected List<Event> close(long lineNumber) throws ParsingException {

        List<Event> result = new ArrayList<>();

        if (currentJavaThreadDumpEvent != null) {


            //
            // collect all leftovers from the stack trace parser
            //

            List<Event> stackTraces = stackTraceParser.close();

            for (Event e : stackTraces) {

                if (e instanceof EndOfStreamEvent) {

                    break;
                }

                StackTraceEvent ste = (StackTraceEvent) e;
                currentJavaThreadDumpEvent.addStackTrace(ste);
            }

            result.add(currentJavaThreadDumpEvent);
        }

        if (memorySnapshotEvent != null) {

            result.add(memorySnapshotEvent);
        }

        return result;
    }

    // Public ----------------------------------------------------------------------------------------------------------

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    /**
     * Wrap up the given (current) thread dump event: collect all leftovers from the stack trace parser, but don't close
     * the stack trace parser, as it will be needed to process upcoming thread dump events.
     *
     * @param query may be null
     * @param epilogueLine may be null if there's no epilogue line.
     */
    private static List<Event> wrapUpCurrentThreadDump(
            Query query, JavaThreadDumpEvent current, StackTraceParser stackTraceParser, String epilogueLine) {

        if (current == null) {

            //
            // nothing to wrap up, we probably shouldn't have been called anyway
            //

            return EMPTY_EVENT_LIST;
        }

        List<Event> stackTraces = stackTraceParser.flush();

        //
        // if we have query, apply it
        //

        if (query != null) {

            stackTraces = query.filter(stackTraces);
        }

        current.addStackTraces(stackTraces);

        if (epilogueLine != null) {

            current.setStringProperty(JavaThreadDumpEvent.RAW_EPILOGUE_PROPERTY_NAME, epilogueLine);
        }

        QueryOnce.set(current, query != null);
        List<Event> result = Collections.singletonList(current);

        if (log.isDebugEnabled()) {

            log.debug(current.toString() + " parsing complete");
        }

        return result;
    }

    // Inner classes ---------------------------------------------------------------------------------------------------

}
