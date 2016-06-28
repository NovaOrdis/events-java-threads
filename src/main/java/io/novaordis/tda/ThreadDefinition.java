package io.novaordis.tda;

/**
 * @author <a href="mailto:ovidiu@novaordis.com">Ovidiu Feodorov</a>
 *
 * Copyright 2010 Ovidiu Feodorov
 */
public class ThreadDefinition
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    private StringBuffer original;
    private String oneLine;
    private String firstLine;
    private String name;

    // Constructors --------------------------------------------------------------------------------

    public ThreadDefinition()
    {
        original = new StringBuffer();
        oneLine = "";
    }

    // Public --------------------------------------------------------------------------------------

    public boolean isEmpty()
    {
        return original.length() == 0;
    }

    /**
     * Anything matches a null regexp.
     */
    public boolean matches(String regex)
    {
        return regex == null || oneLine.indexOf(regex) != -1;

    }

    public String getOriginal()
    {
        return original.toString();
    }

    @Override
    public String toString()
    {
        return name == null ? "INVALID THREAD DEFINITION" : name;
    }

    // Package protected ---------------------------------------------------------------------------

    /**
     * Appends a line.
     */
    void append(String line, long lineNumber) throws Exception
    {
        if (firstLine == null)
        {
            firstLine = line;

            // attempt preemptively extracting the header name. Extracting a valid header name
            // is a pre-requisite to a valid thread definition
            name = extractThreadName(line);

        }

        original.append(line).append("\n");
        oneLine += line + " ";
    }

    boolean isValid()
    {
        return name != null;
    }

    void clear()
    {
        original.setLength(0);
        firstLine = null;
        oneLine = null;
    }

    /**
     * Will return null if thread definition invalid.
     */
    String getName()
    {
        return name;
    }

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    /**
     * @return null if cannot extract the thread name.
     */
    private String extractThreadName(String line)
    {
        if (line == null)
        {
            return null;
        }

        if (!line.startsWith("\""))
        {
            return null;
        }

        line = line.substring(1);

        int i = line.indexOf("\"");

        if (i == -1)
        {
            // quotes don't close
            return null;
        }

        return line.substring(0, i);
    }

    // Inner classes -------------------------------------------------------------------------------
}
