package io.novaordis.events.tdp;

import io.novaordis.utilities.UserErrorException;

/**
 * @author <a href="mailto:ovidiu@novaordis.com">Ovidiu Feodorov</a>
 *
 * Help in HELP.txt
 *
 * Copyright 2010 Ovidiu Feodorov
 */
public class Main {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    public static void main(String[] args) throws Exception {

        try {

            Configuration config = new Configuration(args);

            Command c = config.getCommand();

            c.run();

        }
        catch(UserErrorException e) {

            String msg = e.getMessage();
            System.err.println("[error]: " + msg);
        }
    }

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------
}
