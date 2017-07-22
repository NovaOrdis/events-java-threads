package io.novaordis.tda;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.StringReader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @author <a href="mailto:ovidiu@novaordis.com">Ovidiu Feodorov</a>
 *
 * Copyright 2010 Ovidiu Feodorov
 */
public class StackTraceTest {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    // Tests -----------------------------------------------------------------------------------------------------------

    // constructor -----------------------------------------------------------------------------------------------------

    @Test
    public void constructor() throws Exception {

        String s =
                "\"http-0.0.0.0:8443-266\" #2422 daemon prio=5 os_prio=0 tid=0x00007f7dc853b800 nid=0x4799 runnable [0x00007f7d871f0000]\n" +
                        "   java.lang.Thread.State: RUNNABLE\n" +
                        "\tat java.net.SocketInputStream.socketRead0(Native Method)\n" +
                        "\tat java.net.SocketInputStream.socketRead(SocketInputStream.java:116)\n" +
                        "\tat java.net.SocketInputStream.read(SocketInputStream.java:170)\n" +
                        "\tat java.net.SocketInputStream.read(SocketInputStream.java:141)\n" +
                        "\tat sun.security.ssl.InputRecord.readFully(InputRecord.java:465)\n" +
                        "\tat sun.security.ssl.InputRecord.read(InputRecord.java:503)\n" +
                        "\tat sun.security.ssl.SSLSocketImpl.readRecord(SSLSocketImpl.java:973)\n" +
                        "\t- locked <0x00000007857a86c0> (a java.lang.Object)\n" +
                        "\tat sun.security.ssl.SSLSocketImpl.readDataRecord(SSLSocketImpl.java:930)\n" +
                        "\tat sun.security.ssl.AppInputStream.read(AppInputStream.java:105)\n" +
                        "\t- locked <0x00000007857c2a48> (a sun.security.ssl.AppInputStream)\n" +
                        "\tat org.apache.coyote.http11.InternalInputBuffer.fill(InternalInputBuffer.java:713)\n" +
                        "\tat org.apache.coyote.http11.InternalInputBuffer.parseRequestLine(InternalInputBuffer.java:351)\n" +
                        "\tat org.apache.coyote.http11.Http11Processor.process(Http11Processor.java:819)\n" +
                        "\tat org.apache.coyote.http11.Http11Protocol$Http11ConnectionHandler.process(Http11Protocol.java:656)\n" +
                        "\t- locked <0x00000007ba6e1380> (a org.apache.coyote.http11.Http11Processor)\n" +
                        "\tat org.apache.tomcat.util.net.JIoEndpoint$Worker.run(JIoEndpoint.java:926)\n" +
                        "\tat java.lang.Thread.run(Thread.java:745)\n" +
                        "\n" +
                        "   Locked ownable synchronizers:\n" +
                        "\t- None\n";

        StackTrace t = new StackTrace(1, s);

        assertEquals("http-0.0.0.0:8443-266", t.getName());
        assertEquals("0x00007f7dc853b800", t.getTidAsHexString());
        assertEquals(s, t.getOriginal());
        assertFalse(t.isEmpty());
        assertTrue(t.isValid());
    }

    // getName() -------------------------------------------------------------------------------------------------------

    @Test
    public void getName() throws Exception {

        String s =
                "\"http-0.0.0.0:8443-266\" #2422 daemon prio=5 os_prio=0 tid=0x00007f7dc853b800 nid=0x4799 runnable [0x00007f7d871f0000]\n" +
                        "   java.lang.Thread.State: RUNNABLE\n" +
                        "\tat java.net.SocketInputStream.socketRead0(Native Method)\n" +
                        "\tat java.net.SocketInputStream.socketRead(SocketInputStream.java:116)\n" +
                        "\tat java.net.SocketInputStream.read(SocketInputStream.java:170)\n" +
                        "\tat java.net.SocketInputStream.read(SocketInputStream.java:141)\n" +
                        "\tat sun.security.ssl.InputRecord.readFully(InputRecord.java:465)\n" +
                        "\tat sun.security.ssl.InputRecord.read(InputRecord.java:503)\n" +
                        "\tat sun.security.ssl.SSLSocketImpl.readRecord(SSLSocketImpl.java:973)\n" +
                        "\t- locked <0x00000007857a86c0> (a java.lang.Object)\n" +
                        "\tat sun.security.ssl.SSLSocketImpl.readDataRecord(SSLSocketImpl.java:930)\n" +
                        "\tat sun.security.ssl.AppInputStream.read(AppInputStream.java:105)\n" +
                        "\t- locked <0x00000007857c2a48> (a sun.security.ssl.AppInputStream)\n" +
                        "\tat org.apache.coyote.http11.InternalInputBuffer.fill(InternalInputBuffer.java:713)\n" +
                        "\tat org.apache.coyote.http11.InternalInputBuffer.parseRequestLine(InternalInputBuffer.java:351)\n" +
                        "\tat org.apache.coyote.http11.Http11Processor.process(Http11Processor.java:819)\n" +
                        "\tat org.apache.coyote.http11.Http11Protocol$Http11ConnectionHandler.process(Http11Protocol.java:656)\n" +
                        "\t- locked <0x00000007ba6e1380> (a org.apache.coyote.http11.Http11Processor)\n" +
                        "\tat org.apache.tomcat.util.net.JIoEndpoint$Worker.run(JIoEndpoint.java:926)\n" +
                        "\tat java.lang.Thread.run(Thread.java:745)\n" +
                        "\n" +
                        "   Locked ownable synchronizers:\n" +
                        "\t- None\n";

        StackTrace t = new StackTrace(1, s);

        assertEquals("http-0.0.0.0:8443-266", t.getName());
        assertEquals("0x00007f7dc853b800", t.getTidAsHexString());

    }

    // VALID DEFINITIONS TESTS -----------------------------------------------------------------------------------------

    @Test
    public void testValidDefinition() throws Exception {

        String s =
            "\"http-192.168.30.11-8080-2035\" daemon prio=10 tid=0x000000005078a800 nid=0x4218 waiting for monitor entry [0x00002aab56862000]\n" +
            "   java.lang.Thread.State: BLOCKED (on object monitor)\n" +
            "\tat java.lang.Throwable.printStackTrace(Throwable.java:460)";

        StackTrace td = new StackTrace();

        String line;
        BufferedReader br = new BufferedReader(new StringReader(s));
        while((line = br.readLine()) != null)
        {
            td.append(line, -1);
        }

        assertTrue(td.isValid());
        assertEquals("http-192.168.30.11-8080-2035", td.getName());
    }

    @Test
    public void testValidDefinition_QuotesDoNotClose() throws Exception {

        String s =
            "\"http-192.168.30.11-8080-2035\n" +
            "   java.lang.Thread.State: BLOCKED (on object monitor)\n" +
            "\tat java.lang.Throwable.printStackTrace(Throwable.java:460)";

        StackTrace td = new StackTrace();

        String line;
        BufferedReader br = new BufferedReader(new StringReader(s));
        while((line = br.readLine()) != null)
        {
            td.append(line, -1);
        }

        assertFalse(td.isValid());
        assertNull(td.getName());
    }


    // match() ---------------------------------------------------------------------------------------------------------

    @Test
    public void testMatch() throws Exception {

        StackTrace td = new StackTrace();

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

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------
}
