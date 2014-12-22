package org.csc.phynixx.common.exceptions;

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


import java.io.PrintWriter;
import java.io.StringWriter;


/**
 * @author christoph
 */
public class ExceptionUtils {

    protected ExceptionUtils() {
    }


    /**
     * @param throwable Throwable
     * @return stack trace
     * @since JDK 1.2
     */
    public static String getStackTrace(Throwable throwable) {
        if (throwable == null) {
            return "";
        }
        StringWriter swrt = new StringWriter();
        PrintWriter pwrt = new PrintWriter(swrt);
        throwable.printStackTrace(pwrt);
        return swrt.toString();
    }


    /**
     * @return stack trace
     * @since JDK 1.2
     */
    public static String getStackTrace() {
        StringWriter swrt = new StringWriter();
        PrintWriter pwrt = new PrintWriter(swrt);
        new Exception().printStackTrace(pwrt);
        return swrt.toString();
    }


}
