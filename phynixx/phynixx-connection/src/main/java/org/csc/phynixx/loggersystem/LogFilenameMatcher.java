package org.csc.phynixx.loggersystem;

/*
 * #%L
 * phynixx-connection
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


import java.util.regex.Matcher;
import java.util.regex.Pattern;

class LogFilenameMatcher {

    public static class LogFilenameParts {
        private String loggerName = null;
        private int logfileIndex = -1;

        private LogFilenameParts(String loggerName, int logfileIndex) {
            super();
            this.loggerName = loggerName;
            this.logfileIndex = logfileIndex;
        }

        public String getLoggerName() {
            return loggerName;
        }

        public int getLogfileIndex() {
            return logfileIndex;
        }

    }


    private java.util.regex.Pattern regexPattern = null;

    public LogFilenameMatcher(String pattern) {
        this.regexPattern = Pattern.compile(pattern);
    }


    public LogFilenameParts matches(String input) {

        Matcher m = this.regexPattern.matcher(input);

        if (!m.matches()) {
            return null;
        }

        Matcher result = m; // -- JDK 1.4

        // JDK 1.5 MatchResult result= m.toMatchResult();
        String g1 = result.group(1);
        String g2 = result.group(2);


        return new LogFilenameParts(g1, Integer.parseInt(g2));
    }

}
