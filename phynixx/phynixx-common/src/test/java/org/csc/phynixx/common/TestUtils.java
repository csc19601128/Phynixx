package org.csc.phynixx.common;

/*
 * #%L
 * phynixx-common
 * %%
 * Copyright (C) 2014 csc
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */



public class TestUtils {

    public static void sleep(long msecs) {
        long start = System.currentTimeMillis();
        long waiting = msecs;
        while (waiting > 0) {
            try {
                Thread.currentThread();
                Thread.sleep(waiting);
            } catch (InterruptedException e) {
            } finally {
                waiting = msecs - (System.currentTimeMillis() - start);
            }
        }
    }

    public static void configureLogging() {
        configureLogging("ERROR");
    }

    public static void configureLogging(String log4jLevel) {

    }

}
