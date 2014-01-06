package org.csc.phynixx.exceptions;

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


public class SampleTransactionalException extends RuntimeException {

    /**
     *
     */
    private static final long serialVersionUID = -7037433162290063313L;

    public SampleTransactionalException(String string) {
        super(string);
    }

    public SampleTransactionalException() {
        super();
    }

    public SampleTransactionalException(String message, Throwable cause) {
        super(message, cause);
    }

    public SampleTransactionalException(Throwable cause) {
        super(cause);
    }

}
