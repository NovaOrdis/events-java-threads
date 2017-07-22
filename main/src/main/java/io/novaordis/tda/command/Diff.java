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
import io.novaordis.tda.StackTrace;
import io.novaordis.tda.ThreadDump;
import io.novaordis.tda.ThreadDumpFile;
import io.novaordis.tda.UserErrorException;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class Diff implements Command {

    // Constants -------------------------------------------------------------------------------------------------------

    public static final String LITERAL = "diff";

    // Static ----------------------------------------------------------------------------------------------------------

    /**
     * @return a list of strings only found in the first list, in the order in which they occur. May return an empty
     * list, never null.
     */
    public static List<String> onlyInFirst(List<String> list, List<String> list2) {

        List<String> result = null;

        for(String s: list) {

            if (!list2.contains(s)) {

                if (result == null) {

                    result = new ArrayList<>();
                }

                result.add(s);
            }

        }

        if (result == null) {

            return Collections.emptyList();
        }

        return result;
    }

    // Attributes ------------------------------------------------------------------------------------------------------

    private SimplifiedLogger log;

    private File file, file2;

    private ThreadDumpFile tdFile, tdFile2;

    // Constructors ----------------------------------------------------------------------------------------------------

    public Diff(SimplifiedLogger log, String[] args) throws Exception {

        this.log = log;

        if (args.length < 2) {

            throw new UserErrorException("two files to diff must be specified");
        }

        file = new File(args[0]);

        try {

            tdFile = new ThreadDumpFile(file);
        }
        catch(FileNotFoundException e) {

            throw new UserErrorException("no such file " + file);
        }

        if (tdFile.getCount() != 1) {

            throw new UserErrorException(
                    "we can only diff files that contain just one thread dump, but " + file +
                            " contains " + tdFile.getCount());
        }

        file2 = new File(args[1]);

        try {

            tdFile2 = new ThreadDumpFile(file2);
        }
        catch(FileNotFoundException e) {

            throw new UserErrorException("no such file " + file2);
        }

        if (tdFile2.getCount() != 1) {

            throw new UserErrorException(
                    "we can only diff files that contain just one thread dump, but " + file2 +
                            " contains " + tdFile2.getCount());
        }

    }

    // Command implementation ------------------------------------------------------------------------------------------

    public void run() throws Exception {

        List<String> threadNames = new ArrayList<>(), threadNames2 = new ArrayList<>();

        //
        // the constructor did take care already that we only have a thread dump per file
        //

        ThreadDump d = tdFile.iterator().next();
        ThreadDump d2 = tdFile2.iterator().next();

        for(Iterator<StackTrace> i = d.iterator(); i.hasNext(); ) {

            StackTrace st = i.next();
            String name = st.getName();
            threadNames.add(name);
        }

        for(Iterator<StackTrace> i = d2.iterator(); i.hasNext(); ) {

            StackTrace td = i.next();
            String name = td.getName();
            threadNames2.add(name);
        }

        List<String> onlyInFirst = onlyInFirst(threadNames, threadNames2);
        List<String> onlyInSecond = onlyInFirst(threadNames2, threadNames);

        if (!onlyInFirst.isEmpty()) {

            log.info("only in " + file + ":");

            for(String s: onlyInFirst) {

                log.info("  " + s);
            }

            log.info("");
        }

        if (!onlyInSecond.isEmpty()) {

            log.info("only in " + file2 + ":");

            for(String s: onlyInSecond) {

                log.info("  " + s);
            }

            log.info("");
        }

    }

    // Public ----------------------------------------------------------------------------------------------------------

    public File getFile() {

        return file;
    }

    public File getFile2() {

        return file2;
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
