package io.novaordis.tda;

/**
 * @author <a href="mailto:ovidiu@novaordis.com">Ovidiu Feodorov</a>
 *
 * Copyright 2010 Ovidiu Feodorov
 */
public class UserException extends Exception
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    private long lineNumber;

    // Constructors --------------------------------------------------------------------------------

    public UserException()
    {
        super();
    }

    public UserException(String message)
    {
        super(message);
    }

    public UserException(String message, long lineNumber)
    {
        super(message);
        this.lineNumber = lineNumber;
    }

    public UserException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public UserException(Throwable cause)
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
