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


import io.novaordis.tda.Command;
import io.novaordis.tda.SimplifiedLogger;
import io.novaordis.tda.ThreadDump;
import io.novaordis.tda.ThreadDumpFile;
import io.novaordis.utilities.UserErrorException;

import java.io.File;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

public class Split implements Command {

    // Constants -------------------------------------------------------------------------------------------------------

    public static final Format TIMESTAMP_PREFIX = new SimpleDateFormat("yyyy.MM.dd-HH.mm.ss");

    public static final String LITERAL = "split";

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private SimplifiedLogger log;

    private ThreadDumpFile threadDumpFile;

    // Constructors ----------------------------------------------------------------------------------------------------

    public Split(SimplifiedLogger log, String[] args) throws Exception {

        this.log = log;

        for (String arg : args) {

            if (arg.startsWith("-")) {

                throw new UserErrorException("unknown split option: '" + arg + "'");
            }
            else if (threadDumpFile == null) {

                threadDumpFile = new ThreadDumpFile(arg);
            }
        }

        if (threadDumpFile == null) {

            throw new UserErrorException("no filename provided");
        }
    }

    // Command implementation ------------------------------------------------------------------------------------------

    public void run() throws Exception {

        split(threadDumpFile);
    }

    // Public ----------------------------------------------------------------------------------------------------------

    public File getFile() {

        if (threadDumpFile == null) {

            return null;
        }

        return threadDumpFile.getFile();
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    private void split(ThreadDumpFile f) throws Exception {

        if (f.getCount() == 1) {

            log.info("one thread dump in file, nothing to split");
            return;
        }

        int counter = 0;

        for(Iterator<ThreadDump> i = f.iterator(); i.hasNext(); ) {

            ThreadDump td = i.next();

            Date timestamp = td.getTimestamp();

            File splitFile = generateSplitFile(counter ++, timestamp, f.getFile());

            log.info("writing " + splitFile.getName());
            td.toFile(splitFile);
        }
    }

    /**
     * @param timestamp may be null, in which case only the counter should be used. If not null, both the counter and
     *                  timestamp will be used.
     */
    private File generateSplitFile(int counter, Date timestamp, File f) throws Exception {

        String s = Integer.toString(counter);

        if (counter < 10) {

            s = "0" + s;
        }

        if (timestamp != null) {

            s += "-";
            s += TIMESTAMP_PREFIX.format(timestamp);
        }

        return generateSplitFile(s, f);
    }

    private File generateSplitFile(String prefix, File f) throws Exception {

        //File parent = f.getParentFile();

        String name = f.getName();
        String base = name;
        String ext = "";

        int i = name.lastIndexOf(".");

        if (i != -1) {

            base = name.substring(0, i);
            ext = "." + name.substring(i + 1);
        }

        return new File(prefix + "-" + base + "-thread-dump" + ext);
    }

    // Inner classes ---------------------------------------------------------------------------------------------------

}
