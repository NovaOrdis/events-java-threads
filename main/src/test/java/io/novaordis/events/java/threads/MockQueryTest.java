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

package io.novaordis.events.java.threads;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 11/30/17
 */
public class MockQueryTest {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    // Tests -----------------------------------------------------------------------------------------------------------

    @Test
    public void selects_Time() throws Exception {

        MockQuery q = new MockQuery();

        assertTrue(q.selects(Long.MIN_VALUE));
        assertTrue(q.selects(-1L * System.currentTimeMillis()));
        assertTrue(q.selects(-1L));
        assertTrue(q.selects(0L));
        assertTrue(q.selects(1L));
        assertTrue(q.selects(System.currentTimeMillis()));
        assertTrue(q.selects(Long.MAX_VALUE));
    }

    @Test
    public void selects_From() throws Exception {

        MockQuery q = new MockQuery();
        q.setFrom(10L);

        assertFalse(q.selects(Long.MIN_VALUE));
        assertFalse(q.selects(-1L * System.currentTimeMillis()));
        assertFalse(q.selects(-1L));
        assertFalse(q.selects(0L));
        assertFalse(q.selects(9L));
        assertTrue(q.selects(10L));
        assertTrue(q.selects(11L));
        assertTrue(q.selects(System.currentTimeMillis()));
        assertTrue(q.selects(Long.MAX_VALUE));
    }

    @Test
    public void selects_From_To() throws Exception {

        MockQuery q = new MockQuery();
        q.setFrom(10L);
        q.setTo(12L);

        assertFalse(q.selects(Long.MIN_VALUE));
        assertFalse(q.selects(-1L * System.currentTimeMillis()));
        assertFalse(q.selects(-1L));
        assertFalse(q.selects(0L));
        assertFalse(q.selects(9L));
        assertTrue(q.selects(10L));
        assertTrue(q.selects(11L));
        assertTrue(q.selects(12L));
        assertFalse(q.selects(13L));
        assertFalse(q.selects(System.currentTimeMillis()));
        assertFalse(q.selects(Long.MAX_VALUE));
    }

    @Test
    public void selects_To() throws Exception {

        MockQuery q = new MockQuery();
        q.setTo(12L);

        assertTrue(q.selects(Long.MIN_VALUE));
        assertTrue(q.selects(-1L * System.currentTimeMillis()));
        assertTrue(q.selects(-1L));
        assertTrue(q.selects(0L));
        assertTrue(q.selects(9L));
        assertTrue(q.selects(10L));
        assertTrue(q.selects(11L));
        assertTrue(q.selects(12L));
        assertFalse(q.selects(13L));
        assertFalse(q.selects(System.currentTimeMillis()));
        assertFalse(q.selects(Long.MAX_VALUE));
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
