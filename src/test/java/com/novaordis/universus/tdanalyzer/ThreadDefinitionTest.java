package com.novaordis.universus.tdanalyzer;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.StringReader;

/**
 * @author <a href="mailto:ovidiu@novaordis.com">Ovidiu Feodorov</a>
 *
 * Copyright 2010 Ovidiu Feodorov
 */

public class ThreadDefinitionTest extends Assert
{
    // Constants -----------------------------------------------------------------------------------

    private static final Logger log = Logger.getLogger(ThreadDefinitionTest.class);

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    // Public --------------------------------------------------------------------------------------

    // VALID DEFINITIONS TESTS ---------------------------------------------------------------------

    @Test
    public void testValidDefinition() throws Exception
    {
        String s =
            "\"http-192.168.30.11-8080-2035\" daemon prio=10 tid=0x000000005078a800 nid=0x4218 waiting for monitor entry [0x00002aab56862000]\n" +
            "   java.lang.Thread.State: BLOCKED (on object monitor)\n" +
            "\tat java.lang.Throwable.printStackTrace(Throwable.java:460)";

        ThreadDefinition td = new ThreadDefinition();

        String line = null;
        BufferedReader br = new BufferedReader(new StringReader(s));
        while((line = br.readLine()) != null)
        {
            td.append(line, -1);
        }

        assertTrue(td.isValid());
        assertEquals("http-192.168.30.11-8080-2035", td.getName());
    }

    @Test
    public void testValidDefinition_QuotesDoNotClose() throws Exception
    {
        String s =
            "\"http-192.168.30.11-8080-2035\n" +
            "   java.lang.Thread.State: BLOCKED (on object monitor)\n" +
            "\tat java.lang.Throwable.printStackTrace(Throwable.java:460)";

        ThreadDefinition td = new ThreadDefinition();

        String line = null;
        BufferedReader br = new BufferedReader(new StringReader(s));
        while((line = br.readLine()) != null)
        {
            td.append(line, -1);
        }

        assertFalse(td.isValid());
        assertNull(td.getName());
    }


    // MATCH TESTS ---------------------------------------------------------------------------------

    @Test
    public void testMatch() throws Exception
    {
        ThreadDefinition td = new ThreadDefinition();

        td.append("\"ajp-10.7.25.129-8009-587\" daemon prio=3 tid=0x000000010be0c000 nid=0x34d runnable [0xfffffffe1b07e000]", -1);
        td.append("   java.lang.Thread.State: RUNNABLE", -1);
        td.append("\tat java.net.SocketInputStream.socketRead0(Native Method)", -1);
        td.append("\tat java.net.SocketInputStream.read(SocketInputStream.java:129)", -1);
        td.append("\tat java.net.ManagedSocketInputStreamHighPerformance.read(ManagedSocketInputStreamHighPerformance.java:258)", -1);
        td.append("\tat org.apache.coyote.ajp.AjpProcessor.read(AjpProcessor.java:1036)", -1);
        td.append("\tat org.apache.coyote.ajp.AjpProcessor.readMessage(AjpProcessor.java:1115)", -1);
        td.append("\tat org.apache.coyote.ajp.AjpProcessor.process(AjpProcessor.java:383)", -1);
        td.append("\tat org.apache.coyote.ajp.AjpProtocol$AjpConnectionHandler.process(AjpProtocol.java:384)", -1);
        td.append("\tat org.apache.tomcat.util.net.JIoEndpoint$Worker.run(JIoEndpoint.java:451)", -1);
        td.append("\tat java.lang.Thread.run(Thread.java:619)", -1);

        assertFalse(td.matches("blah"));

        assertTrue(td.matches("ajp-10."));
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
