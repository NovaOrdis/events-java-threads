package io.novaordis.tda;


import io.novaordis.tda.command.Grep;
import io.novaordis.tda.command.Help;

/**
 * @author <a href="mailto:ovidiu@novaordis.com">Ovidiu Feodorov</a>
 *
 * Copyright 2010 Ovidiu Feodorov
 */
class Configuration
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    private Command c;

    private SimplifiedLogger log;

    // Constructors --------------------------------------------------------------------------------

    Configuration(String[] args) throws Exception
    {
        log = new StdoutLogger();

        if (args.length == 0)
        {
            c = new Help(log);
            return;
        }

        args = Util.coalesceQuotedStrings(args);

        // anything else is a Grep command

        c = new Grep(log, args);
    }

    // Public --------------------------------------------------------------------------------------

    public Command getCommand()
    {
        return c;
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------

}
