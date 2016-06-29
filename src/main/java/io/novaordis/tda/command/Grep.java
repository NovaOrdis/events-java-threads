package io.novaordis.tda.command;


import io.novaordis.tda.Command;
import io.novaordis.tda.SimplifiedLogger;
import io.novaordis.tda.ThreadDefinition;
import io.novaordis.tda.ThreadDump;
import io.novaordis.tda.ThreadDumpFile;
import io.novaordis.tda.UserErrorException;

import java.io.File;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

/**
 * @author <a href="mailto:ovidiu@novaordis.com">Ovidiu Feodorov</a>
 *
 * Copyright 2010 Ovidiu Feodorov
 */
public class Grep implements Command
{
    // Constants -----------------------------------------------------------------------------------

    public static final Format TIMESTAMP_PREFIX = new SimpleDateFormat("yyyy.MM.dd-HH.mm.ss");

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    private SimplifiedLogger log;

    private String regex;

    private ThreadDumpFile threadDumpFile;

    private boolean exclude;
    private boolean count;
    private boolean split;

    // Constructors --------------------------------------------------------------------------------

    public Grep(SimplifiedLogger log, String[] args) throws Exception
    {
        this.log = log;

        for (String arg : args) {
            if ("-v".equals(arg)) {
                exclude = true;
            } else if ("-c".equals(arg)) {
                count = true;
            } else if ("-s".equals(arg)) {
                split = true;
            } else if (arg.startsWith("-")) {
                throw new Exception("NOT YET IMPLEMENTED EXCEPTION: 'grep' doesn't know how to handle " + arg);
            } else if (regex == null) {
                regex = arg;
            } else if (threadDumpFile == null) {
                threadDumpFile = new ThreadDumpFile(arg);
            }
        }

        if (threadDumpFile == null)
        {
            threadDumpFile = new ThreadDumpFile(regex);
            regex = null;
        }

        if (exclude && regex == null)
        {
            throw new UserErrorException("-v requires a regular expression");
        }
    }

    // Command implementation ----------------------------------------------------------------------

    public void run() throws Exception
    {
        if (split)
        {
            split(threadDumpFile);
            return;
        }

        // grep functionality

        int cnt = 0;

        for(Iterator<ThreadDump> i = threadDumpFile.iterator(); i.hasNext(); )
        {
            ThreadDump td = i.next();

            for(Iterator<ThreadDefinition> j = td.iterator(); j.hasNext(); )
            {
                ThreadDefinition d = j.next();

                if (d.matches(regex))
                {
                    if (!exclude)
                    {
                        if (count)
                        {
                            cnt ++;
                        }
                        else
                        {
                            log.info(d.getOriginal());
                        }
                    }
                }
                else
                {
                    if (exclude)
                    {
                        if (count)
                        {
                            cnt ++;
                        }
                        else
                        {
                            log.info(d.getOriginal());
                        }
                    }
                }
            }
        }

        if (count)
        {
            log.info("" + cnt);
        }
    }

    // Public --------------------------------------------------------------------------------------

    public boolean isExclude()
    {
        return exclude;
    }

    public boolean isCount()
    {
        return count;
    }

    public String getRegex()
    {
        return regex;
    }

    public File getFile()
    {
        if (threadDumpFile == null)
        {
            return null;
        }

        return threadDumpFile.getFile();
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    private void split(ThreadDumpFile f) throws Exception
    {
        if (f.getCount() == 1)
        {
            log.info("one thread dump in file, nothing to split");
            return;
        }

        int counter = 0;

        for(Iterator<ThreadDump> i = f.iterator(); i.hasNext(); )
        {
            ThreadDump td = i.next();

            Date timestamp = td.getTimestamp();

            File splitFile = generateSplitFile(counter ++, timestamp, f.getFile());

            log.info("writing " + splitFile.getName());
            td.toFile(splitFile);
        }
    }

    /**
     * @param timestamp may be null, in which case only the counter should be used. If not null, both the counter and
     *                  timestamp will be used.
     */
    private File generateSplitFile(int counter, Date timestamp, File f) throws Exception
    {
        String s = Integer.toString(counter);
        if (counter < 10) {
            s = "0" + s;
        }

        if (timestamp != null) {

            s += "-";
            s += TIMESTAMP_PREFIX.format(timestamp);
        }

        return generateSplitFile(s, f);
    }

    private File generateSplitFile(String prefix, File f) throws Exception
    {
        //File parent = f.getParentFile();

        String name = f.getName();
        String base = name;
        String ext = "";

        int i = name.lastIndexOf(".");

        if (i != -1)
        {
            base = name.substring(0, i);
            ext = "." + name.substring(i + 1);
        }

        return new File(prefix + "-" + base + "-thread-dump" + ext);
    }

    // Inner classes -------------------------------------------------------------------------------

}
