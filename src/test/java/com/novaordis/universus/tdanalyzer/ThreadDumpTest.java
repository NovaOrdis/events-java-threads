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

public class ThreadDumpTest extends Assert
{
    // Constants -----------------------------------------------------------------------------------

    private static final Logger log = Logger.getLogger(ThreadDumpTest.class);

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    // Public --------------------------------------------------------------------------------------

    /**
     * For a systematic approach to testing valid thread definitions, see VALID DEFINITIONS TESTS
     * section of ThreadDefinitionTest.
     */
    @Test
    public void testAppend_Invalid() throws Exception
    {
        String s =
            "\n" +
            "blah\n" +
            "blah\n" +
            "\n";

        ThreadDump td = new ThreadDump(null, null, null, -1);

        BufferedReader br = new BufferedReader(new StringReader(s));

        String line = null;

        while((line = br.readLine()) != null)
        {
            td.append(line, -1);
        }

        td.close();

        assertEquals(0, td.getThreadCount());

        br.close();
    }

    /**
     * For a systematic approach to testing valid thread definitions, see VALID DEFINITIONS TESTS
     * section of ThreadDefinitionTest.
     */
    @Test
    public void testAppend() throws Exception
    {
        String s =
            "\n" +
            "\"Thread-21\" prio=3 tid=0x00000001069c4800 nid=0x81 in Object.wait() [0xfffffffe4537f000]\n" +
            "   java.lang.Thread.State: TIMED_WAITING (on object monitor)\n" +
            "\tat java.lang.Object.wait(Native Method)\n" +
            "\t- waiting on <0xfffffffe7beb7660> (a java.lang.Object)\n" +
            "\tat com.arjuna.ats.internal.arjuna.recovery.PeriodicRecovery.doPeriodicWait(PeriodicRecovery.java:675)\n" +
            "\tat com.arjuna.ats.internal.arjuna.recovery.PeriodicRecovery.run(PeriodicRecovery.java:434)\n" +
            "\t- locked <0xfffffffe7beb7660> (a java.lang.Object)\n" +
            "\n" +
            "";

        ThreadDump td = new ThreadDump(null, null, null, -1);

        BufferedReader br = new BufferedReader(new StringReader(s));

        String line = null;

        while((line = br.readLine()) != null)
        {
            td.append(line, -1);
        }

        td.close();

        assertEquals(1, td.getThreadCount());

        br.close();
    }

    /**
     * For a systematic approach to testing valid thread definitions, see VALID DEFINITIONS TESTS
     * section of ThreadDefinitionTest.
     */
    @Test
    public void testAppend_NoFirstEmptyLine() throws Exception
    {
        String s =
            "\"http-192.168.30.11-8080-2038\" daemon prio=10 tid=0x000000005208b800 nid=0x421b waiting for monitor entry [0x00002aab56b65000]\n" +
            "   java.lang.Thread.State: BLOCKED (on object monitor)\n" +
            "\tat java.lang.Throwable.printStackTrace(Throwable.java:460)\n" +
            "\t- waiting to lock <0x0000000680a6b8e0> (a org.jboss.logging.util.LoggerStream)\n" +
            "\tat java.lang.Throwable.printStackTrace(Throwable.java:451)\n" +
            "\tat org.exoplatform.services.organization.idm.PicketLinkIDMOrganizationServiceImpl.endRequest(PicketLinkIDMOrganizationServiceImpl.java:183)\n" +
            "\tat java.lang.Thread.run(Thread.java:662)";

        ThreadDump td = new ThreadDump(null, null, null, -1);

        BufferedReader br = new BufferedReader(new StringReader(s));

        String line = null;

        while((line = br.readLine()) != null)
        {
            td.append(line, -1);
        }

        td.close();

        assertEquals(1, td.getThreadCount());

        br.close();
    }

    @Test
    public void testClose() throws Exception
    {
        ThreadDump td = new ThreadDump(null, null, null, -1);

        td.close();

        try
        {
            td.append("something", 1);
            fail("should fail with Exception, closed");
        }
        catch(Exception e)
        {
            log.info(e.getMessage());
        }
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
