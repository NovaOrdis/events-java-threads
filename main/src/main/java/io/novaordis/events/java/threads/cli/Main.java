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

package io.novaordis.events.java.threads.cli;

import io.novaordis.events.api.parser.Parser;
import io.novaordis.events.cli.EventParserRuntime;
import io.novaordis.events.processing.ProcedureFactory;
import io.novaordis.utilities.UserErrorException;
import io.novaordis.events.java.threads.TDProcedureFactory;
import io.novaordis.events.java.threads.JavaThreadDumpParser;
import io.novaordis.utilities.appspec.ApplicationSpecificBehavior;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 8/14/17
 */
public class Main {

    // Constants -------------------------------------------------------------------------------------------------------

    public static final String APPLICATION_NAME = "td";

    // Static ----------------------------------------------------------------------------------------------------------

    public static void main(String[] args) throws Exception {

        try {

            Parser parser = new JavaThreadDumpParser();

            ProcedureFactory procedureFactory = new TDProcedureFactory();

            ApplicationSpecificBehavior b = new ApplicationSpecificBehavior(parser, procedureFactory);

            EventParserRuntime runtime = new EventParserRuntime(args, APPLICATION_NAME, b);

            if (runtime.getConfiguration().isHelp()) {

                runtime.displayHelp(APPLICATION_NAME, System.out);
                return;
            }

            runtime.run();

        } catch (UserErrorException e) {

            System.err.println(e.getMessage());
        }
    }

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
