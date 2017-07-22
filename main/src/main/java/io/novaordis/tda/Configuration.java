package io.novaordis.tda;


import io.novaordis.tda.command.Diff;
import io.novaordis.tda.command.Grep;
import io.novaordis.tda.command.Help;
import io.novaordis.tda.command.Version;

/**
 * @author <a href="mailto:ovidiu@novaordis.com">Ovidiu Feodorov</a>
 *
 * Copyright 2010 Ovidiu Feodorov
 */
class Configuration {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private Command c;

    private SimplifiedLogger log;

    // Constructors ----------------------------------------------------------------------------------------------------

    Configuration(String[] args) throws Exception {

        log = new StdoutLogger();

        if (args.length == 0) {

            c = new Help(log);
            return;
        }

        args = Util.coalesceQuotedStrings(args);

        String command = args[0];

        if (Version.LITERAL.equals(command)) {

            c = new Version();
        }
        else if (Diff.LITERAL.equals(command)) {

            String[] args2 = new String[args.length - 1];
            System.arraycopy(args, 1, args2, 0, args2.length);
            c = new Diff(log, args2);
        }
        else if (Help.LITERAL.equals(command)) {


        }
        else {

            // anything else is a Grep command and the following argument is interpreted as a regular expression
            c = new Grep(log, args);
        }
    }

    // Public ----------------------------------------------------------------------------------------------------------

    public Command getCommand()
    {
        return c;
    }

    public SimplifiedLogger getLog() {

        return log;
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
