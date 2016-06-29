package io.novaordis.tda;

/**
 * @author <a href="mailto:ovidiu@novaordis.com">Ovidiu Feodorov</a>
 *
 * Copyright 2010 Ovidiu Feodorov
 */
public class UserErrorException extends Exception
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    private long lineNumber;

    // Constructors --------------------------------------------------------------------------------

    public UserErrorException()
    {
        super();
    }

    public UserErrorException(String message)
    {
        super(message);
    }

    public UserErrorException(String message, long lineNumber)
    {
        super(message);
        this.lineNumber = lineNumber;
    }

    public UserErrorException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public UserErrorException(Throwable cause)
    {
        super(cause);
    }

    // Public --------------------------------------------------------------------------------------

    public long getLineNumber()
    {
        return lineNumber;
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
