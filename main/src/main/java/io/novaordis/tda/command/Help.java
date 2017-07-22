package io.novaordis.tda.command;


import io.novaordis.tda.Command;
import io.novaordis.tda.SimplifiedLogger;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @author <a href="mailto:ovidiu@novaordis.com">Ovidiu Feodorov</a>
 *
 * Copyright 2010 Ovidiu Feodorov
 */
public class Help implements Command
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    private SimplifiedLogger log;

    // Constructors --------------------------------------------------------------------------------

    public Help(SimplifiedLogger log)
    {
        this.log = log;
    }

    // Command implementation ----------------------------------------------------------------------

    public void run() throws Exception
    {
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("HELP.txt");

        if (is == null)
        {
            log.error("HELP.txt not found in the root of the jar file, no help available");
        }

        BufferedReader br = null;

        try
        {
            br = new BufferedReader(new InputStreamReader(is));

            String line;

            while((line = br.readLine()) != null)
            {
                log.info(line);
            }
        }
        finally
        {
            if (br != null)
            {
                br.close();
            }
        }
    }

    // Public --------------------------------------------------------------------------------------

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------

}
