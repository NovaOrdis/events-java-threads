package com.novaordis.universus.tdanalyzer;

import com.novaordis.universus.tdanalyzer.command.Grep;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;

/**
 * @author <a href="mailto:ovidiu@novaordis.com">Ovidiu Feodorov</a>
 *
 * Copyright 2010 Ovidiu Feodorov
 */

public class GrepConfigurationTest extends Assert
{
    // Constants -----------------------------------------------------------------------------------

    private static final Logger log = Logger.getLogger(GrepConfigurationTest.class);

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    // Public --------------------------------------------------------------------------------------

    @Test
    public void testConfiguration_Regexp() throws Exception
    {
        String[] args = new String[] {"blah", "./src/test/resources/samples/000.txt"};
        Grep g = new Grep(null, args);

        assertFalse(g.isExclude());
        assertFalse(g.isCount());
        assertEquals("blah", g.getRegex());
        assertEquals(new File("./src/test/resources/samples/000.txt"), g.getFile());
    }

    @Test
    public void testConfiguration_Count_NoRegexp() throws Exception
    {
        String[] args = new String[] {"-c", "./src/test/resources/samples/000.txt"};
        Grep g = new Grep(null, args);

        assertFalse(g.isExclude());
        assertTrue(g.isCount());
        assertNull(g.getRegex());
        assertEquals(new File("./src/test/resources/samples/000.txt"), g.getFile());
    }

    @Test
    public void testConfiguration_Count_RegexpPresent() throws Exception
    {
        String[] args = new String[] {"-c", "blah", "./src/test/resources/samples/000.txt"};
        Grep g = new Grep(null, args);

        assertFalse(g.isExclude());
        assertTrue(g.isCount());
        assertEquals("blah", g.getRegex());
        assertEquals(new File("./src/test/resources/samples/000.txt"), g.getFile());

        args = new String[] {"blah", "-c", "./src/test/resources/samples/000.txt"};
        g = new Grep(null, args);

        assertFalse(g.isExclude());
        assertTrue(g.isCount());
        assertEquals("blah", g.getRegex());
        assertEquals(new File("./src/test/resources/samples/000.txt"), g.getFile());
    }

    @Test
    public void testConfiguration_Exclude_NoRegexp() throws Exception
    {
        String[] args = new String[] {"-v", "./src/test/resources/samples/000.txt"};

        try
        {
            new Grep(null, args);
            fail("should fail, combination does not make sense");
        }
        catch(UserException e)
        {
            log.info(e.getMessage());
        }
    }

    @Test
    public void testConfiguration_Exclude_RegexpPresent() throws Exception
    {
        String[] args = new String[] {"-v", "blah", "./src/test/resources/samples/000.txt"};
        Grep g = new Grep(null, args);

        assertTrue(g.isExclude());
        assertFalse(g.isCount());
        assertEquals("blah", g.getRegex());
        assertEquals(new File("./src/test/resources/samples/000.txt"), g.getFile());

        args = new String[] {"blah", "-v", "./src/test/resources/samples/000.txt"};
        g = new Grep(null, args);

        assertTrue(g.isExclude());
        assertFalse(g.isCount());
        assertEquals("blah", g.getRegex());
        assertEquals(new File("./src/test/resources/samples/000.txt"), g.getFile());
    }

    @Test
    public void testConfiguration_Count_Exclude_NoRegexp() throws Exception
    {
        String[] args = new String[] {"-c", "-v", "./src/test/resources/samples/000.txt"};

        try
        {
            new Grep(null, args);
            fail("should fail, combination does not make sense");
        }
        catch(UserException e)
        {
            log.info(e.getMessage());
        }
    }

    @Test
    public void testConfiguration_Count_Exclude_RegexpPresent() throws Exception
    {
        String[] args = new String[] {"-v", "-c", "blah", "./src/test/resources/samples/000.txt"};
        Grep g = new Grep(null, args);

        assertTrue(g.isExclude());
        assertTrue(g.isCount());
        assertEquals("blah", g.getRegex());
        assertEquals(new File("./src/test/resources/samples/000.txt"), g.getFile());

        args = new String[] {"-c", "blah", "-v", "./src/test/resources/samples/000.txt"};
        g = new Grep(null, args);

        assertTrue(g.isExclude());
        assertTrue(g.isCount());
        assertEquals("blah", g.getRegex());
        assertEquals(new File("./src/test/resources/samples/000.txt"), g.getFile());

        args = new String[] {"blah", "-v", "-c", "./src/test/resources/samples/000.txt"};
        g = new Grep(null, args);

        assertTrue(g.isExclude());
        assertTrue(g.isCount());
        assertEquals("blah", g.getRegex());
        assertEquals(new File("./src/test/resources/samples/000.txt"), g.getFile());
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
