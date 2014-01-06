/*
 * JOnAS: Java(TM) Open Application Server
 * Copyright (C) 2004 Bull S.A.
 * All rights reserved.
 * 
 * Contact: howl@objectweb.org
 * 
 * This software is licensed under the BSD license.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 *   * Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *     
 *   * Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *     
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * ------------------------------------------------------------------------------
 * $Id: LogRecordType.java,v 1.6 2005/06/23 23:28:14 girouxm Exp $
 * ------------------------------------------------------------------------------
 */
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


/**
 * Define record types used by Logger implementations.
 * <p/>
 * contains the constants needed by the xaresource specific implementation of a howl logger
 *
 * @author Christoph Schmidt-Casdorff
 */
public class XALogRecordType {

    /**
     * recorded by XALogger to mark records
     * generated by XALogger#putDone()
     */
    public static final short UNKNOWN_TYPE = -1;

    /**
     * Log records generated by user.
     */
    public final static short USER_TYPE = 0;

    /**
     * recorded by XALogger to mark records as prepared
     */
    public static final short XA_PREPARED_TYPE = 0x0001;

    /**
     * recorded by Logger to mark first block following a
     * restart of the Logger.
     */
    public static final short XA_START_TYPE = 0x0002;

    /**
     * recorded by XALogger to mark records
     * generated by XALogger#putCommit()
     */
    public static final short XA_COMMIT_TYPE = 0x0003;

    /**
     * recorded by XALogger to mark records
     * generated by XALogger#putDone()
     */
    public static final short XA_DONE_TYPE = 0x0004;


    public static final XALogRecordType USER = new XALogRecordType(USER_TYPE, "USER");
    public static final XALogRecordType XA_COMMIT = new XALogRecordType(XA_COMMIT_TYPE, "XA_COMMIT");
    public static final XALogRecordType XA_DONE = new XALogRecordType(XA_DONE_TYPE, "XA_DONE");
    public static final XALogRecordType XA_PREPARED = new XALogRecordType(XA_PREPARED_TYPE, "XA_PREPARED");
    public static final XALogRecordType XA_START = new XALogRecordType(XA_START_TYPE, "XA_START");

    public static final XALogRecordType UNKNOWN = new XALogRecordType(UNKNOWN_TYPE, "?");

    public static XALogRecordType resolve(short type) {

        switch (type) {
            case XA_START_TYPE:
                return XA_START;
            case USER_TYPE:
                return USER;
            case XA_PREPARED_TYPE:
                return XA_PREPARED;
            case XA_COMMIT_TYPE:
                return XA_COMMIT;
            case XA_DONE_TYPE:
                return XA_DONE;
            default:
                return UNKNOWN;
        }
    }

    private short type = -1;
    private String description = null;

    public XALogRecordType(short type, String description) {
        super();
        this.type = type;
        this.description = description;
    }

    public short getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public String toString() {
        return this.getDescription();
    }

    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + type;
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final XALogRecordType other = (XALogRecordType) obj;
        if (type != other.type)
            return false;
        return true;
    }


}
