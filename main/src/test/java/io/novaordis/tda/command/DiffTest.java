/*
 * Copyright (c) 2017 Nova Ordis LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.novaordis.tda.command;

import io.novaordis.tda.MockSimplifiedLogger;
import io.novaordis.tda.UserErrorException;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 4/26/17
 */
public class DiffTest {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    // Tests -----------------------------------------------------------------------------------------------------------

    // constructor -----------------------------------------------------------------------------------------------------

    @Test
    public void constructor_MissingFirstFile() throws Exception {

        MockSimplifiedLogger ml = new MockSimplifiedLogger();

        String[] args = new String[] {};

        try {

            new Diff(ml, args);
            fail("should have thrown exception");
        }
        catch(UserErrorException e) {

            String msg = e.getMessage();
            assertTrue(msg.contains("two files to diff must be specified"));
        }
    }

    @Test
    public void constructor_MissingSecondFile() throws Exception {

        MockSimplifiedLogger ml = new MockSimplifiedLogger();

        String[] args = new String[] { "first-file.txt" };

        try {

            new Diff(ml, args);
            fail("should have thrown exception");
        }
        catch(UserErrorException e) {

            String msg = e.getMessage();
            assertTrue(msg.contains("two files to diff must be specified"));
        }
    }

    @Test
    public void constructor_NoSuchFile() throws Exception {

        MockSimplifiedLogger ml = new MockSimplifiedLogger();

        String[] args = new String[] { "no-such-file.txt", "no-such-file-2.txt" };

        try {

            new Diff(ml, args);
            fail("should have thrown exception");
        }
        catch(UserErrorException e) {

            String msg = e.getMessage();
            assertTrue(msg.contains("no such file"));
            assertTrue(msg.contains("no-such-file.txt"));
        }
    }

    @Test
    public void constructor_MultipleThreadDumps() throws Exception {

        MockSimplifiedLogger ml = new MockSimplifiedLogger();

        File f = new File(System.getProperty("basedir"), "src/test/resources/samples/012_two_thread_dumps.txt");
        assertTrue(f.isFile());

        File f2 = new File(System.getProperty("basedir"), "src/test/resources/samples/000.txt");
        assertTrue(f2.isFile());

        String[] args = new String[] { f.getPath(), f2.getPath() };

        try {

            new Diff(ml, args);
            fail("should have thrown exception");
        }
        catch(UserErrorException e) {

            String msg = e.getMessage();
            assertTrue(msg.contains("we can only diff files that contain just one thread dump"));
            assertTrue(msg.contains("012_two_thread_dumps.txt"));
        }
    }

    @Test
    public void constructor_MultipleThreadDumps2() throws Exception {

        MockSimplifiedLogger ml = new MockSimplifiedLogger();

        File f = new File(System.getProperty("basedir"), "src/test/resources/samples/000.txt");
        assertTrue(f.isFile());

        File f2 = new File(System.getProperty("basedir"), "src/test/resources/samples/012_two_thread_dumps.txt");
        assertTrue(f2.isFile());

        String[] args = new String[] { f.getPath(), f2.getPath() };

        try {

            new Diff(ml, args);
            fail("should have thrown exception");
        }
        catch(UserErrorException e) {

            String msg = e.getMessage();
            assertTrue(msg.contains("we can only diff files that contain just one thread dump"));
            assertTrue(msg.contains("012_two_thread_dumps.txt"));
        }
    }

    @Test
    public void constructor() throws Exception {

        MockSimplifiedLogger ml = new MockSimplifiedLogger();

        File f = new File(System.getProperty("basedir"), "src/test/resources/samples/000.txt");
        assertTrue(f.isFile());

        File f2 = new File(System.getProperty("basedir"), "src/test/resources/samples/001.txt");
        assertTrue(f2.isFile());

        String[] args = new String[] { f.getPath(), f2.getPath() };

        Diff d = new Diff(ml, args);

        assertEquals(f, d.getFile());
        assertEquals(f2, d.getFile2());
    }

    // run() -----------------------------------------------------------------------------------------------------------

    @Test
    public void run() throws Exception {

        MockSimplifiedLogger ml = new MockSimplifiedLogger();

        File f = new File(System.getProperty("basedir"), "src/test/resources/samples/000.txt");
        assertTrue(f.isFile());

        File f2 = new File(System.getProperty("basedir"), "src/test/resources/samples/001.txt");
        assertTrue(f2.isFile());

        String[] args = new String[] { f.getPath(), f2.getPath() };

        Diff d = new Diff(ml, args);

        d.run();

        String info = ml.getInfo();

        int i = info.indexOf("only in");
        assertTrue(i != -1);
        i = info.indexOf("only in", i + "only in".length());
        assertEquals(i, -1);

        assertTrue(info.contains("only in " + f2 + ":"));

    }

    // onlyInFirst() ---------------------------------------------------------------------------------------------------

    @Test
    public void onlyInFirst() throws Exception {

        List<String> first = new ArrayList<>(Arrays.asList("A", "L", "X"));
        List<String> second = new ArrayList<>(Arrays.asList("B", "L", "Z"));

        List<String> onlyInFirst = Diff.onlyInFirst(first, second);
        assertEquals(2, onlyInFirst.size());
        assertEquals("A", onlyInFirst.get(0));
        assertEquals("X", onlyInFirst.get(1));

        List<String> onlyInSecond = Diff.onlyInFirst(second, first);
        assertEquals(2, onlyInFirst.size());
        assertEquals("B", onlyInSecond.get(0));
        assertEquals("Z", onlyInSecond.get(1));

    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
