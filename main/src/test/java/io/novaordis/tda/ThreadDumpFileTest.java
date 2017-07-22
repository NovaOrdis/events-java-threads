package io.novaordis.tda;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import java.io.FileNotFoundException;

/**
 * @author <a href="mailto:ovidiu@novaordis.com">Ovidiu Feodorov</a>
 *
 * Copyright 2010 Ovidiu Feodorov
 */

public class ThreadDumpFileTest extends Assert
{
    // Constants -----------------------------------------------------------------------------------

    private static final Logger log = Logger.getLogger(ThreadDumpFileTest.class);

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    // Public --------------------------------------------------------------------------------------

    @Test
    public void testCtor_NoFile() throws Exception
    {
        try
        {
            new ThreadDumpFile("/no/such/file/obviously");
            fail("should have failed with FileNotFoundException");
        }
        catch(FileNotFoundException e)
        {
            log.info(e.getMessage());
        }
    }

    @Test
    public void testCtor_TimestampAndHeader() throws Exception
    {
        ThreadDumpFile tdf = new ThreadDumpFile(
            "./src/test/resources/samples/010_thread_dump_file_timestamp_header.txt");

        assertEquals(1, tdf.getCount());

        ThreadDump td = tdf.get(0);
        assertEquals(ThreadDumpFile.TIMESTAMP_FORMAT.parseObject("2010-12-14 01:02:03"),
                     td.getTimestamp());
        assertEquals("Full thread dump Java HotSpot(TM) 64-Bit Server VM (16.3-b01 mixed mode):",
                     td.getHeader());

        assertEquals(2, td.getThreadCount());
    }

    @Test
    public void testCtor_JustHeader() throws Exception
    {
        ThreadDumpFile tdf = new ThreadDumpFile(
            "./src/test/resources/samples/011_thread_dump_header_no_timestamp.txt");

        assertEquals(1, tdf.getCount());

        ThreadDump td = tdf.get(0);
        assertNull(td.getTimestamp());
        assertEquals("Full thread dump Java HotSpot(TM) 64-Bit Server VM (16.3-b01 mixed mode):",
                     td.getHeader());
        assertEquals(2, td.getThreadCount());
    }

    @Test
    public void testCtor_TwoThreadDumps() throws Exception
    {
        ThreadDumpFile tdf =
            new ThreadDumpFile("./src/test/resources/samples/012_two_thread_dumps.txt");

        assertEquals(2, tdf.getCount());

        ThreadDump td = tdf.get(0);
        assertEquals(ThreadDumpFile.TIMESTAMP_FORMAT.parseObject("2010-12-14 01:02:03"),
                     td.getTimestamp());
        assertEquals("Full thread dump Java HotSpot(TM) 64-Bit Server VM (16.3-b05 mixed mode):",
                     td.getHeader());
        assertEquals(2, td.getThreadCount());

        td = tdf.get(1);
        assertEquals(ThreadDumpFile.TIMESTAMP_FORMAT.parseObject("2011-01-02 03:04:05"),
                     td.getTimestamp());
        assertEquals("Full thread dump Java HotSpot(TM) 64-Bit Server VM (16.3-b11 mixed mode):",
                     td.getHeader());
        assertEquals(3, td.getThreadCount());
    }

    @Test
    public void testCtor_FullThreadDumpOnFirstLine() throws Exception
    {
        // this is NOT a fragment, but an incomplete thread dump

        try
        {
            new ThreadDumpFile("./src/test/resources/samples/002_1_FullThreadDumpOnFirstLine.txt");
            fail("should have failed with UserErrorException");
        }
        catch(UserErrorException e)
        {
            log.info(e.getMessage());
            assertEquals(1, e.getLineNumber());
        }
    }

    @Test
    public void testCtor_FullThreadDumpOnFirstLine_2() throws Exception
    {
        ThreadDumpFile tdf =
            new ThreadDumpFile("./src/test/resources/samples/002_2_FullThreadDumpOnFirstLine.txt");

        assertEquals(1, tdf.getCount());
        ThreadDump td = tdf.get(0);

        assertNull(td.getTimestamp());
        assertEquals("Full thread dump Java HotSpot(TM) 64-Bit Server VM (16.3-b71 mixed mode):",
                     td.getHeader());

        assertEquals(2, td.getThreadCount());
    }

    @Test
    public void testCtor_FullThreadDumpOnInvalidTimestamp() throws Exception
    {
        // this is NOT a fragment, but an incomplete thread dump

        try
        {
            new ThreadDumpFile("./src/test/resources/samples/003_FullThreadDumpInvalidTimestamp.txt");
            fail("should have failed with UserErrorException");
        }
        catch(UserErrorException e)
        {
            log.info(e.getMessage());
            assertEquals(5, e.getLineNumber());
        }
    }

    @Test
    public void testCtor_NoEmptyLineAfterHeader() throws Exception
    {
        try
        {
            new ThreadDumpFile("./src/test/resources/samples/005_NoEmptyLine.txt");
            fail("should have failed with UserErrorException");
        }
        catch(UserErrorException e)
        {
            log.info(e.getMessage());
            assertEquals(9, e.getLineNumber());
        }
    }

    @Test
    public void testCtor_Minimal() throws Exception
    {
        ThreadDumpFile tdf = new ThreadDumpFile("./src/test/resources/samples/004_Minimal.txt");

        assertEquals(1, tdf.getCount());
        ThreadDump td = tdf.get(0);

        assertEquals(ThreadDumpFile.TIMESTAMP_FORMAT.parseObject("2011-09-09 15:16:17"),
                     td.getTimestamp());
        assertEquals("Full thread dump Java HotSpot(TM) 64-Bit Server VM (15.3-b01 mixed mode):",
                     td.getHeader());
        assertEquals(2, td.getThreadCount());
    }

    @Test
    public void testCtor_Real_001() throws Exception
    {
        ThreadDumpFile tdf = new ThreadDumpFile("./src/test/resources/samples/001.txt");

        assertEquals(1, tdf.getCount());
        ThreadDump td = tdf.get(0);

        assertEquals(ThreadDumpFile.TIMESTAMP_FORMAT.parseObject("2011-10-04 00:09:02"),
                     td.getTimestamp());

        assertEquals("Full thread dump Java HotSpot(TM) 64-Bit Server VM (16.3-b01 mixed mode):",
                     td.getHeader());

        assertEquals(786, td.getThreadCount());
    }

    @Test
    public void testFilteredFragment_NoCR() throws Exception
    {
        ThreadDumpFile tdf =
            new ThreadDumpFile("./src/test/resources/samples/013_filtered_fragment_no_CR.txt");

        assertEquals(1, tdf.getCount());
        ThreadDump td = tdf.get(0);

        assertNull(td.getTimestamp());
        assertNull(td.getHeader());
        assertEquals(1, td.getThreadCount());
    }

    @Test
    public void testFilteredFragment() throws Exception
    {
        ThreadDumpFile tdf =
            new ThreadDumpFile("./src/test/resources/samples/014_filtered_fragment.txt");

        assertEquals(1, tdf.getCount());
        ThreadDump td = tdf.get(0);

        assertNull(td.getTimestamp());
        assertNull(td.getHeader());
        assertEquals(2, td.getThreadCount());
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
