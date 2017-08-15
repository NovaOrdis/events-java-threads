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

    RUNNABLE("runnable"),

    //
    // if a thread is " in Object.wait() [0x00007f6209147000]", the monitor it is waiting on will may be available
    // as the value of the object-wait-monitor property.
    //

    OBJECT_WAIT("in Object.wait()"),

    WAITING_ON_CONDITION("waiting on condition"),

    SLEEPING("sleeping"),

    ;

    // Static ----------------------------------------------------------------------------------------------------------

    /**
     * Recognizes a thread state representation in a string and builds the corresponding instance. It is guaranteed
     * to work with the literals and also with the toString() values.
     *
     * @return null if no known thread state is identified
     */
    static ThreadState fromString(String s) {

        for(ThreadState ts: values()) {

            String literal = ts.getLiteral();

            if (s.contains(literal)) {

                return ts;
            }

            if (ts.toString().equals(s)) {

                return ts;
            }
        }

        return null;
    }

    static void setMonitor(StackTraceEvent e, String threadStateRepresentation) {

        if (threadStateRepresentation == null) {

            return;
        }

        int i = threadStateRepresentation.indexOf('[');

        if (i != -1) {

            int j = threadStateRepresentation.indexOf(']', i);

            if (j != -1) {

                e.setObjectWaitMonitor(threadStateRepresentation.substring(i + 1, j));
            }
        }
    }

    // Attributes ------------------------------------------------------------------------------------------------------

    private String literal;

    // Constructors ----------------------------------------------------------------------------------------------------

    ThreadState(String literal) {

        this.literal = literal;
    }

    // Public ----------------------------------------------------------------------------------------------------------

    /**
     * The string representation of the state in thread dumps.
     */
    public String getLiteral() {

        return literal;
    }

}
