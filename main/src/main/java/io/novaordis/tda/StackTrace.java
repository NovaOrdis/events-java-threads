package io.novaordis.tda;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.Arrays;

/**
 * @author <a href="mailto:ovidiu@novaordis.com">Ovidiu Feodorov</a>
 *
 * Copyright 2010 Ovidiu Feodorov
 */
public class StackTrace {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private StringBuffer original;
    private String oneLine;
    private String firstLine;

    private String name;

    //
    // if null, it means the tid could not be extracted from the stack trace
    //
    private Long tid;
    private boolean tidHexRepresentationStartsWith0x;

    //
    // the hex string representation (without leading 0x, if present) length
    //
    private int tidHexRepresentationLength;

    // Constructors ----------------------------------------------------------------------------------------------------

    public StackTrace() {

        original = new StringBuffer();
        oneLine = "";
    }

    /**
     * @param lineNumber the line number the content starts at. 1-based.
     *
     * @param content the multi-line content
     */
    public StackTrace(long lineNumber, String content) throws Exception {

        this();

        BufferedReader br = null;

        try {

            br = new BufferedReader(new StringReader(content));

            String line;

            while((line = br.readLine()) != null) {

                append(line, lineNumber ++);
            }
        }
        finally {

            if (br != null) {

                br.close();
            }

        }
    }

    // Public ----------------------------------------------------------------------------------------------------------

    /**
     * @return the name of the thread represented by this stack trace. Will return null if thread definition is invalid.
     */
    public String getName() {

        return name;
    }

    /**
     * @return the thread ID. May return null, which means the thread ID could not be extracted from the stack trace.
     */
    public Long getTid() {

        return tid;
    }

    /**
     * @return the thread ID in a hexadecimal representation, similar to the one recorded in the stack trace. May
     * return null, which means the thread ID could not be extracted from the stack trace.
     */
    public String getTidAsHexString() {

        if (tid == null) {

            return null;
        }

        String s = Long.toHexString(tid);

        if (s.length() != tidHexRepresentationLength) {

            //
            // pad with leading zeroes
            //

            char[] padding = new char[tidHexRepresentationLength - s.length()];
            Arrays.fill(padding, '0');
            s = new String(padding) + s;
        }

        if (tidHexRepresentationStartsWith0x) {

            s = "0x" + s;
        }

        return s;
    }

    public boolean isEmpty() {

        return original.length() == 0;
    }

    /**
     * Anything matches a null regexp.
     */
    public boolean matches(String regex) {

        return regex == null || oneLine.contains(regex);
    }

    public String getOriginal()
    {
        return original.toString();
    }

    @Override
    public String toString() {
        return name == null ? "INVALID THREAD DEFINITION" : name;
    }

    // Package protected -----------------------------------------------------------------------------------------------

    /**
     * Appends a line.
     */
    void append(String line, long lineNumber) throws Exception {

        if (firstLine == null) {

            firstLine = line;

            //
            // attempt to extract preemptively the header name, tid, etc. A valid header name is a pre-requisite to a
            // valid thread definition
            //
            parseFirstLine(line);
        }

        original.append(line).append("\n");
        oneLine += line + " ";
    }

    boolean isValid() {

        return name != null;
    }

    void clear() {

        original.setLength(0);
        firstLine = null;
        oneLine = null;
    }

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    private void parseFirstLine(String line) {

        if (line == null) {

            return;
        }

        if (!line.startsWith("\"")) {

            return;
        }

        line = line.substring(1);

        int i = line.indexOf("\"");

        if (i == -1) {

            // quotes don't close
            return;
        }

        this.name = line.substring(0, i);

        i = line.indexOf("tid=");

        if (i != -1) {

            int j = line.indexOf(' ', i);

            if (j == -1) {

                j = line.length();
            }

            String hex = line.substring(i + "tid=".length(), j);

            if (hex.startsWith("0x")) {

                tidHexRepresentationStartsWith0x = true;
                hex = hex.substring("0x".length());
            }
            tidHexRepresentationLength = hex.length();
            tid = Long.parseUnsignedLong(hex, 16);
        }
    }

    // Inner classes ---------------------------------------------------------------------------------------------------
}
