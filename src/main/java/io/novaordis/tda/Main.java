package io.novaordis.tda;

/**
 * @author <a href="mailto:ovidiu@novaordis.com">Ovidiu Feodorov</a>
 *
 * Help in HELP.txt
 *
 * Copyright 2010 Ovidiu Feodorov
 */
public class Main
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    private static int droppedBlocks = 0;

    public static void main(String[] args) throws Exception
    {
        Configuration config = new Configuration(args);

        Command c = config.getCommand();

        c.run();

    }

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    // Public --------------------------------------------------------------------------------------

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
