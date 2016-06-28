package io.novaordis.tda;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * An individual thread dump, containing the snapshots of all threads in the JVM at a certain moment
 * in time. A log file (or a stdout dump file) may contain multiple thread dumps, so it may have
 * associated multiple ThreadDump instances.
 *
 * @see ThreadDumpFile
 *
 * @author <a href="mailto:ovidiu@novaordis.com">Ovidiu Feodorov</a>
 *
 * Copyright 2010 Ovidiu Feodorov
 */
public class ThreadDump
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    private File file;

    private Date timestamp;
    private String header;
    private long headerLineNumber;
    private List<ThreadDefinition> tds;

    private ThreadDefinition current;

    private boolean closed;

    // Constructors --------------------------------------------------------------------------------

    /**
     * @param timestamp - the timestamp to associate the thread dump with, if the VM did not
     *        capture it at the time of the dump. May be null.
     */
    ThreadDump(File file, Date timestamp, String header, long headerLineNumber)
    {
        this.file = file;
        this.timestamp = timestamp;
        this.header = header;
        this.headerLineNumber = headerLineNumber;
        this.tds = new ArrayList<ThreadDefinition>();
        this.current = new ThreadDefinition();
        this.closed = false;
    }

    // Public --------------------------------------------------------------------------------------

    /**
     * May be null if we did not get the proper timestamp in the snapshot.
     */
    public Date getTimestamp()
    {
        return timestamp;
    }

    public String getHeader()
    {
        return header;
    }

    public long getHeaderLineNumber()
    {
        return headerLineNumber;
    }

    public int getThreadCount()
    {
        return tds.size();
    }

    public Iterator<ThreadDefinition> iterator()
    {
        return tds.iterator();
    }

    public File getFile()
    {
        return file;
    }

    public void toFile(File target) throws Exception
    {
        BufferedWriter bw = null;

        try
        {
            bw = new BufferedWriter(new FileWriter(target));

            bw.write(ThreadDumpFile.THREAD_DUMP_HEADER + " (generated)\n");

            for(Iterator<ThreadDefinition> i = iterator(); i.hasNext(); )
            {
                ThreadDefinition td = i.next();

                bw.write(td.getOriginal());

                bw.write("\n");
            }
        }
        finally
        {
            if (bw != null)
            {
                bw.close();
            }
        }
    }

    @Override
    public String toString()
    {
        return
            "[" +
            (timestamp == null ? "UNDATED" : ThreadDumpFile.TIMESTAMP_FORMAT.format(timestamp)) + ", " +
            getThreadCount() + " threads" +
            "]";


    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    void append(String line, long lineNumber) throws Exception
    {
        if (closed)
        {
            throw new Exception(this + " is closed, no more lines can be appended to it");
        }

        if (line.trim().length() == 0)
        {
            // empty line - cue to start a new ThreadDefinition

            if (current.isValid())
            {
                // save the latest one
                tds.add(current);
                current = new ThreadDefinition();
            }
            else
            {
                // clean the garbage accumulated so far
                current.clear();
            }
        }
        else
        {
            current.append(line, lineNumber);
        }
    }

    /**
     * "closes" this thread dump - it is the notification that no more lines will be appended.
     */
    void close()
    {
        if (current.isValid())
        {
            // save the latest one
            tds.add(current);
        }

        current = null;
        closed = true;
    }

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------

}
