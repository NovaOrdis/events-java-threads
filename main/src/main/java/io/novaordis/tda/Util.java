/*
 * Copyright (c) 2016 Nova Ordis LLC
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

package io.novaordis.tda;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 6/28/16
 */
public class Util {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    public static String[] coalesceQuotedStrings(String[] args) throws UserErrorException {

        List<String> result = null;
        String quotedString = null;

        for(int i = 0; i < args.length; i ++) {

            String arg = args[i];

            if (arg.contains("\"")) {

                if (result == null) {

                    result = new ArrayList<>();
                    result.addAll(Arrays.asList(args).subList(0, i));
                }

                if (arg.startsWith("\"")) {

                    if (quotedString != null) {
                        throw new UserErrorException("unbalanced quotes: " + arg);
                    }

                    quotedString = arg.substring(1);
                    continue;

                }
                else if (arg.endsWith("\"")) {

                    if (quotedString == null) {
                        throw new UserErrorException("unbalanced quotes: " + arg);
                    }

                    arg = arg.substring(0, arg.length() - 1);
                    result.add(quotedString + " " + arg);
                    quotedString = null;
                    continue;
                }
                else {
                    throw new RuntimeException("support for \" in the middle of an argument not yet implemented");
                }
            }

            if (quotedString != null) {

                // TODO: problem - how do we capture multiple spaces inside quotes
                quotedString += " " + arg;
            }
            else if (result != null) {
                result.add(arg);
            }
        }

        if (result == null) {
            return args;
        }

        return result.toArray(new String[result.size()]);
    }

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    private Util() {
    }

    // Public ----------------------------------------------------------------------------------------------------------

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
