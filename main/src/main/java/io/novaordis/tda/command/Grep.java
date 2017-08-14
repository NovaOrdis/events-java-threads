package io.novaordis.tda.command;

import io.novaordis.tda.Command;
import io.novaordis.tda.SimplifiedLogger;
import io.novaordis.tda.StackTrace;
import io.novaordis.tda.ThreadDump;
import io.novaordis.tda.ThreadDumpFile;
import io.novaordis.utilities.UserErrorException;

import java.io.File;
import java.util.Iterator;

/**
 * @author <a href="mailto:ovidiu@novaordis.com">Ovidiu Feodorov</a>
 *
 * Copyright 2010 Ovidiu Feodorov
 */
public class Grep implements Command {

    // Constants -------------------------------------------------------------------------------------------------------


    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private SimplifiedLogger log;

    private String regex;

    private ThreadDumpFile threadDumpFile;

    private boolean exclude;
    private boolean count;

    // Constructors ----------------------------------------------------------------------------------------------------

    public Grep(SimplifiedLogger log, String[] args) throws Exception {

        this.log = log;

        for (String arg : args) {

            if ("-v".equals(arg)) {

                exclude = true;

            }
            else if ("-c".equals(arg)) {

                count = true;

            }
            else if (arg.startsWith("-")) {

                throw new UserErrorException("unknown grep option: '" + arg + "'");
            }
            else if (regex == null) {

                regex = arg;

            }
            else if (threadDumpFile == null) {

                threadDumpFile = new ThreadDumpFile(arg);
            }
        }

        if (threadDumpFile == null) {

            threadDumpFile = new ThreadDumpFile(regex);
            regex = null;
        }

        if (exclude && regex == null) {

            throw new UserErrorException("-v requires a regular expression");
        }
    }

    // Command implementation ------------------------------------------------------------------------------------------

    public void run() throws Exception {

//        if (split) {
//
//            split(threadDumpFile);
//            return;
//        }

        // grep functionality

        int cnt = 0;

        for(Iterator<ThreadDump> i = threadDumpFile.iterator(); i.hasNext(); ) {

            ThreadDump td = i.next();

            for(Iterator<StackTrace> j = td.iterator(); j.hasNext(); ) {

                StackTrace d = j.next();

                if (d.matches(regex)) {

                    if (!exclude) {

                        if (count) {

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

    // Public ----------------------------------------------------------------------------------------------------------

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

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
