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

package io.novaordis.events.tdp;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 4/26/17
 */
public class MockSimplifiedLogger implements SimplifiedLogger {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private String info;
    private String error;

    // Constructors ----------------------------------------------------------------------------------------------------

    // SimplifiedLogger implementation ---------------------------------------------------------------------------------

    @Override
    public void info(String s) {

        if (info == null) {

            info = "";
        }

        info += s + "\n";
    }

    @Override
    public void error(String s) {

        if (error == null) {

            error = "";
        }

        error += s + "\n";
    }

    // Public ----------------------------------------------------------------------------------------------------------

    /**
     * @return may return null if info() was not invoked.
     */
    public String getInfo() {

        return info;
    }

    /**
     * @return may return null if error() was not invoked.
     */
    public String getError() {

        return error;
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
