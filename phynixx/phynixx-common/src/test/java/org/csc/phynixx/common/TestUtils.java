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


import org.apache.log4j.PropertyConfigurator;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class TestUtils {

    public static void sleep(long msecs) {
        long start = System.currentTimeMillis();
        long waiting = msecs;
        while (waiting > 0) {
            try {
                Thread.currentThread().sleep(waiting);
            } catch (InterruptedException e) {
            } finally {
                waiting = msecs - (System.currentTimeMillis() - start);
            }
        }
    }


    public static void configureLogging() {
        Set<String> validLogLevels = new HashSet<String>();
        validLogLevels.add("DEBUG");
        validLogLevels.add("INFO");
        validLogLevels.add("WARNING");
        validLogLevels.add("ERROR");
        validLogLevels.add("FATAL");

        Properties log4jProps = new Properties();
        InputStream io = Thread.currentThread().getContextClassLoader().getResourceAsStream("log4j.properties");
        if (io == null) {
            throw new IllegalStateException("log4j.properties not found in ClassPath for testing ");
        }
        try {
            log4jProps.load(io);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Reinitialize the logger and substitute any environment pros
        Properties props = System.getProperties();
        // copy all props beginning with log4j
        Iterator<Map.Entry<Object,Object>> iter = props.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<Object,Object> entry = iter.next();
            String propName = (String) entry.getKey();
            if (propName.startsWith("log4j")) {
                log4jProps.put(entry.getKey(), entry.getValue());
            }
        }

        // check if log4j.level is set correctly ; else set the Default to INFO
        String level = log4jProps.getProperty("log4j_level");
        if (level == null || !validLogLevels.contains(level)) {
            level = "ERROR";
            log4jProps.setProperty("log4j_level", level);
        }

        org.apache.log4j.LogManager.resetConfiguration();

        PropertyConfigurator.configure(log4jProps);

    }

}
