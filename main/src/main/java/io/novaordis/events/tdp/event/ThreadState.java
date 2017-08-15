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

package io.novaordis.events.tdp.event;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 8/15/17
 */
public enum ThreadState {

    // Constants -------------------------------------------------------------------------------------------------------

    RUNNABLE,

    ;

    // Static ----------------------------------------------------------------------------------------------------------

    /**
     * @return null if no known thread state is identified
     */
    static ThreadState fromString(String s) {

        for(ThreadState ts: values()) {

            if (ts.toString().equalsIgnoreCase(s)) {

                return ts;
            }
        }

        return null;
    }

    // Public ----------------------------------------------------------------------------------------------------------

}
