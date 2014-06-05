package org.csc.phynixx.common.io;

/*
 * #%L
 * phynixx-common
 * %%
 * Copyright (C) 2014 Christoph Schmidt-Casdorff
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


import org.apache.commons.io.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * Created by christoph on 12.01.14.
 */
public class LogRecordWriter {


    private final ByteArrayOutputStream byteInput;

    private final DataOutputStream io;

    public LogRecordWriter() {
        this.byteInput = new ByteArrayOutputStream();
        this.io = new DataOutputStream(this.byteInput);
    }


    public byte[] toByteArray() throws IOException {
        this.io.flush();
        return byteInput.toByteArray();
    }

    public LogRecordWriter writeChar(int v) throws IOException {
        io.writeChar(v);
        return this;
    }

    public LogRecordWriter writeShort(int v) throws IOException {
        io.writeShort(v);
        return this;
    }

    public LogRecordWriter writeInt(int v) throws IOException {
        io.writeInt(v);
        return this;
    }

    public LogRecordWriter writeLong(long v) throws IOException {
        io.writeLong(v);
        return this;
    }

    public LogRecordWriter writeFloat(float v) throws IOException {
        io.writeFloat(v);
        return this;
    }

    public LogRecordWriter writeDouble(double v) throws IOException {
        io.writeDouble(v);
        return this;
    }

    public LogRecordWriter writeUTF(String str) throws IOException {
        io.writeUTF(str);
        return this;
    }

    public LogRecordWriter writeBoolean(boolean v) throws IOException {
        io.writeBoolean(v);
        return this;
    }

    /**
     * write the object's class name to check the consistency if the restroe fails.
     *
     * A null object is accepted
     *
     * @param object serializable object. it has to fullfill the requirements of {@link java.io.ObjectOutputStream#writeObject(Object)} .
     * @return returns the fluent API
     * @throws IOException
     */
    public LogRecordWriter writeObject(Object object) throws IOException {
        if( object==null) {
            return this.writeNullObject();
        }
        ObjectOutputStream out=null;
        try {
            ByteArrayOutputStream byteOutput= new ByteArrayOutputStream();
            out = new ObjectOutputStream(byteOutput);
            out.writeObject(object);
            out.flush();
            byte[] serBytes=byteOutput.toByteArray();
            io.writeInt(serBytes.length);
            io.write(serBytes);
        } finally {
            if(out!=null) {
                IOUtils.closeQuietly(out);
            }
        }
        return this;
    }

    /**
     * A null object is accepted
     *
     *  @return returns the fluent API
     * @throws IOException
     */
    private LogRecordWriter writeNullObject() throws IOException {
            io.writeInt(0);
            io.write(new byte[] {});
        return this;
    }

    public void close() {
        if (this.byteInput != null) {
            try {
                this.io.flush();
            } catch (IOException e) {
                // close may not fail
            }
            IOUtils.closeQuietly(this.byteInput);
        }
    }

}
